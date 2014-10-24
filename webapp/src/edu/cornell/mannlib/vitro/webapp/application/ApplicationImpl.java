/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.application;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.filestorage.impl.FileStorageImplWrapper;
import edu.cornell.mannlib.vitro.webapp.imageprocessor.jai.JaiImageProcessor;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileStorage;
import edu.cornell.mannlib.vitro.webapp.modules.imageProcessor.ImageProcessor;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.searchengine.InstrumentedSearchEngineWrapper;
import edu.cornell.mannlib.vitro.webapp.searchengine.solr.SolrSearchEngine;
import edu.cornell.mannlib.vitro.webapp.startup.ComponentStartupStatusImpl;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * The basic implementation of the Application interface.
 */
public class ApplicationImpl implements Application {
	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	private final ServletContext ctx;
	private SearchEngine searchEngine;
	private ImageProcessor imageProcessor;
	private FileStorage fileStorage;

	public ApplicationImpl(ServletContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public ServletContext getServletContext() {
		return ctx;
	}

	@Override
	public SearchEngine getSearchEngine() {
		return searchEngine;
	}

	public void setSearchEngine(SearchEngine searchEngine) {
		this.searchEngine = searchEngine;
	}

	@Override
	public ImageProcessor getImageProcessor() {
		return imageProcessor;
	}

	public void setImageProcessor(ImageProcessor imageProcessor) {
		this.imageProcessor = imageProcessor;
	}

	@Override
	public FileStorage getFileStorage() {
		return fileStorage;
	}

	public void setFileStorage(FileStorage fileStorage) {
		this.fileStorage = fileStorage;
	}

	// ----------------------------------------------------------------------
	// The Setup class.
	// ----------------------------------------------------------------------

	public static class Setup implements ServletContextListener {
		private ApplicationImpl application;

		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ServletContext ctx = sce.getServletContext();
			StartupStatus ss = StartupStatus.getBean(ctx);

			try {
				application = new ApplicationImpl(ctx);
				
				ComponentStartupStatus css = new ComponentStartupStatusImpl(
						this, ss);

				SearchEngine searchEngine = new InstrumentedSearchEngineWrapper(
						new SolrSearchEngine());
				searchEngine.startup(application, css);
				application.setSearchEngine(searchEngine);
				ss.info(this, "Started the searchEngine: " + searchEngine);

				ImageProcessor imageProcessor = new JaiImageProcessor();
				imageProcessor.startup(application, css);
				application.setImageProcessor(imageProcessor);
				ss.info(this, "Started the ImageProcessor: " + searchEngine);

				FileStorage fileStorage = new FileStorageImplWrapper();
				fileStorage.startup(application, css);
				application.setFileStorage(fileStorage);
				ss.info(this, "Started the FileStorage system: " + searchEngine);

				ApplicationUtils.setInstance(application);
				ss.info(this, "Appliation is configured.");
			} catch (Exception e) {
				ss.fatal(this, "Failed to initialize the Application.", e);
			}
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			application.getFileStorage().shutdown(application);
			application.getImageProcessor().shutdown(application);
			application.getSearchEngine().shutdown(application);
		}

	}
}
