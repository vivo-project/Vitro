/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.config;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Test that gets run at servlet context startup to check for the existence and
 * validity of properties in the configuration.
 */
public class ConfigurationPropertiesSmokeTests implements
		ServletContextListener {
	private static final Log log = LogFactory
			.getLog(ConfigurationPropertiesSmokeTests.class);

	private static final String PROPERTY_HOME_DIRECTORY = "vitro.home";
	private static final String PROPERTY_DB_URL = "VitroConnection.DataSource.url";
	private static final String PROPERTY_DB_USERNAME = "VitroConnection.DataSource.username";
	private static final String PROPERTY_DB_PASSWORD = "VitroConnection.DataSource.password";
	private static final String PROPERTY_DB_DRIVER_CLASS_NAME = "VitroConnection.DataSource.driver";
	private static final String PROPERTY_DB_TYPE = "VitroConnection.DataSource.dbtype";
	private static final String PROPERTY_DEFAULT_NAMESPACE = "Vitro.defaultNamespace";
	private static final String PROPERTY_LANGUAGE_BUILD = "languages.addToBuild";
	private static final String PROPERTY_LANGUAGE_SELECTABLE = "languages.selectableLocales";
	private static final String PROPERTY_LANGUAGE_FORCE = "languages.forceLocale";
	private static final String PROPERTY_LANGUAGE_FILTER = "RDFService.languageFilter";

	private static final String DEFAULT_DB_DRIVER_CLASS = "com.mysql.jdbc.Driver";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		ConfigurationProperties props = ConfigurationProperties.getBean(ctx);
		StartupStatus ss = StartupStatus.getBean(ctx);

		checkHomeDirectory(ctx, props, ss);
		checkDatabaseConnection(ctx, props, ss);
		checkDefaultNamespace(ctx, props, ss);
		checkLanguages(props, ss);
	}

	/**
	 * Confirm that: a home directory has been specified; it exists; it is a
	 * directory; it is readable and writable.
	 */
	private void checkHomeDirectory(ServletContext ctx,
			ConfigurationProperties props, StartupStatus ss) {
		String homeDirectoryPath = props.getProperty(PROPERTY_HOME_DIRECTORY);
		if (homeDirectoryPath == null || homeDirectoryPath.isEmpty()) {
			ss.fatal(this, "Can't find a value for the home directory: '"
					+ PROPERTY_HOME_DIRECTORY + "'");
			return;
		}

		File homeDirectory = new File(homeDirectoryPath);
		if (!homeDirectory.exists()) {
			ss.fatal(this, PROPERTY_HOME_DIRECTORY + " '" + homeDirectoryPath
					+ "' does not exist.");
			return;
		}
		if (!homeDirectory.isDirectory()) {
			ss.fatal(this, PROPERTY_HOME_DIRECTORY + " '" + homeDirectoryPath
					+ "' is not a directory.");
			return;
		}

		if (!homeDirectory.canRead()) {
			ss.fatal(this, PROPERTY_HOME_DIRECTORY + " '" + homeDirectoryPath
					+ "' cannot be read.");
		}
		if (!homeDirectory.canWrite()) {
			ss.fatal(this, PROPERTY_HOME_DIRECTORY + " '" + homeDirectoryPath
					+ "' cannot be written to.");
		}
	}

	/**
	 * Confirm that the URL, Username and Password have been specified for the
	 * Database connection. Confirm that we can load the database driver.
	 * Confirm that we can connect to the database using those properties.
	 */
	private void checkDatabaseConnection(ServletContext ctx,
			ConfigurationProperties props, StartupStatus ss) {
		String url = props.getProperty(PROPERTY_DB_URL);
		if (url == null || url.isEmpty()) {
			ss.fatal(this, "runtime.properties does not contain a value for '"
					+ PROPERTY_DB_URL + "'");
			return;
		}
		String username = props.getProperty(PROPERTY_DB_USERNAME);
		if (username == null || username.isEmpty()) {
			ss.fatal(this, "runtime.properties does not contain a value for '"
					+ PROPERTY_DB_USERNAME + "'");
			return;
		}
		String password = props.getProperty(PROPERTY_DB_PASSWORD);
		if (password == null || password.isEmpty()) {
			ss.fatal(this, "runtime.properties does not contain a value for '"
					+ PROPERTY_DB_PASSWORD + "'");
			return;
		}

		String driverClassName = props
				.getProperty(PROPERTY_DB_DRIVER_CLASS_NAME);
		if (driverClassName == null) {
			try {
				Class.forName(DEFAULT_DB_DRIVER_CLASS).newInstance();
			} catch (Exception e) {
				ss.fatal(this, "The default Database Driver failed to load. "
						+ "The driver class is '" + DEFAULT_DB_DRIVER_CLASS
						+ "'", e);
				return;
			}
		} else {
			try {
				Class.forName(driverClassName).newInstance();
			} catch (Exception e) {
				ss.fatal(this, "The Database Driver failed to load. "
						+ "The driver class was set by "
						+ PROPERTY_DB_DRIVER_CLASS_NAME + " to be '"
						+ driverClassName + "'", e);
				return;
			}
		}

		Properties connectionProps = new Properties();
		connectionProps.put("user", username);
		connectionProps.put("password", password);

		try (Connection conn = DriverManager
				.getConnection(url, connectionProps)) {
			// Just open the connection and close it.
		} catch (SQLException e) {
			ss.fatal(this, "Can't connect to the database: " + PROPERTY_DB_URL
					+ "='" + url + "', " + PROPERTY_DB_USERNAME + "='"
					+ username + "'", e);
			return;
		}

		String dbType = props.getProperty(PROPERTY_DB_TYPE, "MySQL");
		checkForPropertHandlingOfUnicodeCharacters(url, connectionProps, ss,
				dbType);
	}

	private void checkForPropertHandlingOfUnicodeCharacters(String url,
			Properties connectionProps, StartupStatus ss, String dbType) {
		String testString = "ABC\u00CE\u0123";

		try (Connection conn = DriverManager
				.getConnection(url, connectionProps);
				Statement stmt = conn.createStatement()) {

			// Create the temporary table.
			stmt.executeUpdate("CREATE TEMPORARY TABLE smoke_test (contents varchar(100))");

			// Write the test string, encoding in UTF-8 on the way in.
			try (PreparedStatement pstmt = conn
					.prepareStatement("INSERT INTO smoke_test values ( ? )")) {
				pstmt.setBytes(1, testString.getBytes("UTF-8"));
				pstmt.executeUpdate();
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}

			// Read it back as a String. Does the database decode it properly?
			ResultSet rs = stmt.executeQuery("SELECT * FROM smoke_test");
			if (!rs.next()) {
				throw new SQLException(
						"Query of temporary table returned 0 rows.");
			}
			String storedValue = rs.getString(1);
			if (!testString.equals(storedValue)) {
				String message = "The database does not store Unicode "
						+ "characters correctly. The test inserted \""
						+ showUnicode(testString)
						+ "\", but the query returned \""
						+ showUnicode(storedValue)
						+ "\". Is the character encoding correctly "
						+ "set on the database?";
				if ("MySQL".equals(dbType)) {
					// For MySQL, we know that this is a configuration problem.
					ss.fatal(this, message);
				} else {
					// For other databases, it might not be.
					ss.warning(this, message);
				}
			}
		} catch (SQLException e) {
			ss.fatal(this, "Failed to check handling of Unicode characters", e);
		}
	}

	/**
	 * Display the hex codes for a String.
	 */
	private String showUnicode(String testString) {
		StringBuilder u = new StringBuilder();
		for (char c : testString.toCharArray()) {
			u.append(String.format("\\u%04x", c & 0x0000FFFF));
		}
		return u.toString();
	}

	/**
	 * Confirm that the default namespace is specified and a syntactically valid
	 * URI. It should also end with "/individual/".
	 */
	private void checkDefaultNamespace(ServletContext ctx,
			ConfigurationProperties props, StartupStatus ss) {
		String ns = props.getProperty(PROPERTY_DEFAULT_NAMESPACE);
		if (ns == null || ns.isEmpty()) {
			ss.fatal(this, "runtime.properties does not contain a value for '"
					+ PROPERTY_DEFAULT_NAMESPACE + "'");
			return;
		}

		try {
			new URI(ns);
		} catch (URISyntaxException e) {
			ss.fatal(this,
					PROPERTY_DEFAULT_NAMESPACE + " '" + ns
							+ "' is not a valid URI. "
							+ (e.getMessage() != null ? e.getMessage() : ""));
			return;
		}

		String suffix = "/individual/";
		if (!ns.endsWith(suffix)) {
			ss.warning(this,
					"Default namespace does not match the expected form "
							+ "(does not end with '" + suffix + "'): '" + ns
							+ "'");
		}
	}

	/**
	 * Warn if we set up the languages incorrectly:
	 * 
	 * Must build with a language in order to select languages. Can't select
	 * languages and force language. Shouldn't build with language unless
	 * language filtering is enabled.
	 */
	private void checkLanguages(ConfigurationProperties props, StartupStatus ss) {
		String buildString = props.getProperty(PROPERTY_LANGUAGE_BUILD);
		boolean buildWithLanguages = StringUtils.isNotBlank(buildString);

		String selectString = props.getProperty(PROPERTY_LANGUAGE_SELECTABLE);
		boolean selectableLanguages = StringUtils.isNotBlank(selectString);

		String forceString = props.getProperty(PROPERTY_LANGUAGE_FORCE);
		boolean forceLanguage = StringUtils.isNotBlank(forceString);

		String filterString = props.getProperty(PROPERTY_LANGUAGE_FILTER,
				"true");
		boolean languageFilter = Boolean.valueOf(filterString);

		if (selectableLanguages && !buildWithLanguages) {
			ss.warning(this, String.format("Problem with Language setup - "
					+ "runtime.properties specifies a "
					+ "list of selectable languages (%s = %s), but "
					+ "build.properties did not include any languages with %s",
					PROPERTY_LANGUAGE_SELECTABLE, selectString,
					PROPERTY_LANGUAGE_BUILD));
		}

		if (selectableLanguages && forceLanguage) {
			ss.warning(this, String.format("Problem with Language setup - "
					+ "runtime.properties specifies a "
					+ "forced locale (%s = %s), and also a list of selectable "
					+ "languages (%s = %s). These options are incompatible.",
					PROPERTY_LANGUAGE_FORCE, forceString,
					PROPERTY_LANGUAGE_SELECTABLE, selectString));
		}

		if (buildWithLanguages && !languageFilter) {
			ss.warning(this, String.format("Problem with Language setup - "
					+ "build.properties includes one or more additional "
					+ "languages (%s = %s), but runtime.properties has "
					+ "disabled language filtering (%s = %s). This will "
					+ "likely result in a mix of languages in the "
					+ "application.", PROPERTY_LANGUAGE_BUILD, buildString,
					PROPERTY_LANGUAGE_FILTER, filterString));
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// nothing to do at shutdown
	}

}
