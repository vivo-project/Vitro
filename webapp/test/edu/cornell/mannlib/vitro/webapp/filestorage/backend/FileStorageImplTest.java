/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * Test the FileStorage methods. The zero-argument constructor was tested in
 * {@link FileStorageFactoryTest}.
 */
public class FileStorageImplTest extends AbstractTestClass {
	private static final List<String> EMPTY_NAMESPACES = Collections
			.emptyList();

	private static File tempDir;
	private static FileStorageImpl generalFs;

	@BeforeClass
	public static void createSomeDirectories() throws IOException {
		tempDir = createTempDirectory(FileStorageImplTest.class.getSimpleName());
		generalFs = createFileStorage("general");
	}

	@AfterClass
	public static void cleanUp() {
		if (tempDir != null) {
			purgeDirectoryRecursively(tempDir);
		}
	}

	// ----------------------------------------------------------------------
	// tests
	// ----------------------------------------------------------------------

	@Test(expected = IllegalArgumentException.class)
	public void baseDirDoesntExist() throws IOException {
		File baseDir = new File(tempDir, "doesntExist");
		new FileStorageImpl(baseDir, EMPTY_NAMESPACES);
	}

	@Test(expected = IllegalStateException.class)
	public void partialInitializationRoot() throws IOException {
		File baseDir = new File(tempDir, "partialWithRoot");
		baseDir.mkdir();
		new File(baseDir, FileStorage.FILE_STORAGE_ROOT).mkdir();

		new FileStorageImpl(baseDir, EMPTY_NAMESPACES);
	}

	@Test(expected = IllegalStateException.class)
	public void partialInitializationNamespaces() throws IOException {
		File baseDir = new File(tempDir, "partialWithNamespaces");
		baseDir.mkdir();
		new File(baseDir, FileStorage.FILE_STORAGE_NAMESPACES_PROPERTIES)
				.createNewFile();

		new FileStorageImpl(baseDir, EMPTY_NAMESPACES);
	}

	@Test
	public void notInitializedNoNamespaces() throws IOException {
		File baseDir = new File(tempDir, "emptyNoNamespaces");
		baseDir.mkdir();

		FileStorageImpl fs = new FileStorageImpl(baseDir,
				new ArrayList<String>());
		assertEquals("baseDir", baseDir, fs.getBaseDir());
		assertEqualSets("namespaces", new String[0], fs.getNamespaces()
				.values());
	}

	@Test
	public void notInitializedNamespaces() throws IOException {
		String[] namespaces = new String[] { "ns1", "ns2" };
		String dirName = "emptyWithNamespaces";

		FileStorageImpl fs = createFileStorage(dirName, namespaces);

		assertEquals("baseDir", new File(tempDir, dirName), fs.getBaseDir());
		assertEqualSets("namespaces", namespaces, fs.getNamespaces().values());
	}

	@Test
	public void initializedOK() throws IOException {
		createFileStorage("initializeTwiceTheSame", "ns1", "ns2");
		createFileStorage("initializeTwiceTheSame", "ns2", "ns1");
	}

	@Test
	public void namespaceDisappears() throws IOException {
		createFileStorage("namespaceDisappears", "ns1", "ns2");
		FileStorageImpl fs = createFileStorage("namespaceDisappears", "ns2");
		assertEqualSets("namespaces", new String[] { "ns1", "ns2" }, fs
				.getNamespaces().values());
	}

	@Test
	public void namespaceChanged() throws IOException {
		setLoggerLevel(FileStorageImpl.class, Level.ERROR);
		createFileStorage("namespaceChanges", "ns1", "ns2");
		FileStorageImpl fs = createFileStorage("namespaceChanges", "ns3", "ns1");
		assertEqualSets("namespaces", new String[] { "ns1", "ns2", "ns3" }, fs
				.getNamespaces().values());
	}

	@Test
	public void createFileOriginal() throws IOException {
		String id = "createOriginal";
		String filename = "someName.txt";
		String contents = "these contents";
		InputStream bytes = new ByteArrayInputStream(contents.getBytes());

		generalFs.createFile(id, filename, bytes);

		assertFileContents(generalFs, id, filename, contents);
	}

	@Test
	public void createFileOverwrite() throws IOException {
		String id = "createOverwrite";
		String filename = "someName.txt";

		String contents1 = "these contents";
		InputStream bytes1 = new ByteArrayInputStream(contents1.getBytes());

		String contents2 = "a different string";
		InputStream bytes2 = new ByteArrayInputStream(contents2.getBytes());

		generalFs.createFile(id, filename, bytes1);
		generalFs.createFile(id, filename, bytes2);

		assertFileContents(generalFs, id, filename, contents2);
	}

	@Test
	public void createFileConflictingName() throws IOException {
		String id = "createConflict";
		String filename1 = "someName.txt";
		String filename2 = "secondFileName.txt";
		String contents = "these contents";
		InputStream bytes = new ByteArrayInputStream(contents.getBytes());

		generalFs.createFile(id, filename1, bytes);
		try {
			generalFs.createFile(id, filename2, bytes);
			fail("Expected FileAlreadyExistsException.");
		} catch (FileAlreadyExistsException e) {
			// expected it.
		}
	}

	@Test
	public void getFilenameExists() throws IOException {
		String id = "filenameExists";
		String filename = "theName.txt";
		String contents = "the contents";
		InputStream bytes = new ByteArrayInputStream(contents.getBytes());

		generalFs.createFile(id, filename, bytes);

		assertEquals("filename", filename, generalFs.getFilename(id));
	}

	@Test
	public void getFilenameDoesntExist() throws IOException {
		assertNull("null filename", generalFs.getFilename("neverHeardOfIt"));
	}

	@Test
	public void getInputStreamFound() throws IOException {
		String id = "inputStreamExists";
		String filename = "myFile";
		String contents = "Some stuff to put into my file.";
		InputStream bytes = new ByteArrayInputStream(contents.getBytes());

		generalFs.createFile(id, filename, bytes);

		assertFileContents(generalFs, id, filename, contents);
		assertEquals("getInputStream", contents,
				readAll(generalFs.getInputStream(id, filename)));
	}

	@Test(expected = FileNotFoundException.class)
	public void getInputStreamNotFound() throws IOException {
		generalFs.getInputStream("notFound", "nothing");
	}

	@Test
	public void deleteFileExists() throws IOException {
		String id = "deleteMe";
		String filename = "deadFile";
		String contents = "Some stuff to put into my file.";
		InputStream bytes = new ByteArrayInputStream(contents.getBytes());

		generalFs.createFile(id, filename, bytes);
		generalFs.deleteFile(id);
		assertNull("deleted filename", generalFs.getFilename(id));
	}

	@Test
	public void deleteFileDoesntExist() throws IOException {
		generalFs.deleteFile("totallyBogus");
	}

	@Test
	public void exerciseWindowsExclusions() throws FileAlreadyExistsException,
			IOException {
		// setLoggerLevel(FileStorageHelper.class, Level.DEBUG);
		String id = "nul";
		String filename = "COM1";
		String contents = "Windows doesn't like certain names.";
		InputStream bytes = new ByteArrayInputStream(contents.getBytes());

		generalFs.createFile(id, filename, bytes);

		assertFileContents(generalFs, id, filename, contents);
		assertEquals("filename", filename, generalFs.getFilename(id));
		assertEquals("getInputStream", contents,
				readAll(generalFs.getInputStream(id, filename)));
	}

	// ----------------------------------------------------------------------
	// helper methods
	// ----------------------------------------------------------------------

	private static FileStorageImpl createFileStorage(String dirName,
			String... namespaces) throws IOException {
		File baseDir = new File(tempDir, dirName);
		baseDir.mkdir();
		return new FileStorageImpl(baseDir, Arrays.asList(namespaces));
	}

	private <T> void assertEqualSets(String message, T[] expected,
			Collection<T> actual) {
		Set<T> expectedSet = new HashSet<T>(Arrays.asList(expected));
		if (expectedSet.size() != expected.length) {
			fail("message: expected array contains duplicate elements: "
					+ Arrays.deepToString(expected));
		}

		Set<T> actualSet = new HashSet<T>(actual);
		if (actualSet.size() != actual.size()) {
			fail("message: actual collection contains duplicate elements: "
					+ actual);
		}

		assertEquals(message, expectedSet, actualSet);
	}

	/**
	 * This file storage should contain a file with this ID and this name, and
	 * it should have these contents.
	 */
	private void assertFileContents(FileStorageImpl fs, String id,
			String filename, String expectedContents) throws IOException {
		File rootDir = new File(fs.getBaseDir(), FileStorage.FILE_STORAGE_ROOT);
		File path = FileStorageHelper.getFullPath(rootDir, id, filename,
				fs.getNamespaces());

		assertTrue("file exists: " + path, path.exists());

		String actualContents = readFile(path);

		assertEquals("file contents", expectedContents, actualContents);
	}
}
