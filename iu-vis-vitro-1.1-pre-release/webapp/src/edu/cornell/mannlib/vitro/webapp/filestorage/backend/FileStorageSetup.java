/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.backend;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;

/**
 * Initializes the file storage system, and stores a reference in the servlet
 * context.
 */
public class FileStorageSetup implements ServletContextListener {
	/**
	 * The implementation of the {@link FileStorage} system will be stored in
	 * the {@link ServletContext} as an attribute with this name.
	 */
	public static final String ATTRIBUTE_NAME = FileStorage.class.getName();

	/**
	 * The default implementation will use this key to ask
	 * {@link ConfigurationProperties} for the file storage base directory.
	 */
	static final String PROPERTY_FILE_STORAGE_BASE_DIR = "upload.directory";

	/**
	 * The default implementation will use this key to ask
	 * {@link ConfigurationProperties} for the default URI namespace.
	 */
	static final String PROPERTY_DEFAULT_NAMESPACE = "Vitro.defaultNamespace";

	/**
	 * Create an implementation of {@link FileStorage} and store it in the
	 * {@link ServletContext}, as an attribute named according to
	 * {@link #ATTRIBUTE_NAME}.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		FileStorage fs;
		try {
			File baseDirectory = figureBaseDir();
			Collection<String> fileNamespace = figureFileNamespace();
			fs = new FileStorageImpl(baseDirectory, fileNamespace);
		} catch (IOException e) {
			throw new IllegalStateException(
					"Failed to initialize the file system.", e);
		}

		ServletContext sc = sce.getServletContext();
		sc.setAttribute(ATTRIBUTE_NAME, fs);
	}

	/**
	 * Get the configuration property for the file storage base directory, and
	 * check that it points to an existing, writeable directory.
	 * 
	 * For use by the constructor in implementations of {@link FileStorage}.
	 */
	private File figureBaseDir() {
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
	private Collection<String> figureFileNamespace() {
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

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do here.
	}

}
