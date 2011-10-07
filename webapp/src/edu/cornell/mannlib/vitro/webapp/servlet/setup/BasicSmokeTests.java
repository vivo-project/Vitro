/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

public class BasicSmokeTests implements ServletContextListener {

	private static final String PROPERTY_HOME_DIRECTORY = "vitro.home.directory";
	private static final String PROPERTY_DB_URL = "VitroConnection.DataSource.url";
	private static final String PROPERTY_DB_USERNAME = "VitroConnection.DataSource.username";
	private static final String PROPERTY_DB_PASSWORD = "VitroConnection.DataSource.password";
	private static final String PROPERTY_DB_DRIVER_CLASS_NAME = "VitroConnection.DataSource.driver";

	private static final String DEFAULT_DB_DRIVER_CLASS = "com.mysql.jdbc.Driver";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		ConfigurationProperties props = ConfigurationProperties.getBean(ctx);
		StartupStatus ss = StartupStatus.getBean(ctx);

		checkHomeDirectory(ctx, props, ss);
		checkDatabaseConnection(ctx, props, ss);
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

		Properties connectionProps = new Properties();
		connectionProps.put("user", username);
		connectionProps.put("password", password);

		String driverClassName = props
				.getProperty(PROPERTY_DB_DRIVER_CLASS_NAME);
		if (driverClassName == null) {
			try {
				Class.forName(DEFAULT_DB_DRIVER_CLASS).newInstance();
			} catch (Exception e) {
				ss.fatal(this, "The default Database Driver failed to load. "
						+ "The driver class is '" + DEFAULT_DB_DRIVER_CLASS
						+ "'", e);
			}
		} else {
			try {
				Class.forName(driverClassName).newInstance();
			} catch (Exception e) {
				ss.fatal(this, "The Database Driver failed to load. "
						+ "The driver class was set by "
						+ PROPERTY_DB_DRIVER_CLASS_NAME + " to be '"
						+ driverClassName + "'", e);
			}
		}

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, connectionProps);
			conn.close();
		} catch (SQLException e) {
			ss.fatal(this, "Can't connect to the database: " + PROPERTY_DB_URL
					+ "='" + url + "', " + PROPERTY_DB_USERNAME + "='"
					+ username + "'", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// nothing to do at shutdown
	}

}
