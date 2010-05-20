/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.filestorage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;

/**
 * The "interface" for the File Storage system. All methods are abstract except
 * for the factory method.
 */
public abstract class FileStorage {
	/**
	 * If this system property is set, it will be taken as the name of the
	 * implementing class.
	 */
	public static final String PROPERTY_IMPLEMETATION_CLASSNAME = FileStorage.class
			.getName();

	/**
	 * The default implementation will use this key to ask
	 * {@link ConfigurationProperties} for the file storage base directory.
	 */
	public static final String PROPERTY_FILE_STORAGE_BASE_DIR = "upload.directory";

	/**
	 * The default implementation will use this key to ask
	 * {@link ConfigurationProperties} for the default URI namespace.
	 */
	public static final String PROPERTY_DEFAULT_NAMESPACE = "Vitro.defaultNamespace";

	/**
	 * <p>
	 * Get an instance of {@link FileStorage}. By default, this will be an
	 * instance of {@link FileStorageImpl}.
	 * </p>
	 * <p>
	 * If the System Property named by
	 * {#SYSTEM_PROPERTY_IMPLEMETATION_CLASSNAME} is set, it must contain the
	 * name of the implementation class, which must be a sub-class of
	 * {@link FileStorage}, and must have a public, no-argument constructor.
	 * </p>
	 */
	public static FileStorage getInstance() {
		String className = System.getProperty(PROPERTY_IMPLEMETATION_CLASSNAME);
		if (className == null) {
			return new FileStorageImpl();
		}

		try {
			Class<?> clazz = Class.forName(className);
			Object instance = clazz.newInstance();
			return FileStorage.class.cast(instance);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(
					"Can't create a FileStorage instance", e);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(
					"Can't create a FileStorage instance", e);
		} catch (InstantiationException e) {
			throw new IllegalArgumentException(
					"Can't create a FileStorage instance", e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(
					"Can't create a FileStorage instance", e);
		}
	}

	/**
	 * Store the bytes from this stream as a file with the specified ID and
	 * filename. If the file already exists, it is over-written.
	 * 
	 * @throws FileAlreadyExistsException
	 *             if a file already exists with this ID but with a different
	 *             filename.
	 * 
	 */
	public abstract void createFile(String id, String filename,
			InputStream bytes) throws FileAlreadyExistsException, IOException;

	/**
	 * If a file exists with this ID, get its name.
	 * 
	 * @return The name of the file (un-encoded) if it exists, or
	 *         <code>null</code> if it does not.
	 */
	public abstract String getFilename(String id) throws IOException;

	/**
	 * Get the contents of the file with this ID and this filename.
	 * 
	 * @throws FileNotFoundException
	 *             if there is no file that matches this ID and filename.
	 */
	public abstract byte[] getfile(String id, String filename)
			throws FileNotFoundException, IOException;

	/**
	 * If a file exists with this ID, it will be deleted, regardless of the file
	 * name. If no such file exists, no action is taken, no exception is thrown.
	 * 
	 * @return <code>true<code> if a file existed, <code>false</code> otherwise.
	 */
	public abstract boolean deleteFile(String id) throws IOException;
}
