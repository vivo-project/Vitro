/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.io.File;
import java.util.Properties;

/**
 * Hold the runtime properties that pertain specifically to cleaning the data
 * model.
 */
public class ModelCleanerProperties {
	public static final String PROP_VIVO_WEBAPP_NAME = "vivo_webapp_name";
	public static final String PROP_TOMCAT_MANAGER_USERNAME = "tomcat_manager_username";
	public static final String PROP_TOMCAT_MANAGER_PASSWORD = "tomcat_manager_password";
	public static final String PROP_MYSQL_USERNAME = "mysql_username";
	public static final String PROP_MYSQL_PASSWORD = "mysql_password";
	public static final String PROP_MYSQL_DB_NAME = "mysql_db_name";
	public static final String PROP_WEBAPP_DIRECTORY = "vivo_webapp_directory";

	private final String vivoWebappName;
	private final String tomcatManagerUsername;
	private final String tomcatManagerPassword;
	private final String mysqlUsername;
	private final String mysqlPassword;
	private final String mysqlDbName;
	private final File webappDirectory;

	/**
	 * Confirm that we have the expected properties, and that their values seem
	 * reasonable.
	 */
	public ModelCleanerProperties(Properties props) {
		this.vivoWebappName = checkWebappName(props);
		this.tomcatManagerUsername = getRequiredProperty(props,
				PROP_TOMCAT_MANAGER_USERNAME);
		this.tomcatManagerPassword = getRequiredProperty(props,
				PROP_TOMCAT_MANAGER_PASSWORD);

		this.mysqlUsername = getRequiredProperty(props, PROP_MYSQL_USERNAME);
		this.mysqlPassword = getRequiredProperty(props, PROP_MYSQL_PASSWORD);
		this.mysqlDbName = getRequiredProperty(props, PROP_MYSQL_DB_NAME);

		this.webappDirectory = confirmWebappDirectory(props);
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

	public String getVivoWebappName() {
		return vivoWebappName;
	}

	public String getTomcatManagerUsername() {
		return tomcatManagerUsername;
	}

	public String getTomcatManagerPassword() {
		return tomcatManagerPassword;
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

	/**
	 * The website URL must end with the webapp name.
	 */
	private String checkWebappName(Properties props) {
		String websiteUrl = getRequiredProperty(props,
				SeleniumRunnerParameters.PROP_WEBSITE_URL);
		String webappName = getRequiredProperty(props, PROP_VIVO_WEBAPP_NAME);
		if (!websiteUrl.endsWith(webappName)) {
			throw new IllegalArgumentException("The " + PROP_VIVO_WEBAPP_NAME
					+ " must be the last item in the "
					+ SeleniumRunnerParameters.PROP_WEBSITE_URL);
		}
		return webappName;
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
		return "\n      vivoWebappName: " + vivoWebappName
				+ "\n      tomcatManagerUsername: " + tomcatManagerUsername
				+ "\n      tomcatManagerPassword: " + tomcatManagerPassword
				+ "\n      mysqlUsername: " + mysqlUsername
				+ "\n      mysqlPassword: " + mysqlPassword
				+ "\n      mysqlDbName: " + mysqlDbName
				+ "\n      webappDirectory: " + webappDirectory;
	}

}
