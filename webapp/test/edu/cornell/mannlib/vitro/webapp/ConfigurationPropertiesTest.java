/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
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

	/**
	 * The resource property files that we might configure.
	 */
	private static final String CONFIGURED_PROPERTY_FILE = "edu/cornell/mannlib/vitro/webapp/test_config.properties";
	private static final String INVALID_PROPERTY_FILE = "edu/cornell/mannlib/vitro/webapp/test_config_invalid.properties";
	private static final String DEFAULT_PROPERTY_FILE = "edu/cornell/mannlib/vitro/webapp/test_config_default.properties";

	/**
	 * The mappings that we might find from the property files.
	 */
	private static final String[][] MAP_VALUES_DEFAULT = new String[][] { {
			"whichfile", "test_config_default" }, {"trimmed", "whitespace_test"} };
	private static final String[][] MAP_VALUES_CONFIGURED = new String[][] { {
			"whichfile", "test_config" } };

	/**
	 * A context to hold the JNDI mappings.
	 */
	private InitialContext initial;

	/**
	 * The original default value for the configuration properties path.
	 */
	private static Object originalPath;

	@BeforeClass
	public static void alterTheDefaultPath() throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		Class<ConfigurationProperties> clazz = ConfigurationProperties.class;
		Field field = clazz.getDeclaredField("DEFAULT_CONFIG_PATH");
		field.setAccessible(true);
		originalPath = field.get(null);
		field.set(null, DEFAULT_PROPERTY_FILE);
	}

	@AfterClass
	public static void restoreTheDefaultPath() throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		Class<ConfigurationProperties> clazz = ConfigurationProperties.class;
		Field field = clazz.getDeclaredField("DEFAULT_CONFIG_PATH");
		field.setAccessible(true);
		field.set(null, originalPath);
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

	@Test
	public void topLevelContextIsMissing() {
		setLoggerLevel(ConfigurationProperties.class, Level.ERROR);
		ConfigurationProperties.getMap();
		assertExpectedMap(MAP_VALUES_DEFAULT);
	}

	@Test
	public void noEnvironmentMapping() throws NamingException {
		setLoggerLevel(ConfigurationProperties.class, Level.ERROR);
		// We map something in the same JNDI environment,
		// but not the mapping we will be looking for.
		initial.bind(NOT_THE_DESIRED_MAPPING, "doesn't matter");
		ConfigurationProperties.getMap();
		assertExpectedMap(MAP_VALUES_DEFAULT);
	}

	@Test(expected = IllegalArgumentException.class)
	public void fileNotFound() throws NamingException {
		initial.bind(CONFIGURATION_PROPERTIES_MAPPING, "noSuchResource");
		ConfigurationProperties.getMap();
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidFileFormat() throws NamingException {
		initial.bind(CONFIGURATION_PROPERTIES_MAPPING, INVALID_PROPERTY_FILE);
		ConfigurationProperties.getMap();
	}

	@Test
	public void readFromConfiguredResource() throws NamingException {
		initial.bind(CONFIGURATION_PROPERTIES_MAPPING, CONFIGURED_PROPERTY_FILE);
		assertExpectedMap(MAP_VALUES_CONFIGURED);
	}

	@Test
	public void checkOtherMethods() throws NamingException {
		initial.bind(CONFIGURATION_PROPERTIES_MAPPING, CONFIGURED_PROPERTY_FILE);
		assertEquals("test_config",
				ConfigurationProperties.getProperty("whichfile"));
		assertEquals(null, ConfigurationProperties.getProperty("notThere"));
		assertEquals("default",
				ConfigurationProperties.getProperty("notThere", "default"));
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
		assertEquals("properties map", expected,
				ConfigurationProperties.getMap());
	}

}
