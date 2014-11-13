/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilterUtils;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.search.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.search.beans.StatementToURIsToUpdate;
import edu.cornell.mannlib.vitro.webapp.search.documentBuilding.DocumentModifier;
import edu.cornell.mannlib.vitro.webapp.search.documentBuilding.IndividualToSearchDocument;
import edu.cornell.mannlib.vitro.webapp.search.documentBuilding.SearchIndexExcluder;
import edu.cornell.mannlib.vitro.webapp.search.indexing.AdditionalUriFinders;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;
import edu.cornell.mannlib.vitro.webapp.search.indexing.SearchReindexingListener;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;
import edu.cornell.mannlib.vitro.webapp.utils.developer.listeners.DeveloperDisabledModelChangeListener;

/**
 * TODO
 */
public class SearchIndexerSetup implements ServletContextListener {
	private static final Log log = LogFactory.getLog(SearchIndexerSetup.class);
	
	public static final String PROHIBITED_FROM_SEARCH = "edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch";
	
	private ServletContext ctx;
	private OntModel displayModel;
	private ConfigurationBeanLoader beanLoader;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		this.ctx = sce.getServletContext();
		this.displayModel = ModelAccess.on(ctx).getOntModel(DISPLAY);
		this.beanLoader = new ConfigurationBeanLoader(displayModel, ctx);
		
		ServletContext context = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(context);
		SearchEngine searchEngine = ApplicationUtils.instance().getSearchEngine();

		try {
			IndividualToSearchDocument indToSearchDoc = setupTranslation();

			/* setup search indexer */
			SearchIndexer searchIndexer = new SearchIndexer(searchEngine, indToSearchDoc);

			// This is where the builder gets the list of places to try to
			// get objects to index. It is filtered so that non-public text
			// does not get into the search index.
			WebappDaoFactory wadf = ModelAccess.on(context)
					.getWebappDaoFactory();
			VitroFilters vf = VitroFilterUtils.getPublicFilter(context);
			wadf = new WebappDaoFactoryFiltering(wadf, vf);

			// make objects that will find additional URIs for context nodes etc
			RDFService rdfService = RDFServiceUtils.getRDFServiceFactory(
					context).getRDFService();
			List<StatementToURIsToUpdate> uriFinders = AdditionalUriFinders
					.getList(rdfService, wadf.getIndividualDao());

			// Make the IndexBuilder
			IndexBuilder builder = new IndexBuilder(searchIndexer, wadf,
					uriFinders);
			// Save it to the servlet context so we can access it later in the
			// webapp.
			context.setAttribute(IndexBuilder.class.getName(), builder);

			// Create listener to notify index builder of changes to model
			// (can be disabled by developer setting.)
			ModelContext.registerListenerForChanges(context,
					new DeveloperDisabledModelChangeListener(
							new SearchReindexingListener(builder),
							Key.SEARCH_INDEX_SUPPRESS_MODEL_CHANGE_LISTENER));

			ss.info(this, "Setup of search indexer completed.");
		} catch (Throwable e) {
			ss.fatal(this, "could not setup search engine", e);
		}

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		IndexBuilder builder = (IndexBuilder) sce.getServletContext()
				.getAttribute(IndexBuilder.class.getName());
		if (builder != null)
			builder.stopIndexingThread();

	}

	private IndividualToSearchDocument setupTranslation() {
		try {
			Set<SearchIndexExcluder> excluders = beanLoader.loadAll(SearchIndexExcluder.class);
			log.debug("Excludes: (" + excluders.size() + ") " + excluders);

			Set<DocumentModifier> modifiers = beanLoader.loadAll(DocumentModifier.class);
			log.debug("Modifiers: (" + modifiers.size() + ") " + modifiers);
			
			return new IndividualToSearchDocument(new ArrayList<>(excluders), new ArrayList<>(modifiers));
		} catch (ConfigurationBeanLoaderException e) {
			throw new RuntimeException("Failed to configure the SearchIndexer", e);
		}
	}
}
