/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.testing;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.Before;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A collection of useful routines to help when testing.
 * <ul>
 * <li>Permit tests to control the Logging levels of individual classes.</li>
 * <li>Permit tests to control system properties.</li>
 * <li>Suppress, capture or test standard output and/or error output.</li>
 * <li>Create and delete temporary files and directories.</li>
 * <li>Create URLs from Strings without throwing checked exceptions.</li>
 * <li>Compare the contents of XML documents.</li>
 * </ul>
 * 
 * @author jeb228
 */
public abstract class AbstractTestClass {

	// ----------------------------------------------------------------------
	// Control the level of logging output.
	// ----------------------------------------------------------------------

	/** The layout we use for logging. */
	private static final PatternLayout patternLayout = new PatternLayout(
			"%p %d{yyyy-MM-dd' 'HH:mm:ss.SSS} [%t] (%c{1}) %m%n");

	/**
	 * Unless modified, all Logging will be done to the console at
	 * {@link Level#INFO}.
	 */
	@Before
	@After
	public void initializeLogging() {
		LogManager.resetConfiguration();
		Logger.getRootLogger().addAppender(new ConsoleAppender(patternLayout));
		Logger.getRootLogger().setLevel(Level.INFO);
	}

	/**
	 * Call this in a "@Before" or "@BeforeClass" method to change the logging
	 * level of a particular class.
	 */
	protected static void setLoggerLevel(Class<?> clazz, Level level) {
		Logger.getLogger(clazz).setLevel(level);
	}

	/**
	 * Same thing, but for a logger that is not named directly after a class.
	 */
	protected static void setLoggerLevel(String category, Level level) {
		Logger.getLogger(category).setLevel(level);
	}

	// ----------------------------------------------------------------------
	// Control standard output or error output.
	// ----------------------------------------------------------------------

	private static final PrintStream originalSysout = System.out;
	private static final PrintStream originalSyserr = System.err;
	private final ByteArrayOutputStream capturedSysout = new ByteArrayOutputStream();
	private final ByteArrayOutputStream capturedSyserr = new ByteArrayOutputStream();

	@Before
	@After
	public void restoreOutputStreams() {
		System.setOut(originalSysout);
		System.setErr(originalSyserr);
		capturedSysout.reset();
		capturedSyserr.reset();
	}

	protected void suppressSysout() {
		System.setOut(new PrintStream(capturedSysout, true));
	}

	protected void suppressSyserr() {
		System.setErr(new PrintStream(capturedSyserr, true));
	}

	protected String getSysoutForTest() {
		return capturedSysout.toString();
	}

	protected String getSyserrForTest() {
		return capturedSyserr.toString();
	}

	// ----------------------------------------------------------------------
	// Set values on System properties for individual tests.
	// ----------------------------------------------------------------------

	private static Properties originalSystemProperties = (Properties) System
			.getProperties().clone();

	@Before
	@After
	public void restoreSystemProperties() {
		System.setProperties((Properties) originalSystemProperties.clone());
	}

	// ----------------------------------------------------------------------
	// Manage temporary files.
	// ----------------------------------------------------------------------

	/**
	 * Delete a file, either before or after the test. If it can't be deleted,
	 * complain.
	 */
	protected static void deleteFile(File file) {
		if (file.exists()) {
			file.delete();
		}
		if (!file.exists()) {
			return;
		}

		/*
		 * If we were unable to delete the file, is it because it's a non-empty
		 * directory?
		 */
		if (!file.isDirectory()) {
			final StringBuffer message = new StringBuffer(
					"Unable to delete directory '" + file.getPath() + "'\n");
			file.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					message.append("   contains file '" + pathname + "'\n");
					return true;
				}
			});
			fail(message.toString().trim());
		} else {
			fail("Unable to delete file '" + file.getPath() + "'");
		}
	}

	/**
	 * Delete all of the files in a directory, and the directory itself. Will
	 * not work if there are sub-directories.
	 */
	protected static void purgeDirectory(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					fail("Directory '" + directory
							+ "' contains at least one nested directory.");
				}
			}
			for (File file : files) {
				deleteFile(file);
			}
			deleteFile(directory);
		}
	}

	/**
	 * Delete all of the files in a directory, any sub-directories, and the
	 * directory itself.
	 */
	protected static void purgeDirectoryRecursively(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					purgeDirectoryRecursively(file);
				} else {
					deleteFile(file);
				}
			}
			deleteFile(directory);
		}
	}

	/**
	 * Create a directory of a given name inside the Temp directory. If such a
	 * directory already exists, purge it and its contents and create it fresh.
	 */
	protected static File createTempDirectory(String name) throws IOException {
		File tempDirectory = new File(System.getProperty("java.io.tmpdir"),
				name);

		// If it already exists, remove it, so we start clean.
		if (tempDirectory.exists()) {
			purgeDirectoryRecursively(tempDirectory);
		}

		if (!tempDirectory.mkdir()) {
			throw new IOException("failed to create temp directory '"
					+ tempDirectory.getPath() + "'");
		}

		return tempDirectory;
	}

	// ----------------------------------------------------------------------
	// Other utilities.
	// ----------------------------------------------------------------------

	/**
	 * Create a file and fill it with the contents provided.
	 */
	protected static File createFile(File directory, String filename,
			String contents) throws IOException {
		Writer writer = null;
		try {
			File file = new File(directory, filename);
			if (file.exists()) {
				throw new IOException("File '" + file.getPath()
						+ "' already exists.");
			}
			file.createNewFile();
			writer = new FileWriter(file);
			writer.write(contents);
			return file;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Read the entire contents of a file, or throw an exception if it's not
	 * there.
	 */
	protected static String readFile(File file) throws IOException {
		if (!file.exists()) {
			throw new IOException("file '" + file.getPath() + "' ('"
					+ file.getAbsolutePath() + "') does not exist.");
		}
		FileReader fileReader = new FileReader(file);
		String result = readAll(fileReader);
		return result;
	}

	/**
	 * Suck all the data from a {@link Reader} into a {@link String}.
	 */
	protected static String readAll(Reader reader) throws IOException {
		StringBuilder result = new StringBuilder();
		BufferedReader buffered = new BufferedReader(reader);
		char[] chunk = new char[4096];
		int howMany;

		try {
			while (-1 != (howMany = buffered.read(chunk))) {
				result.append(chunk, 0, howMany);
			}
		} finally {
			reader.close();
		}

		return result.toString();
	}

	/**
	 * Suck all the data from a {@link InputStream} into a {@link String}.
	 */
	protected static String readAll(InputStream stream) throws IOException {
		return readAll(new InputStreamReader(stream));
	}

	/**
	 * Convert a string to a URL without a checked exception.
	 */
	protected static URL url(String string) {
		try {
			return new URL(string);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Read each string into an XML document and then write it again. This
	 * should discard any differences that are not syntactically significant.
	 * Will they be identical?
	 */
	protected void assertEquivalentXmlDocs(String string1, String string2) {
		String out1 = launderXmlDocument(string1);
		String out2 = launderXmlDocument(string2);
		if (!out1.equals(out2)) {
			fail("XML documents are not equivalent: expected <" + string1
					+ "> but was <" + string2 + ">");
		}
	}

	/**
	 * Read a string of XML into a document and write it again. This should
	 * result in a canonical form that can be compared to other such strings.
	 */
	private String launderXmlDocument(String docString) {
		StringWriter result = new StringWriter();
		try {
			DocumentBuilderFactory bFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = bFactory.newDocumentBuilder();

			TransformerFactory xFactory = TransformerFactory.newInstance();
			Transformer xformer = xFactory.newTransformer();

			StringReader reader = new StringReader(docString);
			Document doc = builder.parse(new InputSource(reader));
			xformer.transform(new DOMSource(doc), new StreamResult(result));
		} catch (ParserConfigurationException e) {
			fail(e.toString());
		} catch (SAXException e) {
			fail(e.toString());
		} catch (IOException e) {
			fail(e.toString());
		} catch (TransformerException e) {
			fail(e.toString());
		}
		return result.toString().replaceAll(">\\s+<", "><");
	}

	protected <T extends Comparable<T>> void assertEqualSets(String label,
			Set<T> expected, Set<T> actual) {
		if (expected.equals(actual)) {
			return;
		}

		Set<T> missing = new TreeSet<T>(expected);
		missing.removeAll(actual);
		Set<T> extras = new TreeSet<T>(actual);
		extras.removeAll(expected);

		String message = label;
		if (!missing.isEmpty()) {
			message += ", missing: " + missing;
		}
		if (!extras.isEmpty()) {
			message += ", extra: " + extras;
		}
		assertEquals(message, expected, actual);
	}

	protected <T> Set<T> buildSet(T... array) {
		return new HashSet<T>(Arrays.asList(array));
	}

}
