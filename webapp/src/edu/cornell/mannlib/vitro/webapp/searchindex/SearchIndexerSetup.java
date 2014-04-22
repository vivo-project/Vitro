/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.solr.client.solrj.impl.HttpSolrServer;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilterUtils;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.search.beans.StatementToURIsToUpdate;
import edu.cornell.mannlib.vitro.webapp.search.indexing.AdditionalUriFinders;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;
import edu.cornell.mannlib.vitro.webapp.search.indexing.SearchReindexingListener;
import edu.cornell.mannlib.vitro.webapp.search.solr.SolrIndexer;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.DocumentModifier;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.ExcludeBasedOnNamespace;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.ExcludeBasedOnType;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.ExcludeBasedOnTypeNamespace;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.ExcludeNonFlagVitro;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.IndividualToSolrDocument;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.NameBoost;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.NameFields;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.SearchIndexExcluder;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.SyncingExcludeBasedOnType;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.ThumbnailImageURL;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * TODO
 */
public class SearchIndexerSetup implements ServletContextListener {
	public static final String PROHIBITED_FROM_SEARCH = "edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch";

	/**
	 * Exclude from the search index Individuals with types from these
	 * namespaces
	 */
	private static final String[] TYPE_NS_EXCLUDES = { VitroVocabulary.PUBLIC
	// if you do OWL.NS here you will exclude all of owl:Thing.
	};

	/**
	 * Exclude from the search index individuals who's URIs start with these
	 * namespaces.
	 */
	private static final String[] INDIVIDUAL_NS_EXCLUDES = {
			VitroVocabulary.vitroURI, VitroVocabulary.VITRO_PUBLIC,
			VitroVocabulary.PSEUDO_BNODE_NS, OWL.NS };

	/** Individuals of these types will be excluded from the search index */
	private static final String[] OWL_TYPES_EXCLUDES = {
			OWL.ObjectProperty.getURI(), OWL.DatatypeProperty.getURI(),
			OWL.AnnotationProperty.getURI() };

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(context);
		HttpSolrServer server = (HttpSolrServer) ApplicationUtils.instance().getSearchEngine();

		try {
			/* set up the individual to solr doc translation */
			OntModel jenaOntModel = ModelAccess.on(context).getJenaOntModel();
			OntModel displayModel = ModelAccess.on(context).getDisplayModel();

			/*
			 * try to get context attribute DocumentModifiers and use that as
			 * the start of the list of DocumentModifier objects. This allows
			 * other ContextListeners to add to the basic set of
			 * DocumentModifiers.
			 */
			@SuppressWarnings("unchecked")
			List<DocumentModifier> modifiersFromContext = (List<DocumentModifier>) context
					.getAttribute("DocumentModifiers");

			/*
			 * try to get context attribute SearchIndexExcludes and use that as
			 * the start of the list of exclude objects. This allows other
			 * ContextListeners to add to the basic set of SearchIndexExcludes .
			 */
			@SuppressWarnings("unchecked")
			List<SearchIndexExcluder> searchIndexExcludesFromContext = (List<SearchIndexExcluder>) context
					.getAttribute("SearchIndexExcludes");

			IndividualToSolrDocument indToSolrDoc = setupTransltion(
					jenaOntModel, displayModel,
					RDFServiceUtils.getRDFServiceFactory(context),
					modifiersFromContext, searchIndexExcludesFromContext);

			/* setup solr indexer */
			SolrIndexer solrIndexer = new SolrIndexer(server, indToSolrDoc);

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
			IndexBuilder builder = new IndexBuilder(solrIndexer, wadf,
					uriFinders);
			// Save it to the servlet context so we can access it later in the
			// webapp.
			context.setAttribute(IndexBuilder.class.getName(), builder);

			// set up listeners so search index builder is notified of changes
			// to model
			ServletContext ctx = sce.getServletContext();
			SearchReindexingListener srl = new SearchReindexingListener(builder);
			ModelContext.registerListenerForChanges(ctx, srl);

			ss.info(this, "Setup of Solr index completed.");
		} catch (Throwable e) {
			ss.fatal(this, "could not setup local solr server", e);
		}

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		IndexBuilder builder = (IndexBuilder) sce.getServletContext()
				.getAttribute(IndexBuilder.class.getName());
		if (builder != null)
			builder.stopIndexingThread();

	}

	public static IndividualToSolrDocument setupTransltion(
			OntModel jenaOntModel, Model displayModel,
			RDFServiceFactory rdfServiceFactory,
			List<DocumentModifier> modifiersFromContext,
			List<SearchIndexExcluder> searchIndexExcludesFromContext) {

		/*
		 * try to get context attribute DocumentModifiers and use that as the
		 * start of the list of DocumentModifier objects. This allows other
		 * ContextListeners to add to the basic set of DocumentModifiers.
		 */
		List<DocumentModifier> modifiers = new ArrayList<DocumentModifier>();
		if (modifiersFromContext != null) {
			modifiers.addAll(modifiersFromContext);
		}

		modifiers.add(new NameFields(rdfServiceFactory));
		modifiers.add(new NameBoost(1.2f));
		modifiers.add(new ThumbnailImageURL(rdfServiceFactory));

		/*
		 * try to get context attribute SearchIndexExcludes and use that as the
		 * start of the list of exclude objects. This allows other
		 * ContextListeners to add to the basic set of SearchIndexExcludes .
		 */
		List<SearchIndexExcluder> excludes = new ArrayList<SearchIndexExcluder>();
		if (searchIndexExcludesFromContext != null) {
			excludes.addAll(searchIndexExcludesFromContext);
		}

		excludes.add(new ExcludeBasedOnNamespace(INDIVIDUAL_NS_EXCLUDES));
		excludes.add(new ExcludeBasedOnTypeNamespace(TYPE_NS_EXCLUDES));
		excludes.add(new ExcludeBasedOnType(OWL_TYPES_EXCLUDES));
		excludes.add(new ExcludeNonFlagVitro());
		excludes.add(new SyncingExcludeBasedOnType(displayModel));

		return new IndividualToSolrDocument(excludes, modifiers);
	}
}
