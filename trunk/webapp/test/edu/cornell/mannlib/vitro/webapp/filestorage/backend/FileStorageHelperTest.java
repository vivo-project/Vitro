/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.backend;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * 
 */
public class FileStorageHelperTest {
	private static String RAW_NAME_1 = "simpleName";
	private static String ENCODED_NAME_1 = "simpleName";
	private static String RAW_NAME_2 = "common:/Chars.pdf";
	private static String ENCODED_NAME_2 = "common+=Chars.pdf";
	private static String RAW_NAME_3 = "rare\"+~chars";
	private static String ENCODED_NAME_3 = "rare^22^2b^7echars";
	private static String RAW_NAME_4 = "combination+<of^:both";
	private static String ENCODED_NAME_4 = "combination^2b^3cof^5e+both";
	private static String RAW_NAME_5 = " invisibles\u0001\u007f";
	private static String ENCODED_NAME_5 = "^20invisibles^01^7f";
	private static String RAW_NAME_6 = "out of range\u0101";

	private static String ID_1 = "simpleName";
	private static String RELATIVE_PATH_1 = "sim/ple/Nam/e";
	private static String ID_2 = "combination+<of^:both";
	private static String RELATIVE_PATH_2 = "com/bin/ati/on^/2b^/3co/f^5/e+b/oth";
	private static String ID_3 = "http://vivo.myDomain.edu/file/n3234";
	private static String RELATIVE_PATH_3 = "htt/p+=/=vi/vo,/myD/oma/in,/edu/=fi/le=/n32/34";
	private static String RELATIVE_PREFIXED_PATH_3 = "b~n/323/4";

	private static File ROOT_DIR_1 = new File("/root");
	private static File ABSOLUTE_PATH_1 = new File("/root/sim/ple/Nam/e");
	private static File ROOT_DIR_2 = new File("/this/that/slash/");
	private static File ABSOLUTE_PATH_2 = new File(
			"/this/that/slash/sim/ple/Nam/e");

	private static String FULL_NAME = "myPhoto.jpg";
	private static String FULL_ID = "http://vivo.myDomain.edu/file/n3234.XXX";
	private static File FULL_ROOT = new File(
			"/usr/local/vivo/uploads/file_storage_root");
	private static File FULL_RESULT_PATH = new File(
			"/usr/local/vivo/uploads/file_storage_root/b~n/323/4,X/XX/myPhoto.jpg");

	private static Map<Character, String> WINDOWS_PREFIX_MAP = initWindowsPrefixMap();
	/** This reserved word will be modified. */
	private static String WINDOWS_NAME = "lpT8";
	/** This ID would translate to a path with a reserved word. */
	private static String WINDOWS_ID = "prefix:createdConflict";
	/** Not allowed to change the root, even if it contains reserved words. */
	private static File WINDOWS_ROOT = new File("/usr/aux/root/");
	private static File WINDOWS_FULL_PATH = new File(
			"/usr/aux/root/a~c/rea/ted/~Con/fli/ct/~lpT8");

	private static Map<Character, String> EMPTY_NAMESPACES = Collections
			.emptyMap();
	private static Map<Character, String> NAMESPACES = initPrefixMap();

	private static Map<Character, String> initPrefixMap() {
		Map<Character, String> map = new HashMap<Character, String>();
		map.put('a', "junk");
		map.put('b', "http://vivo.myDomain.edu/file/");
		return map;
	}

	private static Map<Character, String> initWindowsPrefixMap() {
		Map<Character, String> map = new HashMap<Character, String>();
		map.put('a', "prefix:");
		return map;
	}

	// ----------------------------------------------------------------------
	// encodeName
	// ----------------------------------------------------------------------

	@Test
	public void encodeName1() {
		assertNameEncoding(RAW_NAME_1, ENCODED_NAME_1);
	}

	@Test
	public void encodeName2() {
		assertNameEncoding(RAW_NAME_2, ENCODED_NAME_2);
	}

	@Test
	public void encodeName3() {
		assertNameEncoding(RAW_NAME_3, ENCODED_NAME_3);
	}

	@Test
	public void encodeName4() {
		assertNameEncoding(RAW_NAME_4, ENCODED_NAME_4);
	}

	@Test
	public void encodeName5() {
		assertNameEncoding(RAW_NAME_5, ENCODED_NAME_5);
	}

	@Test(expected = InvalidCharacterException.class)
	public void encodeName6() {
		FileStorageHelper.encodeName(RAW_NAME_6);
	}

	private void assertNameEncoding(String rawName, String expected) {
		String encoded = FileStorageHelper.encodeName(rawName);
		assertEquals("encoded name", expected, encoded);
	}

	// ----------------------------------------------------------------------
	// decodeName
	// ----------------------------------------------------------------------

	@Test
	public void decodeName1() {
		assertNameDecoding(ENCODED_NAME_1, RAW_NAME_1);
	}

	@Test
	public void decodeName2() {
		assertNameDecoding(ENCODED_NAME_2, RAW_NAME_2);
	}

	@Test
	public void decodeName3() {
		assertNameDecoding(ENCODED_NAME_3, RAW_NAME_3);
	}

	@Test
	public void decodeName4() {
		assertNameDecoding(ENCODED_NAME_4, RAW_NAME_4);
	}

	@Test
	public void decodeName5() {
		assertNameDecoding(ENCODED_NAME_5, RAW_NAME_5);
	}

	private void assertNameDecoding(String encodedName, String expected) {
		String decoded = FileStorageHelper.decodeName(encodedName);
		assertEquals("decodedName", expected, decoded);
	}

	// ----------------------------------------------------------------------
	// idToPath
	// ----------------------------------------------------------------------

	@Test
	public void idToPath1() {
		assertIdToPath(ID_1, EMPTY_NAMESPACES, RELATIVE_PATH_1);
	}

	@Test
	public void idToPath2() {
		assertIdToPath(ID_2, EMPTY_NAMESPACES, RELATIVE_PATH_2);
	}

	@Test
	public void idToPath3() {
		assertIdToPath(ID_3, EMPTY_NAMESPACES, RELATIVE_PATH_3);
	}

	@Test
	public void idToPath3WithNamespace() {
		assertIdToPath(ID_3, NAMESPACES, RELATIVE_PREFIXED_PATH_3);
	}

	private void assertIdToPath(String id, Map<Character, String> namespaces,
			String expected) {
		String adjustedExpected = expected.replace('/', File.separatorChar);
		String relativePath = FileStorageHelper.id2Path(id, namespaces);
		assertEquals("idToPath", adjustedExpected, relativePath);
	}

	// ----------------------------------------------------------------------
	// getPathToIdDirectory
	// ----------------------------------------------------------------------

	@Test
	public void getPathToIdDirectory1() {
		assertPathToIdDirectory(ID_1, EMPTY_NAMESPACES, ROOT_DIR_1,
				ABSOLUTE_PATH_1);
	}

	@Test
	public void getPathToIdDirectory2() {
		assertPathToIdDirectory(ID_1, EMPTY_NAMESPACES, ROOT_DIR_2,
				ABSOLUTE_PATH_2);
	}

	private void assertPathToIdDirectory(String id,
			Map<Character, String> namespaces, File rootDir, File expected) {
		File actual = FileStorageHelper.getPathToIdDirectory(id, namespaces,
				rootDir);
		File adjustedExpected = new File(expected.getPath().replace('/',
				File.separatorChar));
		assertEquals("pathToIdDirectory", adjustedExpected, actual);
	}

	// ----------------------------------------------------------------------
	// getFullPath
	// ----------------------------------------------------------------------

	@Test
	public void getFullPath() {
		File actual = FileStorageHelper.getFullPath(FULL_ROOT, FULL_ID,
				FULL_NAME, NAMESPACES);
		assertEquals("fullPath", FULL_RESULT_PATH, actual);
	}

	@Test
	public void checkWindowsExclusions() {
		File actual = FileStorageHelper.getFullPath(WINDOWS_ROOT, WINDOWS_ID,
				WINDOWS_NAME, WINDOWS_PREFIX_MAP);
		assertEquals("windows exclusion", WINDOWS_FULL_PATH, actual);
	}

}
