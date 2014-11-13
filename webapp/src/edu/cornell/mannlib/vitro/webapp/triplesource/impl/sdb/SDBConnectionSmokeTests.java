/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb;

import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.DEFAULT_DRIVER_CLASS;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_DRIVER_CLASS_NAME;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_PASSWORD;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_TYPE;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_URL;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_USERNAME;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;

/**
 * Smoke tests for the database connection that SDB will use.
 * 
 * Confirm that the URL, Username and Password have been specified for the
 * Database connection.
 * 
 * Confirm that we can load the database driver.
 * 
 * Confirm that we can connect to the database using those properties.
 * 
 * Try to confirm that the database connection is configured to use UTF-8
 * encoding. Don't know how well this works.
 */
public class SDBConnectionSmokeTests {
	private final ConfigurationProperties props;
	private final ComponentStartupStatus ss;

	public SDBConnectionSmokeTests(ServletContext ctx, ComponentStartupStatus ss) {
		this.props = ConfigurationProperties.getBean(ctx);
		this.ss = ss;
	}

	public void checkDatabaseConnection() {
		String url = props.getProperty(PROPERTY_DB_URL);
		if (url == null || url.isEmpty()) {
			ss.fatal("runtime.properties does not contain a value for '"
					+ PROPERTY_DB_URL + "'");
			return;
		}
		String username = props.getProperty(PROPERTY_DB_USERNAME);
		if (username == null || username.isEmpty()) {
			ss.fatal("runtime.properties does not contain a value for '"
					+ PROPERTY_DB_USERNAME + "'");
			return;
		}
		String password = props.getProperty(PROPERTY_DB_PASSWORD);
		if (password == null || password.isEmpty()) {
			ss.fatal("runtime.properties does not contain a value for '"
					+ PROPERTY_DB_PASSWORD + "'");
			return;
		}

		String driverClassName = props
				.getProperty(PROPERTY_DB_DRIVER_CLASS_NAME);
		if (driverClassName == null) {
			try {
				Class.forName(DEFAULT_DRIVER_CLASS).newInstance();
			} catch (Exception e) {
				ss.fatal("The default Database Driver failed to load. "
						+ "The driver class is '" + DEFAULT_DRIVER_CLASS + "'",
						e);
				return;
			}
		} else {
			try {
				Class.forName(driverClassName).newInstance();
			} catch (Exception e) {
				ss.fatal("The Database Driver failed to load. "
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
			ss.fatal("Can't connect to the database: " + PROPERTY_DB_URL + "='"
					+ url + "', " + PROPERTY_DB_USERNAME + "='" + username
					+ "'", e);
			return;
		}

		String dbType = props.getProperty(PROPERTY_DB_TYPE, "MySQL");
		checkForPropertHandlingOfUnicodeCharacters(url, connectionProps, dbType);
	}

	private void checkForPropertHandlingOfUnicodeCharacters(String url,
			Properties connectionProps, String dbType) {
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
					ss.fatal(message);
				} else {
					// For other databases, it might not be.
					ss.warning(message);
				}
			}
		} catch (SQLException e) {
			ss.fatal("Failed to check handling of Unicode characters", e);
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

}
