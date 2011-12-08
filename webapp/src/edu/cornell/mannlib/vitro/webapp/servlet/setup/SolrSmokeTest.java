/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

/**
 * Spin off a thread that will try to connect to Solr.
 * 
 * We need to do this in a separate thread because if Solr is in the same Tomcat
 * instance as Vitro, it may not be initialized until after Vitro is
 * initialized. Which is to say, after this Listener has run.
 * 
 * If we can't connect to Solr, add a Warning item to the StartupStatus.
 */
public class SolrSmokeTest implements ServletContextListener {
	private static final Log log = LogFactory.getLog(SolrSmokeTest.class);

	/*
	 * We don't want to treat socket timeout as a non-recoverable error like the
	 * other exceptions. So pretend there's a status code for it instead.
	 */
	private static final int SOCKET_TIMEOUT_STATUS = -500;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		final StartupStatus ss = StartupStatus.getBean(sce.getServletContext());

		String solrUrlString = ConfigurationProperties.getBean(sce)
				.getProperty("vitro.local.solr.url", "");
		if (solrUrlString.isEmpty()) {
			ss.fatal(this, "Can't connect to Solr search engine. "
					+ "deploy.properties must contain a value for "
					+ "vitro.local.solr.url");
			return;
		}

		URL solrUrl = null;

		try {
			solrUrl = new URL(solrUrlString);
		} catch (MalformedURLException e) {
			ss.fatal(this, "Can't connect to Solr search engine. "
					+ "The value for vitro.local.solr.url "
					+ "in deploy.properties is not a valid URL: '"
					+ solrUrlString + "'", e);
		}

		ss.info(this, "Starting thread for Solr test.");
		new SolrSmokeTestThread(this, solrUrl, ss).start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// nothing to tear down.
	}

	private static class SolrSmokeTestThread extends VitroBackgroundThread {
		private final SolrSmokeTest listener;
		private final URL solrUrl;
		private final StartupStatus ss;

		public SolrSmokeTestThread(SolrSmokeTest listener, URL solrUrl,
				StartupStatus ss) {
			super("SolrSmokeTest");
			this.listener = listener;
			this.solrUrl = solrUrl;
			this.ss = ss;
		}

		/**
		 * Try to connect until we suceed, until we detect an unrecoverable
		 * error, or until we have failed 3 times.
		 */
		@Override
		public void run() {
			try {
				new SolrHomePager(solrUrl).connect();
				reportSuccess();
			} catch (SolrProblemException e) {
				reportProblem(e);
				return;
			}

			try {
				new SolrPinger(solrUrl).ping();
				reportGoodPing();
			} catch (SolrProblemException e) {
				reportPingProblem(e);
				return;
			}
		}

		private void reportSuccess() {
			ss.info(listener,
					"Successfully connected to the Solr search server.");
		}

		private void reportGoodPing() {
			ss.info(listener, "The Solr search server responded to a 'ping'.");
		}

		private void reportProblem(SolrProblemException e) {
			int status = e.getStatusCode();
			Throwable cause = e.getCause();

			if (status == HttpStatus.SC_FORBIDDEN) {
				warnForbidden();
			} else if (status == SOCKET_TIMEOUT_STATUS) {
				warnSocketTimeout();
			} else if (status != 0) {
				warnBadHttpStatus(status);
			} else if (cause instanceof HttpException) {
				warnProtocolViolation((HttpException) cause);
			} else if (cause instanceof UnknownHostException) {
				warnUnknownHost((UnknownHostException) cause);
			} else if (cause instanceof ConnectException) {
				warnConnectionRefused((ConnectException) cause);
			} else if (cause instanceof IOException) {
				warnTransportError((IOException) cause);
			} else {
				warnUnknownProblem(e);
			}
		}

		private void reportPingProblem(SolrProblemException e) {
			ss.warning(listener, "The Solr search engine did not respond to a 'ping' request", e);
		}

		private void warnSocketTimeout() {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "The socket connection has repeatedly timed out. "
					+ "Check the value of vitro.local.solr.url in "
					+ "deploy.properties. Is Solr responding at that URL?");
		}

		private void warnForbidden() {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "The Solr server will not accept connections from this "
					+ "host. Check the value of "
					+ "vitro.local.solr.ipaddress.mask in "
					+ "deploy.properties -- "
					+ "does it authorize access from this IP address?");
		}

		private void warnBadHttpStatus(int status) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "The Solr server returned a status code of " + status
					+ ". Check the value of vitro.local.solr.url in "
					+ "deploy.properties.");
		}

		private void warnProtocolViolation(HttpException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "Detected a protocol violation: " + e.getMessage(), e);
		}

		private void warnUnknownHost(UnknownHostException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. '"
					+ e.getMessage() + "' is an unknown host."
					+ "Check the value of vitro.local.solr.url in "
					+ "deploy.properties.", e);
		}

		private void warnConnectionRefused(ConnectException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "The host refused the connection. "
					+ "Is it possible that the port number is incorrect? "
					+ "Check the value of vitro.local.solr.url in "
					+ "deploy.properties.", e);
		}

		private void warnTransportError(IOException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "Detected a transport error: " + e.getMessage(), e);
		}

		private void warnUnknownProblem(SolrProblemException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "Unrecognized error: " + e.getMessage(), e);
		}

	}

	/**
	 * <pre>
	 * This gets a little tricky. Try to connect until:
	 *   -- we suceed
	 *   -- we detect a non-recoverable error
	 *   -- we fail three times
	 * </pre>
	 */
	private static class SolrHomePager {
		private static final long SLEEP_INTERVAL = 10000; // 10 seconds

		private final URL solrUrl;
		private final HttpClient httpClient = new HttpClient();

		private int statusCode;

		public SolrHomePager(URL solrUrl) {
			this.solrUrl = solrUrl;
		}

		public void connect() throws SolrProblemException {
			tryToConnect();

			if (!isDone()) {
				sleep();
				tryToConnect();
			}

			if (!isDone()) {
				sleep();
				tryToConnect();
			}

			if (statusCode != HttpStatus.SC_OK) {
				throw new SolrProblemException(statusCode);
			}
		}

		private void tryToConnect() throws SolrProblemException {
			GetMethod method = new GetMethod(solrUrl.toExternalForm());
			try {
				SolrSmokeTest.log.debug("Trying to connect to Solr");
				statusCode = httpClient.executeMethod(method);
				SolrSmokeTest.log.debug("HTTP status was " + statusCode);

				// clear the buffer.
				InputStream stream = method.getResponseBodyAsStream();
				stream.close();
			} catch (SocketTimeoutException e) {
				// Catch the exception so we can retry this.
				// Save the status so we know why we failed.
				statusCode = SolrSmokeTest.SOCKET_TIMEOUT_STATUS;
			} catch (Exception e) {
				throw new SolrProblemException(e);
			} finally {
				method.releaseConnection();
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

	/**
	 * Issue a "ping" to Solr. If we get here, we've already established
	 * contact, so any error is a fatal one.
	 */
	private static class SolrPinger {
		private final URL solrUrl;
		private final HttpClient httpClient = new HttpClient();

		public SolrPinger(URL solrUrl) {
			this.solrUrl = solrUrl;
		}

		public void ping() throws SolrProblemException {
			GetMethod method = new GetMethod(solrUrl.toExternalForm() + "/admin/ping");
			try {
				SolrSmokeTest.log.debug("Trying to ping Solr");
				int statusCode = httpClient.executeMethod(method);
				SolrSmokeTest.log.debug("Finished pinging Solr");
				
				// clear the buffer.
				InputStream stream = method.getResponseBodyAsStream();
				stream.close();
				
				if (statusCode != HttpStatus.SC_OK) {
					throw new SolrProblemException(statusCode);
				}
			} catch (IOException e) {
				throw new SolrProblemException(e);
			} finally {
				method.releaseConnection();
			}
		}
	}

	private static class SolrProblemException extends Exception {
		private final int statusCode;

		SolrProblemException(int statusCode) {
			super("HTTP status code = " + statusCode);
			this.statusCode = statusCode;
		}

		SolrProblemException(Throwable cause) {
			super(cause);
			this.statusCode = 0;
		}

		int getStatusCode() {
			return this.statusCode;
		}
	}
}
