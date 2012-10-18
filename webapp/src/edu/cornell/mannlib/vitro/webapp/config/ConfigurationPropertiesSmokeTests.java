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

	private static final String PROPERTY_HOME_DIRECTORY = "vitro.home.directory";
	private static final String PROPERTY_DB_URL = "VitroConnection.DataSource.url";
	private static final String PROPERTY_DB_USERNAME = "VitroConnection.DataSource.username";
	private static final String PROPERTY_DB_PASSWORD = "VitroConnection.DataSource.password";
	private static final String PROPERTY_DB_DRIVER_CLASS_NAME = "VitroConnection.DataSource.driver";
	private static final String PROPERTY_DB_TYPE = "VitroConnection.DataSource.dbtype";
	private static final String PROPERTY_DEFAULT_NAMESPACE = "Vitro.defaultNamespace";

	private static final String DEFAULT_DB_DRIVER_CLASS = "com.mysql.jdbc.Driver";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		ConfigurationProperties props = ConfigurationProperties.getBean(ctx);
		StartupStatus ss = StartupStatus.getBean(ctx);

		checkHomeDirectory(ctx, props, ss);
		checkDatabaseConnection(ctx, props, ss);
		checkDefaultNamespace(ctx, props, ss);
	}

	/**
	 * Confirm that: a home directory has been specified; it exists; it is a
	 * directory; it is readable and writable.
	 */
	private void checkHomeDirectory(ServletContext ctx,
			ConfigurationProperties props, StartupStatus ss) {
		String homeDirectoryPath = props.getProperty(PROPERTY_HOME_DIRECTORY);
		if (homeDirectoryPath == null || homeDirectoryPath.isEmpty()) {
			ss.fatal(this, "deploy.properties does not contain a value for '"
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
			ss.fatal(this, "deploy.properties does not contain a value for '"
					+ PROPERTY_DB_URL + "'");
			return;
		}
		String username = props.getProperty(PROPERTY_DB_USERNAME);
		if (username == null || username.isEmpty()) {
			ss.fatal(this, "deploy.properties does not contain a value for '"
					+ PROPERTY_DB_USERNAME + "'");
			return;
		}
		String password = props.getProperty(PROPERTY_DB_PASSWORD);
		if (password == null || password.isEmpty()) {
			ss.fatal(this, "deploy.properties does not contain a value for '"
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

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, connectionProps);
			closeConnection(conn);
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

		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		try {
			// Get the connection.
			conn = DriverManager.getConnection(url, connectionProps);

			// Create the temporary table.
			stmt = conn.createStatement();
			stmt.executeUpdate("CREATE TEMPORARY TABLE smoke_test (contents varchar(100))");

			// Write the test string, encoding in UTF-8 on the way in.
			try {
				pstmt = conn
						.prepareStatement("INSERT INTO smoke_test values ( ? )");
				pstmt.setBytes(1, testString.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			pstmt.executeUpdate();

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
		} finally {
			closeStatement(pstmt);
			closeStatement(stmt);
			closeConnection(conn);
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
	 * Close the statement, catching any exception.
	 */
	private void closeStatement(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				log.error("Failed to close SQL statement", e);
			}
		}
	}

	/**
	 * Close the connection, catching any exception.
	 */
	private void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.error("Failed to close database connection", e);
			}
		}
	}

	/**
	 * Confirm that the default namespace is specified and a syntactically valid
	 * URI. It should also end with "/individual/".
	 */
	private void checkDefaultNamespace(ServletContext ctx,
			ConfigurationProperties props, StartupStatus ss) {
		String ns = props.getProperty(PROPERTY_DEFAULT_NAMESPACE);
		if (ns == null || ns.isEmpty()) {
			ss.fatal(this, "deploy.properties does not contain a value for '"
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

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// nothing to do at shutdown
	}

}
