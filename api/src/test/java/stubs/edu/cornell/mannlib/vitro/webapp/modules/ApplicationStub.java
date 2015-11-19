/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.modules;

import java.lang.reflect.Field;

import javax.servlet.ServletContext;

import stubs.edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStub;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.application.VitroHomeDirectory;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileStorage;
import edu.cornell.mannlib.vitro.webapp.modules.imageProcessor.ImageProcessor;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner.TBoxReasonerModule;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ConfigurationTripleSource;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ContentTripleSource;

/**
 * TODO
 */
public class ApplicationStub implements Application {
	/**
	 * Create an ApplicationStub and set it as the instance in ApplicationUtils.
	 */
	public static void setup(ServletContext ctx, SearchEngine searchEngine) {
		ApplicationStub instance = new ApplicationStub(ctx, searchEngine);
		try {
			Field instanceField = ApplicationUtils.class
					.getDeclaredField("instance");
			instanceField.setAccessible(true);
			instanceField.set(null, instance);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final ServletContext ctx;
	private final SearchEngine searchEngine;
	private final SearchIndexer searchIndexer;

	public ApplicationStub(ServletContext ctx, SearchEngine searchEngine) {
		this.ctx = ctx;
		this.searchEngine = searchEngine;
		
		this.searchIndexer = new SearchIndexerStub();
		this.searchIndexer.unpause();
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public ServletContext getServletContext() {
		return ctx;
	}

	@Override
	public SearchEngine getSearchEngine() {
		return searchEngine;
	}

	@Override
	public SearchIndexer getSearchIndexer() {
		return searchIndexer;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public VitroHomeDirectory getHomeDirectory() {
		throw new RuntimeException(
				"ApplicationStub.getHomeDirectory() not implemented.");
	}

	@Override
	public ImageProcessor getImageProcessor() {
		throw new RuntimeException(
				"ApplicationStub.getImageProcessor() not implemented.");

	}

	@Override
	public FileStorage getFileStorage() {
		throw new RuntimeException(
				"ApplicationStub.getFileStorage() not implemented.");
	}

	@Override
	public void shutdown() {
		throw new RuntimeException(
				"ApplicationStub.shutdown() not implemented.");
	}

	@Override
	public ContentTripleSource getContentTripleSource() {
		throw new RuntimeException(
				"ApplicationStub.getContentTripleSource() not implemented.");
	}

	@Override
	public ConfigurationTripleSource getConfigurationTripleSource() {
		throw new RuntimeException(
				"ApplicationStub.getConfigurationTripleSource() not implemented.");
	}

	@Override
	public TBoxReasonerModule getTBoxReasonerModule() {
		throw new RuntimeException(
				"ApplicationStub.getTBoxReasonerModule() not implemented.");
	}

}
