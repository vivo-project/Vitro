/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage;

import org.apache.log4j.Logger;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorageSetup;

/**
 * Static methods to help when serving uploaded files.
 */
public class FileServingHelper {
	private static final Logger log = Logger.getLogger(FileServingHelper.class);

	private static final String DEFAULT_PATH = "/individual/";
	private static final String FILE_PATH = "/file/";
	private static final String DEFAULT_NAMESPACE = initializeDefaultNamespace();

	/**
	 * At startup, get the default namespace from the configuration properties,
	 * and trim off the suffix.
	 */
	private static String initializeDefaultNamespace() {
		String defaultNamespace = ConfigurationProperties
				.getProperty(FileStorageSetup.PROPERTY_DEFAULT_NAMESPACE);
		if (defaultNamespace == null) {
			throw new IllegalArgumentException(
					"Configuration properties must contain a value for '"
							+ FileStorageSetup.PROPERTY_DEFAULT_NAMESPACE + "'");
		}

		if (!defaultNamespace.endsWith(DEFAULT_PATH)) {
			throw new IllegalArgumentException(
					"Default namespace does not match the expected form: '"
							+ defaultNamespace + "'");
		}

		return defaultNamespace;
	}

	/**
	 * <p>
	 * Combine the URI and the filename to produce a relative URL for the file
	 * (relative to the context of the webapp).
	 * </p>
	 * <p>
	 * This should involve stripping the default namespace from the front of the
	 * URL, replacing it with the file prefix, and adding the filename to the
	 * end.
	 * </p>
	 * 
	 * @return <ul>
	 *         <li>the translated URL, if the URI was in the default namespace,</li>
	 *         <li>the original URI, if it wasn't in the default namespace,</li>
	 *         <li>null, if the original URI or the filename was null.</li>
	 *         </ul>
	 */
	public static String getBytestreamAliasUrl(String uri, String filename) {
		if ((uri == null) || (filename == null)) {
			return null;
		}
		if (!uri.startsWith(DEFAULT_NAMESPACE)) {
			return uri;
		}
		String remainder = uri.substring(DEFAULT_NAMESPACE.length());
		return FILE_PATH + remainder + "/" + filename;
	}

	/** No need for instances because all of the methods are static. */
	private FileServingHelper() {
		// nothing to instantiate.
	}

}
