/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * If there are RDB tables in the database that have not been converted to TDB,
 * throw an error.
 * 
 * The ConfigurationPropertiesSmokeTests has already run, so we know we can
 * access the database. A table named "jena_graph", means that the database
 * contains RDB. A table named "vivo_rdb_migrated" means that the conversion
 * utility has been run.
 */
public class GuardAgainstUnmigratedRDB implements ServletContextListener {
	private static final String PROPERTY_DB_URL = "VitroConnection.DataSource.url";
	private static final String PROPERTY_DB_USERNAME = "VitroConnection.DataSource.username";
	private static final String PROPERTY_DB_PASSWORD = "VitroConnection.DataSource.password";

	private static final String TABLE_NAME_RDB = "jena_graph";
	private static final String TABLE_NAME_CONVERSION = "vivo_rdb_migrated";

	private static final String MESSAGE_PROBLEM = "The database at %s"
			+ " contains data from an earlier VIVO (before 1.7). "
			+ "It does not appear that this data has been migrated. "
			+ "The upgrade guide has instructions on migrating "
			+ "this data to the current VIVO.";
	private static final String MESSAGE_TECHNICAL = "More technically: "
			+ "the database contains tables used by Jena RDB ('jena_graph' and "
			+ "others). It does not contain the table 'vivo_rdb_migrated', "
			+ "which is created when the data is migrated to Jena TDB files.";
	private static final String MESSAGE_WHAT_NOW = "You must either migrate "
			+ "the obsolete RDB data or remove it from your database.";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		ConfigurationProperties props = ConfigurationProperties.getBean(ctx);
		StartupStatus ss = StartupStatus.getBean(ctx);

		String url = props.getProperty(PROPERTY_DB_URL);
		String username = props.getProperty(PROPERTY_DB_USERNAME);
		String password = props.getProperty(PROPERTY_DB_PASSWORD);

		Properties connectionProps = new Properties();
		connectionProps.put("user", username);
		connectionProps.put("password", password);

		try (Connection conn = DriverManager
				.getConnection(url, connectionProps)) {
			boolean hasRdb = checkForRdbTables(conn);
			boolean hasBeenConverted = checkForConversionTable(conn);
			if (hasRdb && !hasBeenConverted) {
				ss.fatal(this, String.format(MESSAGE_PROBLEM, url));
				ss.fatal(this, String.format(MESSAGE_TECHNICAL, url));
				ss.fatal(this, String.format(MESSAGE_WHAT_NOW, url));
			}
		} catch (SQLException e) {
			ss.fatal(this, "Can't connect to the database: " + PROPERTY_DB_URL
					+ "='" + url + "', " + PROPERTY_DB_USERNAME + "='"
					+ username + "'", e);
			return;
		}
	}

	private boolean checkForRdbTables(Connection conn) throws SQLException {
		DatabaseMetaData md = conn.getMetaData();
		try (ResultSet rs = md.getTables(null, null, TABLE_NAME_RDB, null);) {
			while (rs.next()) {
				return true;
			}
		}
		return false;
	}

	private boolean checkForConversionTable(Connection conn)
			throws SQLException {
		DatabaseMetaData md = conn.getMetaData();
		try (ResultSet rs = md.getTables(null, null, TABLE_NAME_CONVERSION,
				null);) {
			while (rs.next()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to tear down.
	}

}
