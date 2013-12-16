/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Test that the JVM is properly configured.
 * 
 * For now, we just test the temp directory. Other
 */
public class JvmSmokeTests implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		checkJvmLevel(ss);
		checkTempDirectory(ss);
	}

	/**
	 * We need to run at 1.7 or later.
	 */
	private void checkJvmLevel(StartupStatus ss) {
		String specLevel = System.getProperty("java.specification.version", "");
		if (specLevel.isEmpty()) {
			ss.warning(this, "Can't determine the current level of Java. "
					+ "VIVO requires at least Java 1.7.");
		} else if (specLevel.compareTo("1.7") < 0) {
			ss.warning(this, "VIVO requires at least Java 1.7 - "
					+ "currently running on Java " + specLevel);
		} else {
			ss.info(this, "Java version is " + specLevel);
		}
	}

	/**
	 * Check the Java temp directory. Make sure that it exists, it is a
	 * directory, we can read it, we can write to it.
	 * 
	 * Create a temp file, and delete it.
	 */
	private void checkTempDirectory(StartupStatus ss) {
		String tempDirPath = System.getProperty("java.io.tmpdir", "");
		if (tempDirPath.isEmpty()) {
			ss.fatal(this, "Problem with Java temp directory: "
					+ "System property 'java.io.tmpdir' is not set.");
			return;
		}
		File tempDir = new File(tempDirPath);
		if (!tempDir.exists()) {
			ss.fatal(this, "Problem with Java temp directory: '" + tempDirPath
					+ "' does not exist.");
			return;
		}
		if (!tempDir.isDirectory()) {
			ss.fatal(this, "Problem with Java temp directory: '" + tempDirPath
					+ "' exists, but is not a directory.");
			return;
		}
		if (!tempDir.canRead()) {
			ss.fatal(this, "Problem with Java temp directory: "
					+ "No read access to '" + tempDirPath + "'.");
			return;
		}
		if (!tempDir.canWrite()) {
			ss.fatal(this, "Problem with Java temp directory: "
					+ "No write access to '" + tempDirPath + "'.");
			return;
		}
		try {
			File testFile = File.createTempFile("smoke", null);
			testFile.delete();
		} catch (Exception e) {
			ss.fatal(this, "Problem with Java temp directory: "
					+ "Failed to create a temporary file in '" + tempDirPath
					+ "'.", e);
			return;
		}
		ss.info(this, "Java temp directory is '" + tempDirPath + "'");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do at shutdown
	}

}
