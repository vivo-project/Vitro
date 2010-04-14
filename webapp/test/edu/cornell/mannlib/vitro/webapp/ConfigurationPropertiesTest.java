/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import stubs.javax.naming.InitialContextStub;
import stubs.javax.naming.spi.InitialContextFactoryStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * 
 * @author jeb228
 */
public class ConfigurationPropertiesTest extends AbstractTestClass {

	/**
	 * The JNDI Mapping for the configuration properties path.
	 */
	private static final String CONFIGURATION_PROPERTIES_MAPPING = ConfigurationProperties.JNDI_BASE
			+ "/" + ConfigurationProperties.PATH_CONFIGURATION;

	private static final String NOT_THE_DESIRED_MAPPING = ConfigurationProperties.JNDI_BASE
			+ "/notTheDesiredMapping";

	private static File tempDir;
	private static File testFile;
	private static File invalidFile;

	private InitialContext initial;

	/**
	 * Create a good test file and a bad test file.
	 * 
	 * (a class-path-based resource should already exist.)
	 */
	@BeforeClass
	public static void createTestFiles() throws IOException {
		tempDir = createTempDirectory(ConfigurationPropertiesTest.class
				.getSimpleName());
		testFile = createFile(tempDir, "testFile", "source = file\n");
		invalidFile = createFile(tempDir, "invalidFile",
				"source = bad Unicode constant \\uu1045");
	}

	/**
	 * Clean up.
	 */
	@AfterClass
	public static void removeTestFiles() {
		purgeDirectoryRecursively(tempDir);
	}

	/**
	 * I don't want to see the INFO messages while running the tests.
	 */
	@Before
	public void setLogging() {
		setLoggerLevel(ConfigurationProperties.class, Level.WARN);
	}

	/**
	 * Use the context stub, and be sure that it is clean for each test.
	 */
	@Before
	public void initializeContextStubs() throws NamingException {
		System.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
				InitialContextFactoryStub.class.getName());
		InitialContextStub.reset();
		initial = new InitialContext();
	}

	/**
	 * {@link ConfigurationProperties} is a singleton, so we need to clean it
	 * before each test.
	 */
	@Before
	public void resetProperties() {
		ConfigurationProperties.reset();
	}

	// ----------------------------------------------------------------------
	// the tests
	// ----------------------------------------------------------------------

	@Test(expected = IllegalStateException.class)
	public void topLevelContextIsMissing() {
		ConfigurationProperties.getMap();
	}

	@Test(expected = IllegalStateException.class)
	public void noEnvironmentMapping() throws NamingException {
		// We map something in the same JNDI environment,
		// but not the mapping we will be looking for.
		initial.bind(NOT_THE_DESIRED_MAPPING, "doesn't matter");
		ConfigurationProperties.getMap();
	}

	@Test(expected = IllegalArgumentException.class)
	public void fileNotFound() throws NamingException {
		initial.bind(CONFIGURATION_PROPERTIES_MAPPING, "noSuchFileOrResource");
		ConfigurationProperties.getMap();
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidFileFormat() throws NamingException {
		initial.bind(CONFIGURATION_PROPERTIES_MAPPING, invalidFile.getPath());
		ConfigurationProperties.getMap();
	}

	@Test
	public void readFromResource() throws NamingException {
		initial.bind(CONFIGURATION_PROPERTIES_MAPPING,
				"edu/cornell/mannlib/vitro/webapp/test_config.properties");
		assertExpectedMap(new String[][] { { "source", "resource" } });
	}

	@Test
	public void readFromFile() throws NamingException {
		initial.bind(CONFIGURATION_PROPERTIES_MAPPING, testFile.getPath());
		assertExpectedMap(new String[][] { { "source", "file" } });
	}

	@Test
	public void checkOtherMethods() throws NamingException {
		initial.bind(CONFIGURATION_PROPERTIES_MAPPING, testFile.getPath());
		assertEquals("file", ConfigurationProperties.getProperty("source"));
		assertEquals(null, ConfigurationProperties.getProperty("notThere"));
		assertEquals("default", ConfigurationProperties.getProperty("notThere",
				"default"));
	}

	// ----------------------------------------------------------------------
	// helper methods
	// ----------------------------------------------------------------------

	/**
	 * Does the configuration properties map look like this group of key/value
	 * pairs?
	 */
	private void assertExpectedMap(String[][] strings) {
		Map<String, String> expected = new HashMap<String, String>();
		for (String[] pair : strings) {
			expected.put(pair[0], pair[1]);
		}
		assertEquals("properties map", expected, ConfigurationProperties
				.getMap());
	}

}
