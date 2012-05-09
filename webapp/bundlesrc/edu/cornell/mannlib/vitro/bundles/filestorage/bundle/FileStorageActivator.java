/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.bundles.filestorage.bundle;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import edu.cornell.mannlib.vitro.bundles.filestorage.internal.FileStorageImpl;
import edu.cornell.mannlib.vitro.webapp.modules.interfaces.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modules.interfaces.FileStorage;
import edu.cornell.mannlib.vitro.webapp.modules.interfaces.StartupStatus;

/**
 * Start the FileStorage implementation as a service.
 */
@Component
public class FileStorageActivator {
	private static final Log log = LogFactory.getLog(FileStorageActivator.class);

	/**
	 * Use this key to ask ConfigurationProperties for the Vitro home directory.
	 */
	public static final String PROPERTY_VITRO_HOME_DIR = "vitro.home.directory";

	/**
	 * This path leads from the Vitro home directory to the base directory of
	 * the File Storage System.
	 */
	public static final String FILE_STORAGE_SUBDIRECTORY = "uploads";

	/**
	 * Use this key to ask ConfigurationProperties for the default namespace.
	 */
	public static final String PROPERTY_DEFAULT_NAMESPACE = "Vitro.defaultNamespace";

	private ConfigurationProperties configProps;
	private StartupStatus ss;

	private ServiceRegistration<?> sr;

	@Reference
	public void setConfigurationProperties(ConfigurationProperties configProps) {
		this.configProps = configProps;
	}

	@Reference
	public void setStartupStatus(StartupStatus ss) {
		this.ss = ss;
	}

	/**
	 * Initializes the file storage system, and publish an implementation of the
	 * FileStorage service.
	 */
	@Activate
	public void startup(BundleContext bundleContext) {
		try {
			// Figure the parameters.
			File baseDirectory = figureBaseDir();
			Collection<String> fileNamespace = confirmDefaultNamespace();

			// Create the implementation instance.
			FileStorage fs = new FileStorageImpl(baseDirectory, fileNamespace);

			// Register it.
			sr = bundleContext.registerService(FileStorage.class, fs, null);
		} catch (Exception e) {
			log.fatal("Failed to initialize the file system.", e);
			ss.fatal(this, "Failed to initialize the file system.", e);
		}
	}

	/**
	 * Get the configuration property for the file storage base directory, and
	 * check that it points to an existing, writeable directory.
	 */
	private File figureBaseDir() throws IOException {
		String homeDirPath = configProps.getProperty(PROPERTY_VITRO_HOME_DIR);
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
				throw new IOException("Unable to create uploads directory at '"
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
	 * @returns a collection containing the default namespace.
	 */
	private Collection<String> confirmDefaultNamespace() {
		String defaultNamespace = configProps
				.getProperty(PROPERTY_DEFAULT_NAMESPACE);
		if (defaultNamespace == null) {
			throw new IllegalArgumentException(
					"Configuration properties must contain a value for '"
							+ PROPERTY_DEFAULT_NAMESPACE + "'");
		}

		String defaultSuffix = "/individual/";

		if (!defaultNamespace.endsWith(defaultSuffix)) {
			String message = "Default namespace does not match the expected "
					+ "form (does not end with '" + defaultSuffix + "'): '"
					+ defaultNamespace + "'";
			log.warn(message);
			ss.warning(this, message);
		}

		return Collections.singleton(defaultNamespace);
	}

	@Deactivate
	public void shutdown() {
		sr.unregister();
	}
}
