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
	private static final String PROP_OUTPUT_DIRECTORY = "output_directory";
	private static final String PROP_UPLOAD_DIRECTORY = "upload_directory";
	private static final String PROP_SUITE_DIRECTORIES = "suite_parent_directories";

	private static final String LOGFILE_NAME = "log_file.txt";

	private final File uploadDirectory;
	private final File outputDirectory;
	private final File logFile;

	private final Collection<File> suiteParentDirectories;

	private Collection<File> selectedSuites = Collections.emptySet();
	private boolean cleanModel = true;
	private boolean cleanUploads = true;
	private Logger logger = new Logger(System.out);

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

			this.uploadDirectory = checkReadWriteDirectory(props,
					PROP_UPLOAD_DIRECTORY);
			this.outputDirectory = checkReadWriteDirectory(props,
					PROP_OUTPUT_DIRECTORY);
			this.logFile = new File(this.outputDirectory, LOGFILE_NAME);
			this.logger = new Logger(this.logFile);

			this.suiteParentDirectories = checkSuiteParentDirectories(props);

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

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public File getUploadDirectory() {
		return uploadDirectory;
	}

	public Collection<File> getSuiteParentDirectories() {
		return suiteParentDirectories;
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

	/**
	 * Look inside this parent directory and find any suite directories. You can
	 * recognize a suite directory because it contains a file named Suite.html.
	 */
	public Collection<File> findSuiteDirs(File parentDir) {
		return Arrays.asList(parentDir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (!pathname.isDirectory()) {
					return false;
				}
				File suiteFile = new File(pathname, "Suite.html");
				if (suiteFile.exists()) {
					return true;
				} else {
					logger.subProcessErrout("Warning: suite file '" + suiteFile.getPath()
							+ "' does not exist.\n");
					return false;
				}
			}
		}));
	}

}
