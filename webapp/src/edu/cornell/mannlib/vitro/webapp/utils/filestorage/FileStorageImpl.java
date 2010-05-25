/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.filestorage;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;

/**
 * The default implementation of {@link FileStorage}.
 */
public class FileStorageImpl implements FileStorage {

	private final File baseDir;
	private final File rootDir;
	private final File namespaceFile;
	private final Map<Character, String> namespacesMap;

	// ----------------------------------------------------------------------
	// Constructors and helper methods.
	// ----------------------------------------------------------------------

	/**
	 * Use the configuration properties to create an instance.
	 * 
	 * @throws IllegalArgumentException
	 *             if the configuration property for the base directory is
	 *             missing, or if it doesn't point to an existing, writeable
	 *             directory.
	 * @throws IllegalArgumentException
	 *             if the configuration property for the default namespace is
	 *             missing, or if it isn't in the expected form.
	 */
	FileStorageImpl() throws IOException {
		this(figureBaseDir(), figureFileNamespace());
	}

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
			this.namespacesMap = confirmNamespaces(namespaces);
		} else if (!rootDir.exists() && !namespaceFile.exists()) {
			this.namespacesMap = mapNamespaces(namespaces);
			initializeStorage();
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
	 * Get the configuration property for the file storage base directory, and
	 * check that it points to an existing, writeable directory.
	 * 
	 * For use by the constructor in implementations of {@link FileStorage}.
	 */
	static File figureBaseDir() {
		String baseDirPath = ConfigurationProperties
				.getProperty(PROPERTY_FILE_STORAGE_BASE_DIR);
		if (baseDirPath == null) {
			throw new IllegalArgumentException(
					"Configuration properties must contain a value for '"
							+ PROPERTY_FILE_STORAGE_BASE_DIR + "'");
		}
		return new File(baseDirPath);
	}

	/**
	 * Get the configuration property for the default namespace, and derive the
	 * file namespace from it. The default namespace is assumed to be in this
	 * form: <code>http://vivo.mydomain.edu/individual/</code>
	 * 
	 * For use by the constructor in implementations of {@link FileStorage}.
	 * 
	 * @returns the file namespace is assumed to be in this form:
	 *          <code>http://vivo.mydomain.edu/file/</code>
	 */
	static Collection<String> figureFileNamespace() {
		String defaultNamespace = ConfigurationProperties
				.getProperty(PROPERTY_DEFAULT_NAMESPACE);
		if (defaultNamespace == null) {
			throw new IllegalArgumentException(
					"Configuration properties must contain a value for '"
							+ PROPERTY_DEFAULT_NAMESPACE + "'");
		}

		String defaultSuffix = "/individual/";
		String fileSuffix = "/file/";

		if (!defaultNamespace.endsWith(defaultSuffix)) {
			throw new IllegalArgumentException(
					"Default namespace does not match the expected form: '"
							+ defaultNamespace + "'");
		}

		int hostLength = defaultNamespace.length() - defaultSuffix.length();
		String fileNamespace = defaultNamespace.substring(0, hostLength)
				+ fileSuffix;
		return Collections.singleton(fileNamespace);
	}

	/**
	 * Assign arbitrary prefixes to these namespaces.
	 */
	private Map<Character, String> mapNamespaces(Collection<String> namespaces) {
		Map<Character, String> map = new HashMap<Character, String>();

		char prefixChar = 'a';
		for (String namespace : namespaces) {
			map.put(prefixChar, namespace);
			prefixChar++;
			if (prefixChar > 'z') {
				throw new IllegalArgumentException(
						"Can't handle more than 26 namespaces.");
			}
		}
		return map;
	}

	/**
	 * Create the root directory and the namespaces file. Write the namespaces
	 * map to the namespaces file.
	 */
	private void initializeStorage() throws IOException {
		boolean created = this.rootDir.mkdir();
		if (!created) {
			throw new IOException("Failed to create root directory '"
					+ this.rootDir + "'");
		}

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
	 * Confirm that the namespaces file contains mappings for these namespaces,
	 * and only these namespaces.
	 */
	private Map<Character, String> confirmNamespaces(
			Collection<String> namespaces) throws IOException {
		Map<Character, String> map;
		Reader reader = null;
		try {
			reader = new FileReader(this.namespaceFile);
			Properties props = new Properties();
			props.load(reader);
			map = new HashMap<Character, String>();
			for (Object key : props.keySet()) {
				char keyChar = key.toString().charAt(0);
				map.put(keyChar, (String) props.get(key));
			}
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

		Set<String> requestedNamespaces = new HashSet<String>(namespaces);
		Set<String> previousNamespaces = new HashSet<String>(map.values());
		if (!requestedNamespaces.equals(previousNamespaces)) {
			throw new IllegalStateException(
					"File storage was previously initialized with a "
							+ "different set of namespaces than are found "
							+ "in the current request. Previous: "
							+ previousNamespaces + ", Requested: "
							+ requestedNamespaces);
		}

		return map;
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

		return true;
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
