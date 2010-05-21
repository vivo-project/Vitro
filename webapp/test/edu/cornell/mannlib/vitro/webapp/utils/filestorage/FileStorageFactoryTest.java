/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.filestorage;

import static edu.cornell.mannlib.vitro.webapp.utils.filestorage.FileStorageFactory.PROPERTY_IMPLEMETATION_CLASSNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import stubs.javax.naming.InitialContextStub;
import stubs.javax.naming.spi.InitialContextFactoryStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;

/**
 * TODO
 */
public class FileStorageFactoryTest extends AbstractTestClass {
	private static final String configProperties = "#mock config properties file\n";
	private static File tempDir;
	private static File propsFile;

	@Before
	public void setup() throws Exception {
		tempDir = createTempDirectory("FileStorageFactoryTest");

		propsFile = createFile(tempDir, "config.properties", configProperties);

		System.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
				InitialContextFactoryStub.class.getName());
		InitialContextStub.reset();
		new InitialContext().bind("java:comp/env/path.configuration", propsFile
				.getPath());
	}

	@Before
	public void initializeConfigurationProperties() throws NamingException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
	}

	@After
	public void cleanup() {
		purgeDirectoryRecursively(tempDir);
	}

	@Test
	public void createDefaultImplementation() throws IOException {
		setConfigurationProperties(tempDir.getPath(),
				"http://vivo.myDomain.edu/individual/");
		FileStorage fs = FileStorageFactory.getFileStorage();
		assertEquals("implementation class", FileStorageImpl.class, fs
				.getClass());
	}

	@Test
	public void createAlternateImplementation() throws IOException {
		System.setProperty(PROPERTY_IMPLEMETATION_CLASSNAME,
				FileStorageStub.class.getName());
		FileStorage fs = FileStorageFactory.getFileStorage();
		assertEquals("implementation class", FileStorageStub.class, fs
				.getClass());
	}

	@Test(expected = IllegalArgumentException.class)
	public void baseDirectoryDoesntExist() throws IOException {
		setConfigurationProperties("/bogus/Directory",
				"http://vivo.myDomain.edu/individual/");
		FileStorageFactory.getFileStorage();
	}

	@Test(expected = IllegalArgumentException.class)
	public void defaultNamespaceIsBogus() throws IOException {
		setConfigurationProperties(tempDir.getPath(), "namespace");
		FileStorageFactory.getFileStorage();
	}

	@Test(expected = IllegalArgumentException.class)
	public void noSuchClass() throws IOException {
		System.setProperty(PROPERTY_IMPLEMETATION_CLASSNAME, "bogus.Classname");
		FileStorageFactory.getFileStorage();
	}

	@Test(expected = IllegalArgumentException.class)
	public void doesntImplement() throws IOException {
		System.setProperty(PROPERTY_IMPLEMETATION_CLASSNAME,
				NotFileStorage.class.getName());
		FileStorageFactory.getFileStorage();
	}

	@Test(expected = IllegalArgumentException.class)
	public void noZeroArgsConstructor() throws IOException {
		System.setProperty(PROPERTY_IMPLEMETATION_CLASSNAME,
				NoConstructor.class.getName());
		FileStorageFactory.getFileStorage();
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void setConfigurationProperties(String baseDir,
			String defaultNamespace) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("upload.directory", baseDir);
		map.put("Vitro.defaultNamespace", defaultNamespace);

		try {
			Field f = ConfigurationProperties.class.getDeclaredField("theMap");
			f.setAccessible(true);
			f.set(null, map);
		} catch (Exception e) {
			fail("Exception while setting config properties: " + e);
		}
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	/** An alternative implementation of FileStorage. */
	public static class FileStorageStub implements FileStorage {
		public void createFile(String id, String filename, InputStream bytes)
				throws FileAlreadyExistsException, IOException {
		}

		public boolean deleteFile(String id) throws IOException {
			return true;
		}

		public String getFilename(String id) throws IOException {
			return "filename";
		}

		public byte[] getfile(String id, String filename)
				throws FileNotFoundException, IOException {
			return new byte[0];
		}
	}

	/** This class has no zero-argument constructor. */
	public static class NoConstructor implements FileStorage {
		@SuppressWarnings("unused")
		public NoConstructor(String string) {
		}

		public void createFile(String id, String filename, InputStream bytes)
				throws FileAlreadyExistsException, IOException {
		}

		public boolean deleteFile(String id) throws IOException {
			return true;
		}

		public String getFilename(String id) throws IOException {
			return "filename";
		}

		public byte[] getfile(String id, String filename)
				throws FileNotFoundException, IOException {
			return new byte[0];
		}
	}

	/** This class does not implement the FileStorage interface. */
	public static class NotFileStorage {
	}
}
