/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import stubs.javax.naming.InitialContextStub;
import stubs.javax.naming.spi.InitialContextFactoryStub;
import stubs.javax.servlet.ServletContextStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;

/**
 * Test the methods of {@link FileStorageSetup}
 */
public class FileStorageSetupTest extends AbstractTestClass {
	// ----------------------------------------------------------------------
	// framework
	// ----------------------------------------------------------------------

	private static final String configProperties = "#mock config properties file\n";
	private static File tempDir;

	private FileStorageSetup fss;
	private ServletContextEvent sce;
	private ServletContext sc;

	/**
	 * Use a mock {@link InitialContext} to create an empty
	 * {@link ConfigurationProperties} object. Each test can use
	 * {@link #setConfigurationProperties(String, String)} to populate it as
	 * they choose.
	 */
	@BeforeClass
	public static void createConfigurationProperties() throws Exception {
		tempDir = createTempDirectory("FileStorageFactoryTest");

		File propsFile = createFile(tempDir, "config.properties",
				configProperties);

		System.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
				InitialContextFactoryStub.class.getName());
		InitialContextStub.reset();
		new InitialContext().bind("java:comp/env/path.configuration",
				propsFile.getPath());
	}

	@Before
	public void createFileStorageSetup() {
		fss = new FileStorageSetup();
		sc = new ServletContextStub();
		sce = new ServletContextEvent(sc);
	}

	@AfterClass
	public static void cleanup() {
		purgeDirectoryRecursively(tempDir);
	}

	// ----------------------------------------------------------------------
	// tests
	// ----------------------------------------------------------------------

	@Test
	public void baseDirectoryNotSpecified() {
		setConfigurationProperties(null, "http://vivo.myDomain.edu/individual/");
		fss.contextInitialized(sce);
		assertNull("no base directory",
				sc.getAttribute(FileStorageSetup.ATTRIBUTE_NAME));
	}

	@Test
	public void baseDirectoryDoesntExist() throws IOException {
		setConfigurationProperties("/bogus/Directory",
				"http://vivo.myDomain.edu/individual/");
		fss.contextInitialized(sce);
		assertNull("no such directory",
				sc.getAttribute(FileStorageSetup.ATTRIBUTE_NAME));
	}

	@Test
	public void defaultNamespaceNotSpecified() {
		setConfigurationProperties(tempDir.getPath(), null);
		fss.contextInitialized(sce);
		assertNull("no default namespace",
				sc.getAttribute(FileStorageSetup.ATTRIBUTE_NAME));
	}

	@Test
	public void defaultNamespaceIsBogus() throws IOException {
		setConfigurationProperties(tempDir.getPath(), "namespace");
		fss.contextInitialized(sce);
		assertNull("default namespace is bogus",
				sc.getAttribute(FileStorageSetup.ATTRIBUTE_NAME));
	}

	@Test
	public void success() throws IOException {
		setConfigurationProperties(tempDir.getPath(),
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
		Map<String, String> map = new HashMap<String, String>();
		if (baseDir != null) {
			map.put(FileStorageSetup.PROPERTY_FILE_STORAGE_BASE_DIR, baseDir);
		}
		if (defaultNamespace != null) {
			map.put(FileStorageSetup.PROPERTY_DEFAULT_NAMESPACE,
					defaultNamespace);
		}

		try {
			Field f = ConfigurationProperties.class.getDeclaredField("theMap");
			f.setAccessible(true);
			f.set(null, map);
		} catch (Exception e) {
			fail("Exception while setting config properties: " + e);
		}
	}

}
