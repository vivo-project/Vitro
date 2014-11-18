/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileAlreadyExistsException;
import edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileStorage;

/**
 * A thin wrapper around the existing FileStorageImpl. Handles the setup.
 */
public class FileStorageImplWrapper implements FileStorage {
	public static final String PROPERTY_DEFAULT_NAMESPACE = "Vitro.defaultNamespace";
	public static final String FILE_STORAGE_SUBDIRECTORY = "uploads";

	private FileStorageImpl fs;

	/**
	 * Create an instance of FileStorageImpl, based on the values in runtime.properties.
	 */
	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		ServletContext ctx = application.getServletContext();

		try {
			File baseDirectory = figureBaseDir();
			Collection<String> fileNamespace = confirmDefaultNamespace(ctx);
			fs = new FileStorageImpl(baseDirectory, fileNamespace);
		} catch (Exception e) {
			ss.fatal("Failed to initialize the file system.", e);
		}
	}

	/**
	 * Get the configuration property for the file storage base directory, and
	 * check that it points to an existing, writeable directory.
	 */
	private File figureBaseDir() throws IOException {
		File homeDir = ApplicationUtils.instance().getHomeDirectory().getPath().toFile();
		File baseDir = new File(homeDir, FILE_STORAGE_SUBDIRECTORY);
		if (!baseDir.exists()) {
			boolean created = baseDir.mkdir();
			if (!created) {
				throw new IOException("Unable to create uploads directory at '"
						+ baseDir + "'");
			}
		}
		return baseDir;
	}

	/**
	 * Get the configuration property for the default namespace.
	 */
	private Collection<String> confirmDefaultNamespace(ServletContext ctx) {
		String defaultNamespace = ConfigurationProperties.getBean(ctx)
				.getProperty(PROPERTY_DEFAULT_NAMESPACE);
		if (defaultNamespace == null) {
			throw new IllegalArgumentException(
					"Configuration properties must contain a value for '"
							+ PROPERTY_DEFAULT_NAMESPACE + "'");
		}

		return Collections.singleton(defaultNamespace);
	}

	@Override
	public void shutdown(Application application) {
		// Nothing to shut down.
	}

	// ----------------------------------------------------------------------
	// Delegated methods
	// ----------------------------------------------------------------------

	@Override
	public void createFile(String id, String filename, InputStream bytes)
			throws FileAlreadyExistsException, IOException {
		fs.createFile(id, filename, bytes);
	}

	@Override
	public String getFilename(String id) throws IOException {
		return fs.getFilename(id);
	}

	@Override
	public InputStream getInputStream(String id, String filename)
			throws FileNotFoundException, IOException {
		return fs.getInputStream(id, filename);
	}

	@Override
	public boolean deleteFile(String id) throws IOException {
		return fs.deleteFile(id);
	}

}
