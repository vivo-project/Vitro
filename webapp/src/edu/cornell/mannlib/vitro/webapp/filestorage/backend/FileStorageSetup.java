/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.backend;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Initializes the file storage system, and stores a reference in the servlet
 * context.
 */
public class FileStorageSetup implements ServletContextListener {
	private static final Log log = LogFactory.getLog(FileStorageSetup.class);

	/**
	 * The implementation of the {@link FileStorage} system will be stored in
	 * the {@link ServletContext} as an attribute with this name.
	 */
	public static final String ATTRIBUTE_NAME = FileStorage.class.getName();

	/**
	 * The default implementation will use this key to ask
	 * {@link ConfigurationProperties} for the vivo home directory. The file
	 * storage base directory is in a subdirectory below this one.
	 */
	public static final String PROPERTY_VITRO_HOME_DIR = "vitro.home.directory";
	public static final String FILE_STORAGE_SUBDIRECTORY = "uploads";

	/**
	 * The default implementation will use this key to ask
	 * {@link ConfigurationProperties} for the default URI namespace.
	 */
	public static final String PROPERTY_DEFAULT_NAMESPACE = "Vitro.defaultNamespace";

	/**
	 * Create an implementation of {@link FileStorage} and store it in the
	 * {@link ServletContext}, as an attribute named according to
	 * {@link #ATTRIBUTE_NAME}.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		try {
			File baseDirectory = figureBaseDir(sce);
			Collection<String> fileNamespace = confirmDefaultNamespace(sce);
			FileStorage fs = new FileStorageImpl(baseDirectory, fileNamespace);

			ctx.setAttribute(ATTRIBUTE_NAME, fs);
		} catch (Exception e) {
			log.fatal("Failed to initialize the file system.", e);
			ss.fatal(this, "Failed to initialize the file system.", e);
		}
	}

	/**
	 * Get the configuration property for the file storage base directory, and
	 * check that it points to an existing, writeable directory.
	 * 
	 * For use by the constructor in implementations of {@link FileStorage}.
	 */
	private File figureBaseDir(ServletContextEvent sce) throws IOException {
		String homeDirPath = ConfigurationProperties.getBean(sce)
			.getProperty(PROPERTY_VITRO_HOME_DIR);
		if (homeDirPath == null) {
			throw new IllegalArgumentException(
					"Configuration properties must contain a value for '"
							+ PROPERTY_VITRO_HOME_DIR + "'");
		}
		
		File homeDir = new File(homeDirPath);
		if (!homeDir.exists()) {
			throw new IllegalStateException("Vitro home directory '"
					+ homeDir.getAbsolutePath() + "' does not exist.");
		}

		File baseDir = new File(homeDir, FILE_STORAGE_SUBDIRECTORY);
		if (!baseDir.exists()) {
			boolean created = baseDir.mkdir();
			if (!created) {
				throw new IOException(
						"Unable to create uploads directory at '"
								+ baseDir + "'");
			}
		}
		return baseDir;
	}

	/**
	 * Get the configuration property for the default namespace, and confirm
	 * that it is in the proper form. The default namespace is assumed to be in
	 * this form: <code>http://vivo.mydomain.edu/individual/</code>
	 * 
	 * For use by the constructor in implementations of {@link FileStorage}.
	 * 
	 * @returns a collection containing the default namespace.
	 */
	private Collection<String> confirmDefaultNamespace(ServletContextEvent sce) {
		String defaultNamespace = ConfigurationProperties.getBean(sce)
				.getProperty(PROPERTY_DEFAULT_NAMESPACE);
		if (defaultNamespace == null) {
			throw new IllegalArgumentException(
					"Configuration properties must contain a value for '"
							+ PROPERTY_DEFAULT_NAMESPACE + "'");
		}

		String defaultSuffix = "/individual/";

		if (!defaultNamespace.endsWith(defaultSuffix)) {
			log.warn("Default namespace does not match the expected form "
					+ "(does not end with '" + defaultSuffix + "'): '"
					+ defaultNamespace + "'");
		}

		return Collections.singleton(defaultNamespace);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do here.
	}

}
