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
	public static final String PROP_WEBAPP_DIRECTORY = "vivo_webapp_directory";

	private final String tomcatStartCommand;
	private final int tomcatStartDelay;
	private final String tomcatStopCommand;
	private final int tomcatStopDelay;
	private final String mysqlUsername;
	private final String mysqlPassword;
	private final String mysqlDbName;
	private final File webappDirectory;

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

		this.webappDirectory = confirmWebappDirectory(props);
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

	public File getWebappDirectory() {
		return webappDirectory;
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
	 * The dumpfile parameter must point to an existing directory.
	 */
	private File confirmWebappDirectory(Properties props) {
		String filename = getRequiredProperty(props, PROP_WEBAPP_DIRECTORY);
		File webappDirectory = new File(filename);
		if (!webappDirectory.exists()) {
			throw new IllegalArgumentException("Invalid value for '"
					+ PROP_WEBAPP_DIRECTORY + "': directory '" + filename
					+ "' does not exist.");
		}
		if (!webappDirectory.isDirectory()) {
			throw new IllegalArgumentException("Invalid value for '"
					+ PROP_WEBAPP_DIRECTORY + "': '" + filename
					+ "' is not a directory.");
		}
		if (!webappDirectory.canWrite()) {
			throw new IllegalArgumentException("Invalid value for '"
					+ PROP_WEBAPP_DIRECTORY + "': directory '" + filename
					+ "' is not writeable.");
		}
		return webappDirectory;
	}

	public String toString() {
		return "\n      tomcatStartCommand: " + tomcatStartCommand
				+ "\n      tomcatStartDelay: " + tomcatStartDelay
				+ "\n      tomcatStopCommand: " + tomcatStopCommand
				+ "\n      tomcatStopDelay: " + tomcatStopDelay
				+ "\n      mysqlUsername: " + mysqlUsername
				+ "\n      mysqlPassword: " + mysqlPassword
				+ "\n      mysqlDbName: " + mysqlDbName
				+ "\n      webappDirectory: " + webappDirectory;
	}

}
