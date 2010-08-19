/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.backend;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The default implementation of {@link FileStorage}.
 */
public class FileStorageImpl implements FileStorage {
	private static final Log log = LogFactory.getLog(FileStorageImpl.class);

	private final File baseDir;
	private final File rootDir;
	private final File namespaceFile;
	private final Map<Character, String> namespacesMap;

	// ----------------------------------------------------------------------
	// Constructors and helper methods.
	// ----------------------------------------------------------------------

	/**
	 * Use the arguments to create an instance. If the base directory is empty,
	 * initialize it. Otherwise, check that it was initialized to the same
	 * namespaces.
	 * 
	 * @throws IllegalArgumentException
	 *             if the configuration property doesn't point to an existing,
	 *             writeable directory.
	 */
	FileStorageImpl(File baseDir, Collection<String> namespaces)
			throws IOException {
		checkBaseDirValid(baseDir);
		checkNamespacesValid(namespaces);

		this.baseDir = baseDir;
		this.rootDir = new File(this.baseDir, FILE_STORAGE_ROOT);

		this.namespaceFile = new File(baseDir,
				FILE_STORAGE_NAMESPACES_PROPERTIES);

		if (rootDir.exists() && namespaceFile.exists()) {
			Map<Character, String> existingMap = readNamespaces();
			this.namespacesMap = adjustNamespaces(existingMap, namespaces);
			if (!namespacesMap.equals(existingMap)) {
				initializeNamespacesFile();
			}
		} else if (!rootDir.exists() && !namespaceFile.exists()) {
			this.namespacesMap = mapNamespaces(namespaces);
			initializeRootDirectory();
			initializeNamespacesFile();
		} else if (rootDir.exists()) {
			throw new IllegalStateException("Storage directory '"
					+ baseDir.getPath() + "' has been partially initialized. '"
					+ FILE_STORAGE_ROOT + "' exists, but '"
					+ FILE_STORAGE_NAMESPACES_PROPERTIES + "' does not.");
		} else {
			throw new IllegalStateException("Storage directory '"
					+ baseDir.getPath() + "' has been partially initialized. '"
					+ FILE_STORAGE_NAMESPACES_PROPERTIES + "' exists, but '"
					+ FILE_STORAGE_ROOT + "' does not.");
		}
	}

	private void checkNamespacesValid(Collection<String> namespaces) {
		if (namespaces == null) {
			throw new NullPointerException("namespaces may not be null.");
		}
	}

	/**
	 * 'baseDir' must point to an existing, writeable directory.
	 */
	private void checkBaseDirValid(File baseDir) {
		if (baseDir == null) {
			throw new NullPointerException("baseDir may not be null.");
		}
		if (!baseDir.exists()) {
			throw new IllegalArgumentException(
					"File upload directory does not exist: '"
							+ baseDir.getPath() + "'");
		}
		if (!baseDir.isDirectory()) {
			throw new IllegalArgumentException(
					"File upload directory is not a directory: '"
							+ baseDir.getPath() + "'");
		}
		if (!baseDir.canWrite()) {
			throw new IllegalArgumentException(
					"File upload directory is not writeable: '"
							+ baseDir.getPath() + "'");
		}
	}

	/**
	 * Assign arbitrary prefixes to these namespaces.
	 */
	private Map<Character, String> mapNamespaces(Collection<String> namespaces) {
		Map<Character, String> map = new HashMap<Character, String>();
		for (String namespace : namespaces) {
			map.put(findAvailableKey(map), namespace);
		}
		return map;
	}

	/**
	 * @throws FileNotFoundException
	 */
	private void initializeNamespacesFile() throws FileNotFoundException {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(this.namespaceFile);
			for (Entry<Character, String> entry : this.namespacesMap.entrySet()) {
				writer.println(entry.getKey() + " = " + entry.getValue());
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Create the root directory. Check for success.
	 */
	private void initializeRootDirectory() throws IOException {
		boolean created = this.rootDir.mkdir();
		if (!created) {
			throw new IOException("Failed to create root directory '"
					+ this.rootDir + "'");
		}
	}

	/**
	 * Load the namespaces file from the disk. It's easy to load into a
	 * {@link Properties}, but we need to convert it to a {@link Map}.
	 */
	private Map<Character, String> readNamespaces() throws IOException {
		Reader reader = null;
		try {
			reader = new FileReader(this.namespaceFile);
			Properties props = new Properties();
			props.load(reader);

			Map<Character, String> map = new HashMap<Character, String>();
			for (Object key : props.keySet()) {
				char keyChar = key.toString().charAt(0);
				map.put(keyChar, (String) props.get(key));
			}

			return map;
		} catch (Exception e) {
			throw new IOException("Problem loading the namespace file.");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * If any of the requested aren't in the existing map, add them.
	 */
	private Map<Character, String> adjustNamespaces(
			Map<Character, String> existingMap, Collection<String> namespaces) {
		Map<Character, String> adjustedMap = new HashMap<Character, String>(
				existingMap);

		for (String namespace : namespaces) {
			if (!existingMap.values().contains(namespace)) {
				log.warn("Adding a new namespace to the file storage system: "
						+ namespace);
				Character key = findAvailableKey(adjustedMap);
				adjustedMap.put(key, namespace);
			}
		}

		return adjustedMap;
	}

	/**
	 * Are there any characters that we're not using as prefixes? We only allow
	 * a-z.
	 */
	private Character findAvailableKey(Map<Character, String> adjustedMap) {
		for (char key = 'a'; key <= 'z'; key++) {
			if (!adjustedMap.keySet().contains(key)) {
				return key;
			}
		}
		throw new IllegalArgumentException(
				"Can't handle more than 26 namespaces.");
	}

	// ----------------------------------------------------------------------
	// package access methods -- used in unit tests.
	// ----------------------------------------------------------------------

	File getBaseDir() {
		return this.baseDir;
	}

	Map<Character, String> getNamespaces() {
		return this.namespacesMap;
	}

	// ----------------------------------------------------------------------
	// Public methods
	// ----------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Before creating the file, we may need to create one or more parent
	 * directories to put it in.
	 * </p>
	 */
	@Override
	public void createFile(String id, String filename, InputStream bytes)
			throws FileAlreadyExistsException, IOException {
		String existingFilename = getFilename(id);
		if ((existingFilename != null) && (!filename.equals(existingFilename))) {
			throw new FileAlreadyExistsException(id, existingFilename, filename);

		}

		File file = FileStorageHelper.getFullPath(this.rootDir, id, filename,
				this.namespacesMap);
		File parent = file.getParentFile();

		if (!parent.exists()) {
			parent.mkdirs();
			if (!parent.exists()) {
				throw new IOException(
						"Failed to create parent directories for file with ID '"
								+ id + "', file location '" + file + "'");
			}
		}

		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			InputStream in = new BufferedInputStream(bytes);

			byte[] buffer = new byte[4096];
			int howMany;
			while (-1 != (howMany = in.read(buffer))) {
				out.write(buffer, 0, howMany);
			}
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * If deleting this file leaves its parent directory empty, that directory
	 * will be deleted. This repeats, up to (but not including) the root
	 * directory.
	 */
	@Override
	public boolean deleteFile(String id) throws IOException {
		String existingFilename = getFilename(id);
		if (existingFilename == null) {
			return false;
		}

		File file = FileStorageHelper.getFullPath(this.rootDir, id,
				existingFilename, this.namespacesMap);

		file.delete();
		if (file.exists()) {
			throw new IOException("Failed to delete file with ID '" + id
					+ "', file location '" + file + "'");
		}

		deleteEmptyParents(file);

		return true;
	}

	/**
	 * We have deleted this file. If the parent directory is now empty, delete
	 * it. Then check its parent in turn. This continues until we find a parent
	 * that we will not delete, either because it is not empty, or because it is
	 * the file storage root.
	 */
	private void deleteEmptyParents(File file) {
		File parent = file.getParentFile();
		if (parent == null) {
			log.warn("This is crazy. How can file '" + file.getAbsolutePath()
					+ "' have no parent?");
			return;
		}

		if (parent.equals(rootDir)) {
			log.trace("Not deleting the root directory.");
			return;
		}

		File[] children = parent.listFiles();
		if (children == null) {
			log.warn("This is crazy. How can file '" + parent.getAbsolutePath()
					+ "' not be a directory?");
			return;
		}

		if (children.length > 0) {
			log.trace("Directory '" + parent.getAbsolutePath()
					+ "' is not empty. Not deleting.");
			return;
		}

		log.trace("Deleting empty directory '" + parent.getAbsolutePath() + "'");
		parent.delete();
		if (parent.exists()) {
			log.warn("Failed to delete directory '" + parent.getAbsolutePath()
					+ "'");
			return;
		}

		deleteEmptyParents(parent);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * For a non-null result, a directory must exist for the ID, and it must
	 * contain a file (it may or may not contain other directories).
	 * </p>
	 */
	@Override
	public String getFilename(String id) throws IOException {
		File dir = FileStorageHelper.getPathToIdDirectory(id,
				this.namespacesMap, this.rootDir);
		log.debug("ID '" + id + "' translates to this directory path: '" + dir
				+ "'");

		if ((!dir.exists()) || (!dir.isDirectory())) {
			return null;
		}

		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isFile();
			}
		});

		if (files.length == 0) {
			return null;
		}

		if (files.length > 1) {
			throw new IllegalStateException(
					"More than one file associated with ID: '" + id
							+ "', directory location '" + dir + "'");
		}

		return FileStorageHelper.decodeName(files[0].getName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream getInputStream(String id, String filename)
			throws IOException {

		File file = FileStorageHelper.getFullPath(this.rootDir, id, filename,
				this.namespacesMap);

		if (!file.exists()) {
			throw new FileNotFoundException("No file exists with ID '" + id
					+ "', file location '" + file + "'");
		}

		return new FileInputStream(file);
	}
}
