/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.backend;

import java.io.IOException;

/**
 * Create an instance of {@link FileStorage} -- either the default
 * implementation, or one specified by a system property.
 */
public class FileStorageFactory {
	/**
	 * If this system property is set, it will be taken as the name of the
	 * implementing class.
	 */
	public static final String PROPERTY_IMPLEMETATION_CLASSNAME = FileStorage.class
			.getName();

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
	public static FileStorage getFileStorage() throws IOException {
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

}
