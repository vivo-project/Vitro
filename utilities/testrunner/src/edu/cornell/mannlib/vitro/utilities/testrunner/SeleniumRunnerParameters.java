/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Holds the runtime parameters that are read from the properties file, perhaps
 * with modifications from the GUI if we are running interactively.
 */
public class SeleniumRunnerParameters {
	public static final String PROP_OUTPUT_DIRECTORY = "output_directory";
	public static final String PROP_UPLOAD_DIRECTORY = "upload_directory";
	public static final String PROP_SUITE_DIRECTORIES = "suite_parent_directories";
	public static final String PROP_WEBSITE_URL = "website_url";
	public static final String PROP_USER_EXTENSIONS_PATH = "user_extensions_path";
	public static final String PROP_FIREFOX_PROFILE_PATH = "firefox_profile_template_path";
	public static final String PROP_SUITE_TIMEOUT_LIMIT = "suite_timeout_limit";
	public static final String PROP_SELENIUM_JAR_PATH = "selenium_jar_path";

	public static final String LOGFILE_NAME = "log_file.txt";

	private final String websiteUrl;
	private final File userExtensionsFile;
	private final File firefoxProfileDir;
	private final int suiteTimeoutLimit;
	private final File seleniumJarPath;
	private final File uploadDirectory;
	private final File outputDirectory;
	private final File logFile;
	private final Collection<File> suiteParentDirectories;
	private final ModelCleanerProperties modelCleanerProperties;

	private Collection<File> selectedSuites = Collections.emptySet();
	private boolean cleanModel = true;
	private boolean cleanUploads = true;
	private Listener listener = new Listener(System.out);

	/**
	 * Read the required properties from the property file, and do some checks
	 * on them.
	 */
	public SeleniumRunnerParameters(String propertiesFilepath)
			throws IOException {
		Reader propsReader = null;
		try {
			propsReader = new FileReader(new File(propertiesFilepath));
			Properties props = new Properties();
			props.load(propsReader);

			this.websiteUrl = getRequiredProperty(props, PROP_WEBSITE_URL);
			this.userExtensionsFile = checkReadableFile(props,
					PROP_USER_EXTENSIONS_PATH);
			this.firefoxProfileDir = checkOptionalReadableDirectory(props,
					PROP_FIREFOX_PROFILE_PATH);
			this.suiteTimeoutLimit = getRequiredIntegerProperty(props,
					PROP_SUITE_TIMEOUT_LIMIT);
			this.seleniumJarPath = checkReadableFile(props,
					PROP_SELENIUM_JAR_PATH);
			this.uploadDirectory = checkReadWriteDirectory(props,
					PROP_UPLOAD_DIRECTORY);

			this.outputDirectory = checkReadWriteDirectory(props,
					PROP_OUTPUT_DIRECTORY);
			this.logFile = new File(this.outputDirectory, LOGFILE_NAME);
			this.listener = new Listener(this.logFile);

			this.suiteParentDirectories = checkSuiteParentDirectories(props);

			this.modelCleanerProperties = new ModelCleanerProperties(props);
		} finally {
			if (propsReader != null) {
				try {
					propsReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * If there is a parameter for this key, it should point to a readable
	 * directory.
	 */
	private File checkOptionalReadableDirectory(Properties props, String key) {
		String value = props.getProperty(key);
		if (value == null) {
			return null;
		}

		File dir = new File(value);

		if (!dir.exists()) {
			throw new IllegalArgumentException("Directory " + key + " '"
					+ value + "' does not exist.");
		}

		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Directory " + key + " '"
					+ value + "' is not a directory.");
		}

		if (!dir.canRead()) {
			throw new IllegalArgumentException("Directory " + key + " '"
					+ value + "' is not readable.");
		}

		return dir;
	}

	/**
	 * Check that there is a property for the output directory, and that it
	 * points to a valid directory.
	 */
	private File checkReadWriteDirectory(Properties props, String key) {
		String value = getRequiredProperty(props, key);

		File dir = new File(value);

		if (!dir.exists()) {
			throw new IllegalArgumentException("Directory " + key + " '"
					+ value + "' does not exist.");
		}

		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Directory " + key + " '"
					+ value + "' is not a directory.");
		}

		if (!dir.canRead()) {
			throw new IllegalArgumentException("Directory " + key + " '"
					+ value + "' is not readable.");
		}

		if (!dir.canWrite()) {
			throw new IllegalArgumentException("Directory " + key + " '"
					+ value + "' is not writeable.");
		}
		return dir;
	}

	private File checkReadableFile(Properties props, String key) {
		String value = getRequiredProperty(props, key);

		File file = new File(value);

		if (!file.exists()) {
			throw new IllegalArgumentException("File " + key
					+ ": '' does not exist.");
		}
		if (!file.isFile()) {
			throw new IllegalArgumentException("File " + key
					+ ": '' is not a file.");
		}
		if (!file.canRead()) {
			throw new IllegalArgumentException("File " + key
					+ ": '' is not readable.");
		}
		return file;
	}

	/**
	 * Get the property for the suite directories and ensure that each one is
	 * indeed a readable directory.
	 */
	private Collection<File> checkSuiteParentDirectories(Properties props) {
		String value = getRequiredProperty(props, PROP_SUITE_DIRECTORIES);

		List<File> dirs = new ArrayList<File>();
		String[] paths = value.split("[:;]");
		for (String path : paths) {
			File dir = new File(path.trim());

			if (!dir.exists()) {
				throw new IllegalArgumentException("Suite directory '"
						+ dir.getPath() + "' does not exist.");
			}
			if (!dir.isDirectory()) {
				throw new IllegalArgumentException("Suite directory '"
						+ dir.getPath() + "' is not a directory.");
			}
			if (!dir.canRead()) {
				throw new IllegalArgumentException("Suite directory '"
						+ dir.getPath() + "' is not readable.");
			}
			dirs.add(dir);
		}
		return dirs;
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
	 * This required property must be a valid integer.
	 */
	private int getRequiredIntegerProperty(Properties props, String key) {
		String value = getRequiredProperty(props, key);
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Property value for '" + key
					+ "' is not a valid integer: " + value);
		}
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public File getUserExtensionsFile() {
		return userExtensionsFile;
	}

	public boolean hasFirefoxProfileDir() {
		return firefoxProfileDir != null;
	}

	public File getFirefoxProfileDir() {
		return firefoxProfileDir;
	}

	public int getSuiteTimeoutLimit() {
		return suiteTimeoutLimit;
	}

	public File getSeleniumJarPath() {
		return seleniumJarPath;
	}

	public Listener getListener() {
		return listener;
	}

	public void setListener(Listener logger) {
		this.listener = logger;
	}

	public File getUploadDirectory() {
		return uploadDirectory;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public Collection<File> getSuiteParentDirectories() {
		return suiteParentDirectories;
	}

	public ModelCleanerProperties getModelCleanerProperties() {
		return modelCleanerProperties;
	}

	public void setSelectedSuites(Collection<File> selectedSuites) {
		this.selectedSuites = selectedSuites;
	}

	public Collection<File> getSelectedSuites() {
		return new ArrayList<File>(this.selectedSuites);
	}

	public boolean isCleanModel() {
		return cleanModel;
	}

	public void setCleanModel(boolean cleanModel) {
		this.cleanModel = cleanModel;
	}

	public boolean isCleanUploads() {
		return cleanUploads;
	}

	public void setCleanUploads(boolean cleanUploads) {
		this.cleanUploads = cleanUploads;
	}

	public String toString() {
		return "Parameters:" + "\n  websiteUrl: " + websiteUrl
				+ "\n  userExtensionsFile: " + userExtensionsFile.getPath()
				+ "\n  firefoxProfileDir: " + firefoxProfileDir.getPath()
				+ "\n  suiteTimeoutLimit: " + suiteTimeoutLimit
				+ "\n  seleniumJarPath: " + seleniumJarPath.getPath()
				+ "\n  uploadDirectory: " + uploadDirectory.getPath()
				+ "\n  outputDirectory: " + outputDirectory.getPath()
				+ "\n  suiteParentDirectories: " + suiteParentDirectories
				+ "\n  modelCleanerProperties: " + modelCleanerProperties
				+ "\n\n  selectedSuites: " + showSelectedSuites()
				+ "\n  cleanModel: " + cleanModel + "\n  cleanUploads: "
				+ cleanUploads;
	}

	private String showSelectedSuites() {
		StringBuilder buffer = new StringBuilder();
		for (File suite : selectedSuites) {
			buffer.append("\n      ").append(suite.getPath());
		}
		return buffer.toString();
	}

	/**
	 * Look inside this parent directory and find any suite directories. You can
	 * recognize a suite directory because it contains a file named Suite.html.
	 */
	public Collection<File> findSuiteDirs(File parentDir) {
		System.out.println("parentDir: " + parentDir);
		return Arrays.asList(parentDir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (!pathname.isDirectory()) {
					return false;
				}
				if (pathname.getName().charAt(0) == '.') {
					return false;
				}

				File suiteFile = new File(pathname, "Suite.html");
				if (suiteFile.exists()) {
					return true;
				} else {
					listener.subProcessErrout("Warning: suite file '"
							+ suiteFile.getPath() + "' does not exist.\n");
					return false;
				}
			}
		}));
	}

}
