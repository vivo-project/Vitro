/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.application;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONFIGURATION;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.jena.ontology.OntDocumentManager;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileStorage;
import edu.cornell.mannlib.vitro.webapp.modules.imageProcessor.ImageProcessor;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner.TBoxReasonerModule;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ConfigurationTripleSource;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ContentTripleSource;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.startup.ComponentStartupStatusImpl;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.triplesource.impl.BasicCombinedTripleSource;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * The basic implementation of the Application interface.
 */
public class ApplicationImpl implements Application {
	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	private ServletContext ctx;
	private VitroHomeDirectory homeDirectory;

	private SearchEngine searchEngine;
	private SearchIndexer searchIndexer;
	private ImageProcessor imageProcessor;
	private FileStorage fileStorage;
	private ContentTripleSource contentTripleSource;
	private ConfigurationTripleSource configurationTripleSource;
	private TBoxReasonerModule tboxReasonerModule;

	public void setServletContext(ServletContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public ServletContext getServletContext() {
		return ctx;
	}

	@Override
	public VitroHomeDirectory getHomeDirectory() {
		return homeDirectory;
	}

	public void setHomeDirectory(VitroHomeDirectory homeDirectory) {
		this.homeDirectory = homeDirectory;
	}

	@Override
	public SearchEngine getSearchEngine() {
		return searchEngine;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasSearchEngine", minOccurs = 1, maxOccurs = 1)
	public void setSearchEngine(SearchEngine se) {
		searchEngine = se;
	}

	@Override
	public SearchIndexer getSearchIndexer() {
		return searchIndexer;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasSearchIndexer", minOccurs = 1, maxOccurs = 1)
	public void setSearchIndexer(SearchIndexer si) {
		searchIndexer = si;
	}

	@Override
	public ImageProcessor getImageProcessor() {
		return imageProcessor;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasImageProcessor", minOccurs = 1, maxOccurs = 1)
	public void setImageProcessor(ImageProcessor ip) {
		imageProcessor = ip;
	}

	@Override
	public FileStorage getFileStorage() {
		return fileStorage;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasFileStorage", minOccurs = 1, maxOccurs = 1)
	public void setFileStorage(FileStorage fs) {
		fileStorage = fs;
	}

	@Override
	public ContentTripleSource getContentTripleSource() {
		return contentTripleSource;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasContentTripleSource", minOccurs = 1, maxOccurs = 1)
	public void setContentTripleSource(ContentTripleSource source) {
		contentTripleSource = source;
	}

	@Override
	public ConfigurationTripleSource getConfigurationTripleSource() {
		return configurationTripleSource;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasConfigurationTripleSource", minOccurs = 1, maxOccurs = 1)
	public void setConfigurationTripleSource(ConfigurationTripleSource source) {
		configurationTripleSource = source;
	}

	@Override
	public TBoxReasonerModule getTBoxReasonerModule() {
		return tboxReasonerModule;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasTBoxReasonerModule", minOccurs = 1, maxOccurs = 1)
	public void setTBoxReasonerModule(TBoxReasonerModule module) {
		tboxReasonerModule = module;
	}

	@Override
	public void shutdown() {
		// Nothing to do.
	}

	// ----------------------------------------------------------------------
	// Setup the major components.
	//
	// This must happen after the ConfigurationProperties and some other stuff.
	// ----------------------------------------------------------------------

	public static class ComponentsSetup implements ServletContextListener {
		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ServletContext ctx = sce.getServletContext();
			Application app = ApplicationUtils.instance();
			StartupStatus ss = StartupStatus.getBean(ctx);
			ComponentStartupStatus css = new ComponentStartupStatusImpl(this,
					ss);

			SearchEngine searchEngine = app.getSearchEngine();
			searchEngine.startup(app, css);
			ss.info(this, "Started the SearchEngine: " + searchEngine);

			ImageProcessor imageProcessor = app.getImageProcessor();
			imageProcessor.startup(app, css);
			ss.info(this, "Started the ImageProcessor: " + imageProcessor);

			FileStorage fileStorage = app.getFileStorage();
			fileStorage.startup(app, css);
			ss.info(this, "Started the FileStorage system: " + fileStorage);

			ContentTripleSource contentTripleSource = app
					.getContentTripleSource();
			contentTripleSource.startup(app, css);
			ss.info(this, "Started the ContentTripleSource: "
					+ contentTripleSource);

			ConfigurationTripleSource configurationTripleSource = app
					.getConfigurationTripleSource();
			configurationTripleSource.startup(app, css);
			ss.info(this, "Started the ConfigurationTripleSource: "
					+ configurationTripleSource);

			configureJena();
			prepareCombinedTripleSource(app, ctx);
		}

		private void configureJena() {
			// we do not want to fetch imports when we wrap Models in OntModels
			OntDocumentManager.getInstance().setProcessImports(false);
		}

		private void prepareCombinedTripleSource(Application app,
				ServletContext ctx) {
			ContentTripleSource contentSource = app.getContentTripleSource();
			ConfigurationTripleSource configurationSource = app
					.getConfigurationTripleSource();
			BasicCombinedTripleSource source = new BasicCombinedTripleSource(
					contentSource, configurationSource);

			RDFServiceUtils.setRDFServiceFactory(ctx,
					contentSource.getRDFServiceFactory());
			RDFServiceUtils.setRDFServiceFactory(ctx,
					configurationSource.getRDFServiceFactory(), CONFIGURATION);

			ModelAccess.setCombinedTripleSource(source);
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			Application app = ApplicationUtils.instance();
			app.getConfigurationTripleSource().shutdown(app);
			app.getContentTripleSource().shutdown(app);
			app.getFileStorage().shutdown(app);
			app.getImageProcessor().shutdown(app);
			app.getSearchEngine().shutdown(app);
		}
	}

	// ----------------------------------------------------------------------
	// Setup the reasoners.
	//
	// This must happen after the FileGraphSetup.
	// ----------------------------------------------------------------------

	public static class ReasonersSetup implements ServletContextListener {
		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ServletContext ctx = sce.getServletContext();
			Application app = ApplicationUtils.instance();
			StartupStatus ss = StartupStatus.getBean(ctx);
			ComponentStartupStatus css = new ComponentStartupStatusImpl(this,
					ss);

			TBoxReasonerModule tboxReasoner = app.getTBoxReasonerModule();
			tboxReasoner.startup(app, css);
			ss.info(this, "Started the TBoxReasonerModule: " + tboxReasoner);
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			Application app = ApplicationUtils.instance();
			app.getTBoxReasonerModule().shutdown(app);
		}
	}
}
