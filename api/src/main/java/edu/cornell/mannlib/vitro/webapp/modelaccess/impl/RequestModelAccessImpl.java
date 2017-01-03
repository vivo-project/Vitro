/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.impl;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption.LANGUAGE_AWARE;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption.LANGUAGE_NEUTRAL;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption.POLICY_AWARE;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.APPLICATION_METADATA;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.USER_ACCOUNTS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.HideFromDisplayByPolicyFilter;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelectorImpl;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;
import edu.cornell.mannlib.vitro.webapp.filters.ModelSwitcher;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.DatasetOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.OntModelSelectorOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.RdfServiceOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WebappDaoFactoryOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys.DatasetKey;
import edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys.OntModelKey;
import edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys.OntModelSelectorKey;
import edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys.RDFServiceKey;
import edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys.WebappDaoFactoryKey;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringRDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringUtils;
import edu.cornell.mannlib.vitro.webapp.triplesource.ShortTermCombinedTripleSource;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * For each category of data structure, the pattern is the same:
 * 
 * Build a key from the supplied options. If the cache contains a structure with
 * that key, return it.
 * 
 * Otherwise, create the requested structure, often from a simplified version.
 * For example, if the request was for a LANGUAGE_AWARE RDFService, get the
 * LANGUAGE_NEUTRAL RDFService and wrap it with langauge awareness.
 * 
 * This second step is a recursive call, so we check again to see if the cache
 * contains the new requested structure based on a modified key. If it does
 * contain the structure, use it. Otherwise, create it. Eventually, we reach a
 * request for the simplest structure, which we fetch from the
 * ShortTermDataStructuresFactory.
 * 
 * If the request includes language-awareness, the structure can be created from
 * the language-neutral version. If language-awareness is disabled, the
 * language-neutral structure will be cached under two different keys.
 * 
 * ----------------------------------------
 * 
 * There are two hacks here to support model switching: one in the OntModels and
 * one in the WebappDaoFactories. These are hacks for several reasons, not the
 * least of which is that the model switching will not be available on
 * WebappDaoFactories based on ASSERTIONS_ONLY or INFERENCES_ONLY, and similarly
 * for the OntModel.
 */
public class RequestModelAccessImpl implements RequestModelAccess {
	private static final Log log = LogFactory
			.getLog(RequestModelAccessImpl.class);

	private final HttpServletRequest req;
	private final ServletContext ctx;
	private final ConfigurationProperties props;
	private final ShortTermCombinedTripleSource source;

	public RequestModelAccessImpl(HttpServletRequest req,
			ShortTermCombinedTripleSource source) {
		this.req = req;
		this.ctx = req.getSession().getServletContext();
		this.props = ConfigurationProperties.getBean(req);
		this.source = source;
	}

	/**
	 * Language awareness is disabled unless they explicitly enable it.
	 */
	private Boolean isLanguageAwarenessEnabled() {
		return Boolean.valueOf(props.getProperty("RDFService.languageFilter",
				"false"));
	}

	private List<String> getPreferredLanguages() {
		return LanguageFilteringUtils.localesToLanguages(req.getLocales());
	}

	@Override
	public void close() {
		this.source.close();
	}

	@Override
	public String toString() {
		return "RequestModelAccessImpl[" + ToString.hashHex(this) + ", req="
				+ ToString.hashHex(req) + ", source=" + source + "]";
	}

	// ----------------------------------------------------------------------
	// RDFServices
	// ----------------------------------------------------------------------

	private final Map<RDFServiceKey, RDFService> rdfServiceMap = new HashMap<>();

	@Override
	public RDFService getRDFService(RdfServiceOption... options) {
		return getRDFService(new RDFServiceKey(options));
	}

	private RDFService getRDFService(RDFServiceKey key) {
		if (!rdfServiceMap.containsKey(key)) {
			RDFService rdfService = createRDFService(key);
			log.debug("Creating:   " + key + ", request=" + req.hashCode()
					+ ", " + rdfService);
			rdfServiceMap.put(key, rdfService);
		}
		RDFService rdfService = rdfServiceMap.get(key);
		log.debug("getRDFService, " + key + ": " + rdfService);
		return rdfService;
	}

	private RDFService createRDFService(RDFServiceKey key) {
		if (key.getLanguageOption() == LANGUAGE_AWARE) {
			return addLanguageAwareness(getRDFService(LANGUAGE_NEUTRAL));
		} else {
			return source.getRDFService(key.getWhichService());
		}
	}

	private RDFService addLanguageAwareness(RDFService unaware) {
		if (isLanguageAwarenessEnabled()) {
			return new LanguageFilteringRDFService(unaware,
					getPreferredLanguages());
		} else {
			return unaware;
		}
	}

	// ----------------------------------------------------------------------
	// Datasets
	// ----------------------------------------------------------------------

	private final Map<DatasetKey, Dataset> datasetMap = new HashMap<>();

	@Override
	public Dataset getDataset(DatasetOption... options) {
		return getDataset(new DatasetKey(options));
	}

	private Dataset getDataset(DatasetKey key) {
		if (!datasetMap.containsKey(key)) {
			Dataset dataset = createDataset(key);
			log.debug("Creating:   " + key + ", request=" + req.hashCode()
					+ ", " + dataset);
			datasetMap.put(key, dataset);
		}
		Dataset dataset = datasetMap.get(key);
		log.debug("getDataset, " + key + ": " + dataset);
		return dataset;
	}

	private Dataset createDataset(DatasetKey key) {
		return new RDFServiceDataset(getRDFService(key.rdfServiceKey()));
	}

	// ----------------------------------------------------------------------
	// OntModels
	// ----------------------------------------------------------------------

	private final Map<OntModelKey, OntModel> ontModelMap = new HashMap<>();

	@Override
	public OntModel getOntModel(LanguageOption... options) {
		return getOntModel(ModelNames.FULL_UNION, options);
	}

	@Override
	public OntModel getOntModel(String name, LanguageOption... options) {
		return getOntModel(new OntModelKey(name, options));
	}

	private OntModel getOntModel(OntModelKey key) {
		if (!ontModelMap.containsKey(key)) {
			OntModel ontModel = createOntModel(key);
			if ( log.isDebugEnabled() ) {
				String ontModelStr = ToString.ontModelToString(ontModel);
				log.debug("Creating:   " + key + ", request=" + req.hashCode()
						+ ", " + ontModelStr);
			}
			ontModelMap.put(key, ontModel);
		}
		OntModel ontModel = ontModelMap.get(key);
		if ( log.isDebugEnabled() ) {
			String ontModelStr = ToString.ontModelToString(ontModel);
			log.debug("getOntModel, " + key + ": " + ontModelStr);
		}
		return ontModel;
	}

	private OntModel createOntModel(OntModelKey key) {
		if (key.getLanguageOption() == LANGUAGE_AWARE) {
			return addLanguageAwareness(getOntModel(key.getName(),
					LANGUAGE_NEUTRAL));
		} else {
			return source.getOntModelCache().getOntModel(key.getName());
		}
	}

	private OntModel addLanguageAwareness(OntModel unaware) {
		if (isLanguageAwarenessEnabled()) {
			return LanguageFilteringUtils.wrapOntModelInALanguageFilter(
					unaware, req);
		} else {
			return unaware;
		}
	}

	/**
	 * TODO Hack for model switching.
	 */
	public void setSpecialWriteModel(OntModel mainOntModel) {
		ontModelMap.put(new OntModelKey(ModelNames.FULL_UNION), mainOntModel);
	}

	// ----------------------------------------------------------------------
	// OntModelSelectors
	// ----------------------------------------------------------------------

	private final Map<OntModelSelectorKey, OntModelSelector> ontModelSelectorMap = new HashMap<>();

	@Override
	public OntModelSelector getOntModelSelector(
			OntModelSelectorOption... options) {
		return getOntModelSelector(new OntModelSelectorKey(options));
	}

	private OntModelSelector getOntModelSelector(OntModelSelectorKey key) {
		if (!ontModelSelectorMap.containsKey(key)) {
			OntModelSelector oms = createOntModelSelector(key);
			log.debug("Creating:   " + key + ", request=" + req.hashCode()
					+ ", " + oms);
			ontModelSelectorMap.put(key, oms);
		}
		OntModelSelector ontModelSelector = ontModelSelectorMap.get(key);
		log.debug("getOntModelSelector, " + key + ": " + ontModelSelector);
		return ontModelSelector;
	}

	private OntModelSelector createOntModelSelector(OntModelSelectorKey key) {
		OntModelSelectorImpl oms = new OntModelSelectorImpl();

		oms.setABoxModel(getOntModel(key.aboxKey()));
		oms.setTBoxModel(getOntModel(key.tboxKey()));
		oms.setFullModel(getOntModel(key.fullKey()));

		oms.setApplicationMetadataModel(getOntModel(key
				.ontModelKey(APPLICATION_METADATA)));
		oms.setDisplayModel(getOntModel(key.ontModelKey(DISPLAY)));
		oms.setUserAccountsModel(getOntModel(key.ontModelKey(USER_ACCOUNTS)));

		return oms;
	}

	// ----------------------------------------------------------------------
	// WebappDaoFactories
	// ----------------------------------------------------------------------

	private final Map<WebappDaoFactoryKey, WebappDaoFactory> wadfMap = new HashMap<>();

	@Override
	public WebappDaoFactory getWebappDaoFactory(
			WebappDaoFactoryOption... options) {
		return getWebappDaoFactory(new WebappDaoFactoryKey(options));
	}

	private WebappDaoFactory getWebappDaoFactory(WebappDaoFactoryKey key) {
		if (!wadfMap.containsKey(key)) {
			WebappDaoFactory wadf = createWebappDaoFactory(key);
			log.debug("Creating:   " + key + ", request=" + req.hashCode()
					+ ", " + wadf);
			wadfMap.put(key, wadf);
		}
		WebappDaoFactory wadf = wadfMap.get(key);
		log.debug("getWebappDaoFactory, " + key + ": " + wadf);
		return wadf;
	}

	private WebappDaoFactory createWebappDaoFactory(WebappDaoFactoryKey key) {
		if (key.getPolicyOption() == POLICY_AWARE) {
			return addPolicyAwareness(getWebappDaoFactory(key.policyNeutral()));
		}

		RDFService rdfService = getRDFService(key.rdfServiceKey());
		OntModelSelector ontModelSelector = getOntModelSelector(key
				.ontModelSelectorKey());
		WebappDaoFactoryConfig config = source.getWebappDaoFactoryConfig();

		switch (key.getReasoningOption()) {
		case ASSERTIONS_ONLY:
			return new WebappDaoFactorySDB(rdfService, ontModelSelector,
					config, SDBDatasetMode.ASSERTIONS_ONLY);
		case INFERENCES_ONLY:
			return new WebappDaoFactorySDB(rdfService, ontModelSelector,
					config, SDBDatasetMode.INFERENCES_ONLY);
		default: // ASSERTIONS_AND_INFERENCES
			// TODO Do model switching and replace the WebappDaoFactory with
			// a different version if requested by parameters
			WebappDaoFactory unswitched = new WebappDaoFactorySDB(rdfService,
					ontModelSelector, config);
			return new ModelSwitcher().checkForModelSwitching(new VitroRequest(
					req), unswitched);
		}
	}

	private WebappDaoFactory addPolicyAwareness(WebappDaoFactory unaware) {
		HideFromDisplayByPolicyFilter filter = new HideFromDisplayByPolicyFilter(
				RequestIdentifiers.getIdBundleForRequest(req),
				ServletPolicyList.getPolicies(ctx));
		return new WebappDaoFactoryFiltering(unaware, filter);
	}

}
