/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.backend;

import static edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage.SHORTY_LENGTH;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A collection of utility routines used by the file storage system. Routines
 * exist to:
 * <ul>
 * <li>encode filenames for safe storage</li>
 * <li>decode filenames to their original values</li>
 * <li>convert an ID (with namespaces) to a path, relative to the root directory
 * </li>
 * <li>convert an ID (with namespaces) to an absolute path</li>
 * <li>convert an ID (with namespaces) and a filename to a full path for storing
 * the file</li>
 * </ul>
 */
public class FileStorageHelper {
	private static final Log log = LogFactory.getLog(FileStorageHelper.class);

	public static final char HEX_ESCAPE_CHAR = '^';

	public static final String HEX_ENCODE_SOURCES = "\"*+,<=>?^|\\~";

	public static final char[] PATH_SINGLE_CHARACTER_SOURCES = new char[] {
			'/', ':', '.' };
	public static final char[] PATH_SINGLE_CHARACTER_TARGETS = new char[] {
			'=', '+', ',' };

	/** Same as for path, except that a period is not translated. */
	public static final char[] NAME_SINGLE_CHARACTER_SOURCES = new char[] {
			'/', ':' };
	/** Same as for path, except that a period is not translated. */
	public static final char[] NAME_SINGLE_CHARACTER_TARGETS = new char[] {
			'=', '+' };

	/**
	 * Windows reserves these names (case-insensitive), so they can't be used
	 * for directories or files.
	 */
	public static final String[] WINDOWS_RESERVED_NAMES = new String[] { "CON",
			"PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5",
			"COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4",
			"LPT5", "LPT6", "LPT7", "LPT8", "LPT9" };

	/**
	 * Encode the filename as needed to guard against illegal characters.
	 * 
	 * @see edu.cornell.mannlib.vitro.webapp.utils.filestorage
	 */
	public static String encodeName(String filename) {
		String hexed = addHexEncoding(filename);
		String cleaned = addSingleCharacterConversions(hexed,
				NAME_SINGLE_CHARACTER_SOURCES, NAME_SINGLE_CHARACTER_TARGETS);
		return excludeWindowsReservedNames(cleaned);
	}

	/**
	 * Encode special characters to hex sequences.
	 */
	private static String addHexEncoding(String clear) {
		for (int i = 0; i < clear.length(); i++) {
			char c = clear.charAt(i);
			if (c > 255) {
				throw new InvalidCharacterException(c, i, clear);
			}
		}

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < clear.length(); i++) {
			result.append(hexEncodeCharacter(clear.charAt(i)));
		}

		log.debug("Add hex encodings to '" + clear + "' giving '" + result
				+ "'");
		return result.toString();
	}

	/**
	 * Create a string holding either the character or its hex-encoding.
	 */
	private static String hexEncodeCharacter(char c) {
		if ((c < 0x21) || (c > 0x7e) || (HEX_ENCODE_SOURCES.indexOf(c) >= 0)) {
			return new StringBuilder().append(HEX_ESCAPE_CHAR).append(
					toHexDigit(c / 16)).append(toHexDigit(c % 16)).toString();

		} else {
			return Character.toString(c);
		}
	}

	/**
	 * Return the correct hex character for this integer value.
	 */
	private static char toHexDigit(int i) {
		return "0123456789abcdef".charAt(i);
	}

	/**
	 * Perform common single-character substitutions.
	 */
	private static String addSingleCharacterConversions(String encoded,
			char[] sources, char[] targets) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < encoded.length(); i++) {
			char c = encoded.charAt(i);
			result.append(translateSingleCharacter(c, sources, targets));
		}
		log.debug("Add single character conversions to '" + encoded
				+ "' giving '" + result + "'");
		return result.toString();
	}

	/**
	 * If a character found in the "from" set, return its corresponding
	 * character from the "to" set. Otherwise, return the character itself.
	 */
	private static char translateSingleCharacter(char c, char[] from, char[] to) {
		for (int j = 0; j < from.length; j++) {
			if (c == from[j]) {
				return to[j];
			}
		}
		return c;
	}

	/**
	 * If a requested filename, after cleaning, is one of the Windows reserved
	 * words, add a tilde in front.
	 */
	private static String excludeWindowsReservedNames(String cleanedName) {
		for (String word : WINDOWS_RESERVED_NAMES) {
			if (word.equalsIgnoreCase(cleanedName)) {
				return '~' + cleanedName;
			}
		}
		return cleanedName;
	}

	/**
	 * Restore the filename to its original form, removing the encoding.
	 * 
	 * @see edu.cornell.mannlib.vitro.webapp.utils.filestorage
	 */
	public static String decodeName(String stored) {
		String unexcluded = unexcludeWindowsReservedNames(stored);
		String hexed = removeSingleCharacterConversions(unexcluded,
				NAME_SINGLE_CHARACTER_SOURCES, NAME_SINGLE_CHARACTER_TARGETS);
		return removeHexEncoding(hexed);
	}

	/**
	 * If the stored filename was a tilde followed by a Windows reserved word,
	 * strip the tilde.
	 */
	private static String unexcludeWindowsReservedNames(String stored) {
		if (stored.startsWith("~")) {
			String remainder = stored.substring(1);
			for (String word : WINDOWS_RESERVED_NAMES) {
				if (word.equalsIgnoreCase(remainder)) {
					return remainder;
				}
			}
		}
		return stored;
	}

	/**
	 * Convert common single-character substitutions back to their original
	 * values.
	 */
	private static String removeSingleCharacterConversions(String cleaned,
			char[] sources, char[] targets) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < cleaned.length(); i++) {
			char c = cleaned.charAt(i);
			result.append(translateSingleCharacter(c, targets, sources));
		}
		log.debug("Remove single character conversions from '" + cleaned
				+ "' giving '" + result + "'");
		return result.toString();
	}

	/**
	 * Convert hex-encoded characters back to their original values.
	 */
	private static String removeHexEncoding(String encoded) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < encoded.length(); i++) {
			char c = encoded.charAt(i);
			if (c == HEX_ESCAPE_CHAR) {
				try {
					if (i + 2 >= encoded.length()) {
						throw new InvalidPathException(
								"Invalid hex encoding in path: '" + encoded
										+ "'");
					}
					String hexChars = encoded.substring(i + 1, i + 3);
					int value = Integer.parseInt(hexChars, 16);
					result.append((char) value);
					i += 2;
				} catch (NumberFormatException e) {
					throw new InvalidPathException(
							"Invalid hex encoding in path: '" + encoded + "'",
							e);
				}
			} else {
				result.append(c);
			}
		}
		log.debug("Remove hex encodings from '" + encoded + "' giving '"
				+ result + "'");
		return result.toString();
	}

	/**
	 * Translate the object ID to a relative directory path. A recognized
	 * namespace is translated to its prefix, and illegal characters are
	 * encoded. The resulting string is broken up into 3-character directory
	 * names (or less). Windows reserved words are prefixed with tilde.
	 * 
	 * @see edu.cornell.mannlib.vitro.webapp.utils.filestorage
	 */
	public static String id2Path(String id, Map<Character, String> namespacesMap) {
		char prefix = 0;
		String localName = id;
		for (Entry<Character, String> entry : namespacesMap.entrySet()) {
			String namespace = entry.getValue();
			if (id.startsWith(namespace)) {
				prefix = entry.getKey();
				localName = id.substring(namespace.length());
				break;
			}
		}

		String hexed = addHexEncoding(localName);
		String cleaned = addSingleCharacterConversions(hexed,
				PATH_SINGLE_CHARACTER_SOURCES, PATH_SINGLE_CHARACTER_TARGETS);
		String prefixed = applyPrefixChar(prefix, cleaned);
		String brokenUp = insertPathDelimiters(prefixed);
		String result = excludeWindowsWordsFromPath(brokenUp);
		log.debug("id2Path: id='" + id + "', namespaces='" + namespacesMap
				+ "', path='" + result + "'");
		return result;
	}

	/**
	 * Now that the cleaning is complete, add the prefix if there is one.
	 */
	private static String applyPrefixChar(char prefix, String cleaned) {
		if (prefix == 0) {
			return cleaned;
		} else {
			return prefix + "~" + cleaned;
		}
	}

	/**
	 * Add path delimiters as needed to turn the cleaned prefixed string into a
	 * relative path.
	 */
	private static String insertPathDelimiters(String prefixed) {
		StringBuilder path = new StringBuilder();
		for (int i = 0; i < prefixed.length(); i++) {
			if ((i % SHORTY_LENGTH == 0) && (i > 0)) {
				path.append(File.separatorChar);
			}
			path.append(prefixed.charAt(i));
		}
		log.debug("Insert path delimiters to '" + prefixed + "' giving '"
				+ path + "'");
		return path.toString();
	}

	/**
	 * Check each part in the path, and if it is a Windows reserved word, add a
	 * tilde. This only applies to the relative path.
	 */
	private static String excludeWindowsWordsFromPath(String rawPath) {
		String path = rawPath.replace(File.separatorChar, '/');
		String[] parts = path.split("/");
		
		StringBuilder newPath = new StringBuilder();
		
		for (int i = 0; i < parts.length; i++) {
			String part = excludeWindowsReservedNames(parts[i]);
			if (i > 0) {
				newPath.append(File.separatorChar);
			}
			newPath.append(part);
		}
		return newPath.toString();
	}

	/**
	 * Translate the object ID and the file storage root directory into a full
	 * path to the directory that would represent that ID.
	 * 
	 * @see edu.cornell.mannlib.vitro.webapp.utils.filestorage
	 */
	public static File getPathToIdDirectory(String id,
			Map<Character, String> namespacesMap, File rootDir) {
		return new File(rootDir, id2Path(id, namespacesMap));
	}

	/**
	 * Translate the object ID, the file storage root directory and the filename
	 * into a full path to where the file would be stored.
	 * 
	 * @see edu.cornell.mannlib.vitro.webapp.utils.filestorage
	 */
	public static File getFullPath(File rootDir, String id, String filename,
			Map<Character, String> namespacesMap) {
		return new File(getPathToIdDirectory(id, namespacesMap, rootDir),
				encodeName(filename));
	}

}
