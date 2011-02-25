/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.config.ConfigurationPropertiesStub;
import stubs.javax.servlet.ServletContextStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorageSetup;

/**
 */
public class FileServingHelperTest extends AbstractTestClass {
	private static final String DEFAULT_NAMESPACE = "http://some.crazy.domain/individual/";

	// ----------------------------------------------------------------------
	// framework
	// ----------------------------------------------------------------------

	private ServletContextStub ctx;

	/**
	 * Set the desired default namespace into the ConfigurationProperties.
	 */
	@Before
	public void createConfigurationProperties() throws Exception {
		setLoggerLevel(ConfigurationProperties.class, Level.WARN);
		
		ctx = new ServletContextStub();

		ConfigurationPropertiesStub props = new ConfigurationPropertiesStub();
		props.setProperty(FileStorageSetup.PROPERTY_DEFAULT_NAMESPACE,
				DEFAULT_NAMESPACE);
		props.setBean(ctx);
	}

	// ----------------------------------------------------------------------
	// tests
	// ----------------------------------------------------------------------

	@Test
	public void nullUri() {
		assertCorrectUrl(null, "somefilename.ext", null);
	}

	@Test
	public void nullFilename() {
		assertCorrectUrl("http://some.crazy.domain/individual/n4324", null,
				null);
	}

	@Test
	public void notInDefaultNamespace() {
		setLoggerLevel(FileServingHelper.class, Level.ERROR);
		assertCorrectUrl("notInTheNamespace", "somefilename.ext",
				"notInTheNamespace");
	}

	@Test
	public void inDefaultNamespaceNoTrailingSlash() {
		assertCorrectUrl("http://some.crazy.domain/individual/n4324",
				"somefilename.ext", "/file/n4324/somefilename.ext");
	}

	@Test
	public void inDefaultNamespaceTrailingSlash() {
		assertCorrectUrl("http://some.crazy.domain/individual/n4324/",
				"somefilename.ext", "/file/n4324/somefilename.ext");
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void assertCorrectUrl(String uri, String filename, String expected) {
		String actual = FileServingHelper.getBytestreamAliasUrl(uri, filename,
				ctx);
		assertEquals("url", expected, actual);
	}

}
