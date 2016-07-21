/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.ucsf.vitro.opensocial;

import java.io.File;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.utils.http.HttpClientFactory;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;
import org.apache.http.util.EntityUtils;

/**
 * Do some quick checks to see whether the OpenSocial stuff is configured and
 * working.
 */
public class OpenSocialSmokeTests implements ServletContextListener {
	private static final String PROPERTY_SHINDIG_URL = "OpenSocial.shindigURL";
	private static final String PROPERTY_SHINDIG_TOKEN_KEY_FILE = "OpenSocial.tokenKeyFile";
	private static final String PROPERTY_SHINDIG_TOKEN_SERVICE = "OpenSocial.tokenService";

	private static final String PROPERTY_DB_DRIVER = "VitroConnection.DataSource.driver";
	private static final String PROPERTY_DB_JDBC_URL = "VitroConnection.DataSource.url";
	private static final String PROPERTY_DB_USERNAME = "VitroConnection.DataSource.username";
	private static final String PROPERTY_DB_PASSWORD = "VitroConnection.DataSource.password";

	private static final String FILENAME_SHINDIG_PROPERTIES = "shindigorng.properties";

	/*
	 * If a connection fails in the tester thread, how long do we wait before
	 * trying again?
	 */
	private static final long SLEEP_INTERVAL = 20000; // 20 seconds

	private ServletContext ctx;
	private ConfigurationProperties configProps;
	private List<Warning> warnings = new ArrayList<Warning>();

	private String shindigBaseUrl;
	private String tokenServiceHost;
	private int tokenServicePort;

	/**
	 * When the system starts up, run the tests.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);
		configProps = ConfigurationProperties.getBean(ctx);

		/*
		 * If OpenSocial is not configured in runtime.properties, skip the
		 * tests.
		 */
		if (!configurationPresent()) {
			ss.info(this, "The OpenSocial connection is not configured.");
			return;
		}

		/*
		 * Run all of the non-threaded tests. If any fail, skip the threaded
		 * tests.
		 */
		checkDatabaseTables();
		checkShindigConfigFile();
		checkTokenKeyFile();
		checkTokenServiceInfo();
		if (!warnings.isEmpty()) {
			for (Warning w : warnings) {
				w.warn(ss);
			}
			return;
		}

		/*
		 * Run the threaded tests.
		 */
		ss.info(this, "Starting threads for OpenSocial smoke tests");
		new ShindigTestThread(this, ss, shindigBaseUrl).start();
		new TokenServiceTestThread(this, ss, tokenServiceHost, tokenServicePort)
				.start();
	}

	/**
	 * Get the base URL for the Shindig server. If none, then the whole thing is
	 * disabled.
	 */
	private boolean configurationPresent() {
		String shindigUrl = configProps.getProperty(PROPERTY_SHINDIG_URL);
		if (StringUtils.isNotEmpty(shindigUrl)) {
			this.shindigBaseUrl = shindigUrl;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check that we can connect to the database, and query one of the Shindig
	 * tables.
	 */
	private void checkDatabaseTables() {
		BasicDataSource dataSource = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		try {
			dataSource = new BasicDataSource();
			dataSource.setDriverClassName(getProperty(PROPERTY_DB_DRIVER));
			dataSource.setUrl(getProperty(PROPERTY_DB_JDBC_URL));
			dataSource.setUsername(getProperty(PROPERTY_DB_USERNAME));
			dataSource.setPassword(getProperty(PROPERTY_DB_PASSWORD));

			conn = dataSource.getConnection();
			stmt = conn.createStatement();
			rset = stmt.executeQuery("select * from orng_apps");
		} catch (NoSuchPropertyException e) {
			warnings.add(new Warning(e.getMessage()));
		} catch (SQLException e) {
			if (e.getMessage().contains("doesn't exist")) {
				warnings.add(new Warning("The Shindig tables don't exist "
						+ "in the database. Was shindig_orng_tables.sql "
						+ "run to set them up?", e));
			} else {
				warnings.add(new Warning(
						"Can't access the Shindig database tables", e));
			}
		} finally {
			try {
				if (rset != null) {
					rset.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Check that the Shindig configuration file is present in the classpath.
	 */
	private void checkShindigConfigFile() {
		URL url = this.getClass()
				.getResource("/" + FILENAME_SHINDIG_PROPERTIES);
		if (url == null) {
			String message = "Can't find the '" + FILENAME_SHINDIG_PROPERTIES
					+ "' file in the classpath. ";
			message += "Has the Tomcat classpath been set to include the "
					+ "Shindig config directory? "
					+ "(inside the Vitro home directory) ";
			message += "Was the openSocial build script run? ('ant orng')";
			warnings.add(new Warning(message));
		}
	}

	/**
	 * Check that the Token Key file has been specified in runtime.properties,
	 * and that it actually does exist.
	 */
	private void checkTokenKeyFile() {
		try {
			String tokenFilename = getProperty(PROPERTY_SHINDIG_TOKEN_KEY_FILE);
			File tokenFile = new File(tokenFilename);
			if (!tokenFile.exists()) {
				warnings.add(new Warning(
						"Token key file for Shindig does not exist: '"
								+ tokenFilename + "'"));
			} else if (!tokenFile.isFile()) {
				warnings.add(new Warning(
						"Token key file for Shindig is not a file: '"
								+ tokenFilename + "'"));
			}
		} catch (NoSuchPropertyException e) {
			warnings.add(new Warning(e.getMessage()));
		}
	}

	/**
	 * Get the Token Service info from runtime.properties. It must be in the
	 * form of host:port, and may not refer to localhost.
	 */
	private void checkTokenServiceInfo() {
		String tsInfo = configProps.getProperty(PROPERTY_SHINDIG_TOKEN_SERVICE);
		if (StringUtils.isEmpty(tsInfo)) {
			warnings.add(new Warning("There is no value for '"
					+ PROPERTY_SHINDIG_TOKEN_SERVICE
					+ "' in runtime.properties"));
			return;
		}

		/*
		 * If the parameter is invalid, use this message.
		 */
		String warningText = "The '" + PROPERTY_SHINDIG_TOKEN_SERVICE
				+ "' parameter is set to \"" + tsInfo
				+ "\". It must be in the form [hostname]:[port]. "
				+ "For example, \"myhost.mydomain.edu:8777\". "
				+ "The hostname may be an IP address, "
				+ "but it may not be \"localhost\" or \"127.0.0.1\"";

		int firstColon = tsInfo.indexOf(':');
		if (firstColon <= 0) {
			warnings.add(new Warning(warningText));
			return;
		}

		int lastColon = tsInfo.lastIndexOf(':');
		if (firstColon != lastColon) {
			warnings.add(new Warning(warningText));
			return;
		}

		tokenServiceHost = tsInfo.substring(0, firstColon);
		if (("localhost".equals(tokenServiceHost))
				|| ("127.0.0.1".equals(tokenServiceHost))) {
			warnings.add(new Warning(warningText));
			return;
		}

		try {
			tokenServicePort = Integer.parseInt(tsInfo
					.substring(firstColon + 1));
		} catch (Exception e) { // probably a NumberFormatException
			warnings.add(new Warning(warningText, e));
		}
	}

	private String getProperty(String key) throws NoSuchPropertyException {
		String value = configProps.getProperty(key);
		if (StringUtils.isEmpty(value)) {
			throw new NoSuchPropertyException(key);
		} else {
			return value;
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// nothing to destroy
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class NoSuchPropertyException extends Exception {
		NoSuchPropertyException(String key) {
			super("There is no value for '" + key + "' in build.properties");
		}
	}

	private class Warning {
		private final String message;
		private final Throwable cause;

		Warning(String message) {
			this.message = message;
			this.cause = null;
		}

		Warning(String message, Throwable cause) {
			this.message = message;
			this.cause = cause;
		}

		void warn(StartupStatus ss) {
			if (cause == null) {
				ss.warning(OpenSocialSmokeTests.this, message);
			} else {
				ss.warning(OpenSocialSmokeTests.this, message, cause);
			}
		}
	}

	private static class ShindigTestThread extends VitroBackgroundThread {
		private final OpenSocialSmokeTests listener;
		private final StartupStatus ss;
		private final String shindigBaseUrl;

		public ShindigTestThread(OpenSocialSmokeTests listener,
				StartupStatus ss, String shindigBaseUrl) {
			super("OpenSocialSmokeTest.ShindigTestThread");
			this.listener = listener;
			this.ss = ss;
			this.shindigBaseUrl = shindigBaseUrl;
		}

		@Override
		public void run() {
			try {
				new ShindigTester(shindigBaseUrl).connect();
				ss.info(listener, "Shindig service responds to a REST query.");
			} catch (ShindigTesterException e) {
				String message = e.getMessage();
				Throwable cause = e.getCause();
				if (cause == null) {
					ss.warning(listener, message);
				} else {
					ss.warning(listener, message, cause);
				}
				return;
			}
		}
	}

	private static class ShindigTester {
		// Use the parent's log
		private static final Log log = LogFactory
				.getLog(OpenSocialSmokeTests.class);

		/** Pretend that there is an HTTP status code for this. */
		private static final int SOCKET_TIMEOUT_STATUS = -500;

		private final String shindigBaseUrl;
		private final String shindigTestUrl;
		private final HttpClient httpClient = HttpClientFactory.getHttpClient();

		private int statusCode = Integer.MIN_VALUE;

		public ShindigTester(String shindigBaseUrl) {
			this.shindigBaseUrl = shindigBaseUrl;
			this.shindigTestUrl = shindigBaseUrl + "/rest/appdata";
		}

		public void connect() throws ShindigTesterException {
			testConnection();

			if (!isDone()) {
				sleep();
				testConnection();
			}

			if (!isDone()) {
				sleep();
				testConnection();
			}

			if (statusCode != HttpStatus.SC_OK) {
				throw new ShindigTesterException(statusCode, shindigBaseUrl,
						shindigTestUrl);
			}
		}

		private void testConnection() throws ShindigTesterException {
			HttpGet method = new HttpGet(shindigTestUrl);
			try {
				log.debug("Trying to connect to Shindig");
				HttpResponse response = httpClient.execute(method);
				try {
					statusCode = response.getStatusLine().getStatusCode();
					log.debug("HTTP status was " + statusCode);
				} finally {
					EntityUtils.consume(response.getEntity());
				}
			} catch (SocketTimeoutException e) {
				// Catch the exception so we can retry this.
				// Save the status so we know why we failed.
				statusCode = SOCKET_TIMEOUT_STATUS;
			} catch (Exception e) {
				throw new ShindigTesterException(e, shindigBaseUrl,
						shindigTestUrl);
			}
		}

		/**
		 * Stop trying to connect if we succeed, or if we receive an error that
		 * won't change on retry.
		 */
		private boolean isDone() {
			return (statusCode == HttpStatus.SC_OK)
					|| (statusCode == HttpStatus.SC_FORBIDDEN);
		}

		private void sleep() {
			try {
				Thread.sleep(SLEEP_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace(); // Should never happen
			}
		}
	}

	protected static class ShindigTesterException extends Exception {
		private final int httpStatusCode;

		protected ShindigTesterException(Integer httpStatusCode,
				String baseUrl, String testUrl) {
			super("Failed to connect to the Shindig service at '" + baseUrl
					+ "' (tried for a response at '" + testUrl + "'). "
					+ "status code was " + httpStatusCode);
			this.httpStatusCode = httpStatusCode;
		}

		protected ShindigTesterException(Throwable cause, String baseUrl,
				String testUrl) {
			super("Failed to connect to the Shindig service at '" + baseUrl
					+ "' (tried for a response at '" + testUrl + "').", cause);
			this.httpStatusCode = Integer.MIN_VALUE;
		}

		protected int getHttpStatusCode() {
			return httpStatusCode;
		}
	}

	private static class TokenServiceTestThread extends VitroBackgroundThread {
		private final OpenSocialSmokeTests listener;
		private final StartupStatus ss;
		private final String tokenServiceHost;
		private final int tokenServicePort;

		public TokenServiceTestThread(OpenSocialSmokeTests listener,
				StartupStatus ss, String tokenServiceHost, int tokenServicePort) {
			super("OpenSocialSmokeTest.TokenServiceTestThread");
			this.listener = listener;
			this.ss = ss;
			this.tokenServiceHost = tokenServiceHost;
			this.tokenServicePort = tokenServicePort;
		}

		@Override
		public void run() {
			try {
				new TokenServiceTester(tokenServiceHost, tokenServicePort)
						.connect();
				ss.info(listener,
						"Shindig security token service responds to a request.");
			} catch (TokenServiceTesterException e) {
				String message = e.getMessage();
				Throwable cause = e.getCause();
				if (cause == null) {
					ss.warning(listener, message);
				} else {
					ss.warning(listener, message, cause);
				}
				return;
			}
		}
	}

	protected static class TokenServiceTester {
		// Use the parent's log
		private static final Log log = LogFactory
				.getLog(OpenSocialSmokeTests.class);

		private final String host;
		private final int port;

		private Object problem;

		public TokenServiceTester(String host, int port) {
			this.host = host;
			this.port = port;
		}

		public void connect() throws TokenServiceTesterException {
			testConnection();

			if (!isDone()) {
				sleep();
				testConnection();
			}

			if (!isDone()) {
				sleep();
				testConnection();
			}

			if (problem instanceof Throwable) {
				throw new TokenServiceTesterException(
						"Test of the Shindig token service failed.",
						(Throwable) problem);
			} else if (problem instanceof String) {
				throw new TokenServiceTesterException((String) problem);
			}
		}

		private void testConnection() {
			try {
				log.debug("Connecting to the token service");
				Socket s = new Socket(host, port);
				try {
					s.getOutputStream().write("c=default\n".getBytes());

					int byteCount = 0;
					int totalBytecount = 0;
					byte[] buffer = new byte[8192];

					// The following will block until the page is transmitted.
					InputStream inputStream = s.getInputStream();
					while ((byteCount = inputStream.read(buffer)) > 0) {
						totalBytecount += byteCount;
					}

					if (totalBytecount == 0) {
						log.debug("Received an empty response.");
						problem = "The Shindig security token service responded to a test, but the response was empty.";
					} else {
						log.debug("Recieved the token.");
						problem = null;
					}
				} finally {
					s.close();
				}
			} catch (Exception e) {
				log.debug("Problem with the token service", e);
				problem = e;
			}
		}

		/**
		 * Stop trying to connect if we succeed, or if we receive an error that
		 * won't change on retry.
		 */
		private boolean isDone() {
			return ((problem == null) || (problem instanceof String));
		}

		private void sleep() {
			try {
				Thread.sleep(SLEEP_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace(); // Should never happen
			}
		}

	}

	protected static class TokenServiceTesterException extends Exception {
		protected TokenServiceTesterException(String message) {
			super(message);
		}

		protected TokenServiceTesterException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
