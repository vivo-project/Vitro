/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.io.File;
import java.util.Properties;

/**
 * Hold the runtime properties that pertain specifically to cleaning the data
 * model.
 */
public class ModelCleanerProperties {
	public static final String PROP_TOMCAT_START_COMMAND = "tomcat_start_command";
	public static final String PROP_TOMCAT_START_DELAY = "tomcat_start_delay";
	public static final String PROP_TOMCAT_STOP_COMMAND = "tomcat_stop_command";
	public static final String PROP_TOMCAT_STOP_DELAY = "tomcat_stop_delay";
	public static final String PROP_MYSQL_USERNAME = "mysql_username";
	public static final String PROP_MYSQL_PASSWORD = "mysql_password";
	public static final String PROP_MYSQL_DB_NAME = "mysql_db_name";
	public static final String PROP_MYSQL_DUMPFILE = "mysql_dumpfile";

	private final String tomcatStartCommand;
	private final int tomcatStartDelay;
	private final String tomcatStopCommand;
	private final int tomcatStopDelay;
	private final String mysqlUsername;
	private final String mysqlPassword;
	private final String mysqlDbName;
	private final File mysqlDumpfile;

	/**
	 * Confirm that we have the expected properties, and that their values seem
	 * reasonable.
	 */
	public ModelCleanerProperties(Properties props) {
		this.tomcatStartCommand = getRequiredProperty(props,
				PROP_TOMCAT_START_COMMAND);
		this.tomcatStartDelay = getRequiredIntegerProperty(props,
				PROP_TOMCAT_START_DELAY);

		this.tomcatStopCommand = getRequiredProperty(props,
				PROP_TOMCAT_STOP_COMMAND);
		this.tomcatStopDelay = getRequiredIntegerProperty(props,
				PROP_TOMCAT_STOP_DELAY);

		this.mysqlUsername = getRequiredProperty(props, PROP_MYSQL_USERNAME);
		this.mysqlPassword = getRequiredProperty(props, PROP_MYSQL_PASSWORD);
		this.mysqlDbName = getRequiredProperty(props, PROP_MYSQL_DB_NAME);

		this.mysqlDumpfile = confirmDumpfile(props);
	}

	public String getTomcatStartCommand() {
		return tomcatStartCommand;
	}

	public int getTomcatStartDelay() {
		return tomcatStartDelay;
	}

	public String getTomcatStopCommand() {
		return tomcatStopCommand;
	}

	public int getTomcatStopDelay() {
		return tomcatStopDelay;
	}

	public String getMysqlUsername() {
		return mysqlUsername;
	}

	public String getMysqlPassword() {
		return mysqlPassword;
	}

	public String getMysqlDbName() {
		return mysqlDbName;
	}

	public File getMysqlDumpfile() {
		return mysqlDumpfile;
	}

	/**
	 * Get the value for this property. If there isn't one, or if it's empty,
	 * complain.
	 */
	private String getRequiredProperty(Properties props, String key) {
		String value = props.getProperty(key);
		if ((value == null) || (value.trim().length() == 0)) {
			throw new IllegalArgumentException(
					"Property file must provide a value for '" + key + "'");
		}
		return value;
	}

	private int getRequiredIntegerProperty(Properties props, String key) {
		String value = getRequiredProperty(props, key);
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Property value for '" + key
					+ "' is not a valid integer: " + value);
		}
	}

	/**
	 * The dumpfile parameter must point to an existing file.
	 */
	private File confirmDumpfile(Properties props) {
		String filename = getRequiredProperty(props, PROP_MYSQL_DUMPFILE);
		File dumpfile = new File(filename);
		if (!dumpfile.exists()) {
			throw new IllegalArgumentException("Invalid value for '"
					+ PROP_MYSQL_DUMPFILE + "': file '" + filename
					+ "' does not exist.");
		}
		if (!dumpfile.isFile()) {
			throw new IllegalArgumentException("Invalid value for '"
					+ PROP_MYSQL_DUMPFILE + "': '" + filename
					+ "' is not a file.");
		}
		if (!dumpfile.canRead()) {
			throw new IllegalArgumentException("Invalid value for '"
					+ PROP_MYSQL_DUMPFILE + "': file '" + filename
					+ "' is not readable.");
		}
		return dumpfile;
	}

	public String toString() {
		return "\n      tomcatStartCommand: " + tomcatStartCommand
				+ "\n      tomcatStartDelay: " + tomcatStartDelay
				+ "\n      tomcatStopCommand: " + tomcatStopCommand
				+ "\n      tomcatStopDelay: " + tomcatStopDelay;
	}
}
