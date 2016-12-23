/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.impl;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption.ASSERTIONS_AND_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption.ASSERTIONS_ONLY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption.INFERENCES_ONLY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONFIGURATION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONTENT;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_UNION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.APPLICATION_METADATA;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_UNION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.USER_ACCOUNTS;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelectorImpl;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.triplesource.CombinedTripleSource;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * The context-based model access is simpler than the request-based model
 * access.
 * 
 * There is no "current user", so there can be no preferred language or
 * policy-based filtering.
 * 
 * We are confident that each of these structures will be used at some point, so
 * there is no need for lazy initialization.
 */
public class ContextModelAccessImpl implements ContextModelAccess {
	private static final Log log = LogFactory
			.getLog(ContextModelAccessImpl.class);

	private static final String VITRO_DEFAULT_NAMESPACE = "Vitro.defaultNamespace";

	private final ConfigurationProperties props;

	private final CombinedTripleSource factory;

	private final Map<WhichService, RDFService> rdfServiceMap;
	private final Map<WhichService, Dataset> datasetMap;
	private final Map<WhichService, ModelMaker> modelMakerMap;
	private final OntModelCache ontModelCache;
	private final Map<ReasoningOption, OntModelSelector> omsMap;
	private final Map<ReasoningOption, WebappDaoFactory> wadfMap;

	/**
	 * Pull all of the items into maps, instead of fetching them from the
	 * factory on demand. We don't know that the factory wouldn't create fresh
	 * ones.
	 * 
	 * The exception is the OntModels, for two reasons: first, because the
	 * OntModelCache assures us that it won't create fresh copies, and second
	 * because we may not know the names of all of the models that will be
	 * requested.
	 */
	public ContextModelAccessImpl(ServletContext ctx,
			CombinedTripleSource factory) {
		this.props = ConfigurationProperties.getBean(ctx);
		this.factory = factory;

		this.ontModelCache = factory.getOntModelCache();
		this.rdfServiceMap = populateRdfServiceMap();
		this.datasetMap = populateDatasetMap();
		this.modelMakerMap = populateModelMakerMap();
		this.omsMap = populateOmsMap();
		this.wadfMap = populateWadfMap();
	}

	@Override
	public String toString() {
		return "ContextModelAccessImpl[" + ToString.hashHex(this)
				+ ", factory=" + factory + "]";
	}

	// ----------------------------------------------------------------------
	// RDFServices
	// ----------------------------------------------------------------------

	private Map<WhichService, RDFService> populateRdfServiceMap() {
		Map<WhichService, RDFService> map = new EnumMap<>(WhichService.class);
		map.put(CONTENT, factory.getRDFService(CONTENT));
		map.put(CONFIGURATION, factory.getRDFService(CONFIGURATION));
		log.debug("RdfServiceMap: " + map);
		return Collections.unmodifiableMap(map);
	}

	@Override
	public RDFService getRDFService() {
		return getRDFService(CONTENT);
	}

	@Override
	public RDFService getRDFService(WhichService which) {
		RDFService rdfService = rdfServiceMap.get(which);
		log.debug("getRDFService " + which + ": " + rdfService);
		return rdfService;
	}

	// ----------------------------------------------------------------------
	// Datasets
	// ----------------------------------------------------------------------

	private Map<WhichService, Dataset> populateDatasetMap() {
		Map<WhichService, Dataset> map = new EnumMap<>(WhichService.class);
		map.put(CONTENT, factory.getDataset(CONTENT));
		map.put(CONFIGURATION, factory.getDataset(CONFIGURATION));
		log.debug("DatasetMap: " + map);
		return Collections.unmodifiableMap(map);
	}

	@Override
	public Dataset getDataset() {
		return getDataset(CONTENT);
	}

	@Override
	public Dataset getDataset(WhichService which) {
		Dataset dataset = datasetMap.get(which);
		log.debug("getDataset " + which + ": " + dataset);
		return dataset;
	}

	// ----------------------------------------------------------------------
	// ModelMakers
	// ----------------------------------------------------------------------

	private Map<WhichService, ModelMaker> populateModelMakerMap() {
		Map<WhichService, ModelMaker> map = new EnumMap<>(WhichService.class);
		map.put(CONTENT, factory.getModelMaker(CONTENT));
		map.put(CONFIGURATION, factory.getModelMaker(CONFIGURATION));
		log.debug("ModelMakerMap: " + map);
		return Collections.unmodifiableMap(map);
	}

	@Override
	public ModelMaker getModelMaker() {
		return getModelMaker(CONTENT);
	}

	@Override
	public ModelMaker getModelMaker(WhichService which) {
		ModelMaker modelMaker = modelMakerMap.get(which);
		log.debug("getModelMaker " + which + ": " + modelMaker);
		return modelMaker;
	}

	// ----------------------------------------------------------------------
	// OntModels
	// ----------------------------------------------------------------------

	@Override
	public OntModel getOntModel() {
		return getOntModel(FULL_UNION);
	}

	@Override
	public OntModel getOntModel(String name) {
		OntModel ontModel = ontModelCache.getOntModel(name);
		log.debug("getOntModel: " + ontModel);
		return ontModel;
	}

	// ----------------------------------------------------------------------
	// OntModelSelectors
	// ----------------------------------------------------------------------

	private Map<ReasoningOption, OntModelSelector> populateOmsMap() {
		Map<ReasoningOption, OntModelSelector> map = new EnumMap<>(
				ReasoningOption.class);
		map.put(ASSERTIONS_ONLY,
				createOntModelSelector(ABOX_ASSERTIONS, TBOX_ASSERTIONS,
						FULL_ASSERTIONS));
		map.put(INFERENCES_ONLY,
				createOntModelSelector(ABOX_INFERENCES, TBOX_INFERENCES,
						FULL_INFERENCES));
		map.put(ASSERTIONS_AND_INFERENCES,
				createOntModelSelector(ABOX_UNION, TBOX_UNION, FULL_UNION));
		log.debug("OntModelSelectorMap: " + map);
		return Collections.unmodifiableMap(map);
	}

	private OntModelSelector createOntModelSelector(String aboxName,
			String tboxName, String fullName) {
		OntModelSelectorImpl oms = new OntModelSelectorImpl();
		oms.setABoxModel(getOntModel(aboxName));
		oms.setTBoxModel(getOntModel(tboxName));
		oms.setFullModel(getOntModel(fullName));
		oms.setApplicationMetadataModel(getOntModel(APPLICATION_METADATA));
		oms.setDisplayModel(getOntModel(DISPLAY));
		oms.setUserAccountsModel(getOntModel(USER_ACCOUNTS));
		return oms;
	}

	@Override
	public OntModelSelector getOntModelSelector() {
		return getOntModelSelector(ASSERTIONS_AND_INFERENCES);
	}

	@Override
	public OntModelSelector getOntModelSelector(ReasoningOption option) {
		OntModelSelector ontModelSelector = omsMap.get(option);
		log.debug("getOntModelSelector: " + ontModelSelector);
		return ontModelSelector;
	}

	// ----------------------------------------------------------------------
	// WebappDaoFactories
	// ----------------------------------------------------------------------

	private Map<ReasoningOption, WebappDaoFactory> populateWadfMap() {
		WebappDaoFactoryConfig config = new WebappDaoFactoryConfig();
		config.setDefaultNamespace(getDefaultNamespace());

		RDFService rdfService = getRDFService(CONTENT);

		Map<ReasoningOption, WebappDaoFactory> map = new EnumMap<>(
				ReasoningOption.class);
		map.put(ASSERTIONS_ONLY, new WebappDaoFactorySDB(rdfService,
				getOntModelSelector(ASSERTIONS_ONLY), config,
				SDBDatasetMode.ASSERTIONS_ONLY));
		map.put(INFERENCES_ONLY, new WebappDaoFactorySDB(rdfService,
				getOntModelSelector(INFERENCES_ONLY), config,
				SDBDatasetMode.INFERENCES_ONLY));
		map.put(ASSERTIONS_AND_INFERENCES, new WebappDaoFactorySDB(rdfService,
				getOntModelSelector(ASSERTIONS_AND_INFERENCES), config,
				SDBDatasetMode.ASSERTIONS_AND_INFERENCES));
		log.debug("WebappdaoFactoryMap: " + map);
		return Collections.unmodifiableMap(map);
	}

	private String getDefaultNamespace() {
		return props.getProperty(VITRO_DEFAULT_NAMESPACE);
	}

	@Override
	public WebappDaoFactory getWebappDaoFactory() {
		return getWebappDaoFactory(ASSERTIONS_AND_INFERENCES);
	}

	@Override
	public WebappDaoFactory getWebappDaoFactory(ReasoningOption option) {
		WebappDaoFactory wadf = wadfMap.get(option);
		log.debug("getWebappDaoFactory: " + wadf);
		return wadf;
	}

}
