/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.application.VitroHomeDirectory;
import edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileStorage;
import edu.cornell.mannlib.vitro.webapp.modules.imageProcessor.ImageProcessor;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner.TBoxReasonerModule;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ConfigurationTripleSource;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ContentTripleSource;

/**
 * The interface that holds the modules and extensions together.
 */
public interface Application {
	ServletContext getServletContext();

	VitroHomeDirectory getHomeDirectory();

	SearchEngine getSearchEngine();

	SearchIndexer getSearchIndexer();

	ImageProcessor getImageProcessor();

	FileStorage getFileStorage();

	ContentTripleSource getContentTripleSource();

	ConfigurationTripleSource getConfigurationTripleSource();

	TBoxReasonerModule getTBoxReasonerModule();

	void shutdown();

	public interface Component {
		enum LifecycleState {
			NEW, ACTIVE, STOPPED
		}

		/**
		 * This should be called only once, and should be the first call on this
		 * Component.
		 */
		void startup(Application application, ComponentStartupStatus ss);

		/**
		 * This should be called only once, and should be the last call on this
		 * Component.
		 */
		void shutdown(Application application);
	}

	public static interface Module extends Component {
		// Nothing except lifecycle so far.
	}

	public static interface Extension extends Component {
		// Nothing except lifecycle so far.
	}

}
