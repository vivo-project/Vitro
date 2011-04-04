/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.config.ConfigurationPropertiesStub;
import stubs.javax.servlet.ServletContextStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;

/**
 * Test the methods of {@link FileStorageSetup}
 */
public class FileStorageSetupTest extends AbstractTestClass {
	// ----------------------------------------------------------------------
	// framework
	// ----------------------------------------------------------------------

	private static File tempDir;
	private static File vivoHomeDir;
	private static File fsBaseDir;

	private FileStorageSetup fss;
	private ServletContextEvent sce;
	private ServletContext sc;

	@Before
	public void createFileStorageSetup() {
		fss = new FileStorageSetup();
	}

	@Before
	public void createContext() {
		sc = new ServletContextStub();
		sce = new ServletContextEvent(sc);
	}

	@Before
	public void createBaseDirectory() throws IOException {
		tempDir = createTempDirectory("FileStorageFactoryTest");
		vivoHomeDir = new File(tempDir, "fsBaseDirectory");
		vivoHomeDir.mkdir();
		fsBaseDir = new File(vivoHomeDir, FileStorageSetup.FILE_STORAGE_SUBDIRECTORY);
		fsBaseDir.mkdir();
	}

	@After
	public void cleanupBaseDirectory() {
		purgeDirectoryRecursively(tempDir);
	}

	// ----------------------------------------------------------------------
	// tests
	// ----------------------------------------------------------------------

	@Test
	public void baseDirectoryNotSpecified() {
		setLoggerLevel(FileStorageSetup.class, Level.OFF);
		setConfigurationProperties(null, "http://vivo.myDomain.edu/individual/");
		fss.contextInitialized(sce);
		assertNull("no base directory",
				sc.getAttribute(FileStorageSetup.ATTRIBUTE_NAME));
	}

	@Test
	public void baseDirectoryDoesntExist() {
		setLoggerLevel(FileStorageSetup.class, Level.OFF);
		setConfigurationProperties("/bogus/Directory",
				"http://vivo.myDomain.edu/individual/");
		fss.contextInitialized(sce);
		assertNull("no such directory",
				sc.getAttribute(FileStorageSetup.ATTRIBUTE_NAME));
	}

	@Test
	public void defaultNamespaceNotSpecified() {
		setLoggerLevel(FileStorageSetup.class, Level.OFF);
		setConfigurationProperties(vivoHomeDir.getPath(), null);
		fss.contextInitialized(sce);
		assertNull("no default namespace",
				sc.getAttribute(FileStorageSetup.ATTRIBUTE_NAME));
	}

	// This no longer throws an exception - it should be a success.
	@Test
	public void defaultNamespaceIsBogus() {
		setLoggerLevel(FileStorageSetup.class, Level.ERROR);
		setConfigurationProperties(vivoHomeDir.getPath(), "namespace");
		fss.contextInitialized(sce);

		Object o = sc.getAttribute(FileStorageSetup.ATTRIBUTE_NAME);
		FileStorage fs = (FileStorage) o;

		assertEquals("implementation class", FileStorageImpl.class,
				fs.getClass());
	}

	@Test
	public void success() {
		setConfigurationProperties(vivoHomeDir.getPath(),
				"http://vivo.myDomain.edu/individual/");
		fss.contextInitialized(sce);

		Object o = sc.getAttribute(FileStorageSetup.ATTRIBUTE_NAME);
		FileStorage fs = (FileStorage) o;

		assertEquals("implementation class", FileStorageImpl.class,
				fs.getClass());
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void setConfigurationProperties(String baseDir,
			String defaultNamespace) {
		ConfigurationPropertiesStub props = new ConfigurationPropertiesStub();

		if (baseDir != null) {
			props.setProperty(FileStorageSetup.PROPERTY_VITRO_HOME_DIR,
					baseDir);
		}
		if (defaultNamespace != null) {
			props.setProperty(FileStorageSetup.PROPERTY_DEFAULT_NAMESPACE,
					defaultNamespace);
		}

		setLoggerLevel(ConfigurationProperties.class, Level.WARN);
		props.setBean(sc);
	}

}
