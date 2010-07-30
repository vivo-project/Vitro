/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * Test the methods of {@link FlatteningTemplateLoader}.
 */
public class FlatteningTemplateLoaderTest extends AbstractTestClass {
	/**
	 * TODO test plan
	 * 
	 * <pre>
	 * findTemplateSource
	 *   null arg
	 *   not found
	 *   found in top level
	 *   found in lower level
	 *   with path
	 *   
	 * getReader
	 *   get it, read it, check it, close it.
	 *   
	 * getLastModified
	 * 	 check the create date within a range
	 *   modify it and check again.
	 * 
	 * </pre>
	 */
	// ----------------------------------------------------------------------
	// setup and teardown
	// ----------------------------------------------------------------------

	private static final String SUBDIRECTORY_NAME = "sub";

	private static final String TEMPLATE_NAME_UPPER = "template.ftl";
	private static final String TEMPLATE_NAME_UPPER_WITH_PATH = "path/template.ftl";
	private static final String TEMPLATE_UPPER_CONTENTS = "The contents of the file.";

	private static final String TEMPLATE_NAME_LOWER = "another.ftl";
	private static final String TEMPLATE_LOWER_CONTENTS = "Another template file.";

	private static long setupTime;
	private static File tempDir;
	private static File notADirectory;
	private static File upperTemplate;
	private static File lowerTemplate;

	private FlatteningTemplateLoader loader;

	@BeforeClass
	public static void setUpFiles() throws IOException {
		setupTime = System.currentTimeMillis();

		notADirectory = File.createTempFile(
				FlatteningTemplateLoader.class.getSimpleName(), "");

		tempDir = createTempDirectory(FlatteningTemplateLoader.class
				.getSimpleName());
		upperTemplate = createFile(tempDir, TEMPLATE_NAME_UPPER,
				TEMPLATE_UPPER_CONTENTS);

		File subdirectory = new File(tempDir, SUBDIRECTORY_NAME);
		subdirectory.mkdir();
		lowerTemplate = createFile(subdirectory, TEMPLATE_NAME_LOWER,
				TEMPLATE_LOWER_CONTENTS);
	}

	@Before
	public void initializeLoader() {
		loader = new FlatteningTemplateLoader(tempDir);
	}

	@AfterClass
	public static void cleanUpFiles() throws IOException {
		purgeDirectoryRecursively(tempDir);
	}

	// ----------------------------------------------------------------------
	// the tests
	// ----------------------------------------------------------------------

	@Test(expected = NullPointerException.class)
	public void constructorNull() {
		new FlatteningTemplateLoader(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorNonExistent() {
		new FlatteningTemplateLoader(new File("bogusDirName"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorNotADirectory() {
		new FlatteningTemplateLoader(notADirectory);
	}

	@Test
	public void findNull() throws IOException {
		Object source = loader.findTemplateSource(null);
		assertNull("find null", source);
	}

	@Test
	public void findNotFound() throws IOException {
		Object source = loader.findTemplateSource("bogus");
		assertNull("not found", source);
	}

	@Test
	public void findInTopLevel() throws IOException {
		Object source = loader.findTemplateSource(TEMPLATE_NAME_UPPER);
		assertEquals("top level", upperTemplate, source);
	}

	@Test
	public void findInLowerLevel() throws IOException {
		Object source = loader.findTemplateSource(TEMPLATE_NAME_LOWER);
		assertEquals("lower level", lowerTemplate, source);
	}

	@Test
	public void findIgnoringPath() throws IOException {
		Object source = loader
				.findTemplateSource(TEMPLATE_NAME_UPPER_WITH_PATH);
		assertEquals("top level", upperTemplate, source);
	}

	@Test
	public void checkTheReader() throws IOException {
		Object source = loader.findTemplateSource(TEMPLATE_NAME_UPPER);
		Reader reader = loader.getReader(source, "UTF-8");
		String contents = readAll(reader);
		assertEquals("read the contents", contents, TEMPLATE_UPPER_CONTENTS);
	}

	/**
	 * We may not know exactly when the file was last modified, but it should
	 * fall into a known range.
	 */
	@Test
	public void lastModified() throws IOException {
		Object source = loader.findTemplateSource(TEMPLATE_NAME_UPPER);
		long modified = loader.getLastModified(source);
		long firstBoundary = System.currentTimeMillis();
		assertInRange("created", setupTime, firstBoundary, modified);

		rewriteFile(upperTemplate, TEMPLATE_UPPER_CONTENTS);
		long secondBoundary = System.currentTimeMillis();
		modified = loader.getLastModified(source);
		assertInRange("modified", firstBoundary, secondBoundary, modified);
	}

	@Test
	public void closeDoesntCrash() throws IOException {
		Object source = loader.findTemplateSource(TEMPLATE_NAME_UPPER);
		loader.closeTemplateSource(source);
	}

	// ----------------------------------------------------------------------
	// helper methods
	// ----------------------------------------------------------------------

	/**
	 * Fill an existing file with new contents.
	 */
	private void rewriteFile(File file, String contents) throws IOException {
		Writer writer = null;
		try {
			writer = new FileWriter(file);
			writer.write(contents);
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
	 * Assert that the modified time falls between (or on) the two boundary
	 * times.
	 */
	private void assertInRange(String message, long lowerBound,
			long upperBound, long modified) {
		if (modified < lowerBound) {
			fail(message + ": " + formatTimeStamp(modified)
					+ " is less than the lower bound "
					+ formatTimeStamp(lowerBound));
		}
		if (modified > upperBound) {
			fail(message + ": " + formatTimeStamp(modified)
					+ " is greater than the upper bound "
					+ formatTimeStamp(upperBound));
		}
	}

	private String formatTimeStamp(long time) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.SSS");
		return formatter.format(time);
	}

}
