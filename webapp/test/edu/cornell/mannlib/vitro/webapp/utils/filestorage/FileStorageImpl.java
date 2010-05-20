/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.filestorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;

/**
 * The default implementation of {@link FileStorage}.
 */
public class FileStorageImpl extends FileStorage {

	// static FileStorage getInstance()
	// gets baseDir and namespaces from ConfigurationProperties
	// gets instance class from system properties
	// throws IllegalStateException if requires properties are missing
	// throws IOException
	//	
	// void createFile(String id, String filename, InputStream bytes)
	// stores the bytes as a file with this name under this id
	// if the file already exists, over-writes it
	// throws FileAlreadyExistsException if a file already exists under this id
	// with a different name
	// throws IOException
	//
	// String getfilename(String id)
	// returns the name of the file at this ID, or null if there is none.
	// throws IOException
	//	
	// byte[] getFile(String id, String filename)
	// gets the bytes from the file
	// throws FileNotFoundException if the file does not exist
	// throws IOException
	//
	// boolean deleteFile(String id)
	// removes the file at this id, returns true
	// if no such file, takes no action, returns false
	// throws IOException
	//		
	// FileStorageImpl
	//

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
	FileStorageImpl() {
		this(figureBaseDir(), figureFileNamespace());
	}

	// package-level constructor(File baseDir, Collection<String> namespaces),
	// gets properties from arguments
	// if baseDir is not initialized with file_storage_root and
	// file_storage_prefixMap, do it.
	// otherwise check for correctness and consistency
	// throws IllegalStateException if partially initialized
	// throws IllegalStateException if already initialized and namespaces don't
	// match

	/**
	 * Use the arguments to create an instance. If the base directory is empty,
	 * initialize it. Otherwise, check that it was initialized to the same
	 * namespaces.
	 * 
	 * @throws IllegalArgumentException
	 *             if the configuration property doesn't point to an existing,
	 *             writeable directory.
	 */
	FileStorageImpl(File baseDir, Collection<String> namespaces) {
		if (baseDir == null) {
			throw new NullPointerException("baseDir may not be null.");
		}
		if (namespaces == null) {
			throw new NullPointerException("namespaces may not be null.");
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
	 */
	private static File figureBaseDir() {
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
	 * @returns the file namespace is assumed to be in this form:
	 *          <code>http://vivo.mydomain.edu/file/</code>
	 */
	private static Collection<String> figureFileNamespace() {
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
	 * {@inheritDoc}
	 */
	@Override
	public void createFile(String id, String filename, InputStream bytes)
			throws FileAlreadyExistsException, IOException {
		// TODO Auto-generated method stub
		throw new RuntimeException("FileStorage.createFile() not implemented.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean deleteFile(String id) throws IOException {
		// TODO Auto-generated method stub
		throw new RuntimeException("FileStorage.deleteFile() not implemented.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFilename(String id) throws IOException {
		// TODO Auto-generated method stub
		throw new RuntimeException("FileStorage.getFilename() not implemented.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getfile(String id, String filename)
			throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		throw new RuntimeException("FileStorage.getfile() not implemented.");
	}

}
