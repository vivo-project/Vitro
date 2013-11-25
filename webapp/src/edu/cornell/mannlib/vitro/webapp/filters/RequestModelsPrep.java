/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import static edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase.JENA_DB_MODEL;
import static edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase.JENA_INF_MODEL;

import java.io.IOException;
import java.text.Collator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openjena.atlas.lib.Pair;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.FactoryID;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.HideFromDisplayByPolicyFilter;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelectorImpl;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SpecialBulkUpdateHandlerGraph;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringRDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringUtils;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

/**
 * This sets up several objects in the Request scope for each incoming HTTP
 * request. This is done in a Filter so that controllers and JSPs get the same
 * setup.
 * 
 * This code configures the WebappDaoFactory for each request.
 */
public class RequestModelsPrep implements Filter {
	private final static Log log = LogFactory.getLog(RequestModelsPrep.class);

	/**
	 * The filter will be applied to all incoming urls, this is a list of URI
	 * patterns to skip. These are matched against the requestURI sans query
	 * parameters, e.g. "/vitro/index.jsp" "/vitro/themes/enhanced/css/edit.css"
	 */
	private final static Pattern[] skipPatterns = {
			Pattern.compile(".*\\.(gif|GIF|jpg|jpeg|png|PNG)$"),
			Pattern.compile(".*\\.css$"), Pattern.compile(".*\\.js$"),
			Pattern.compile("/.*/themes/.*/site_icons/.*"),
			Pattern.compile("/.*/images/.*") };

	private ServletContext ctx;
	private ConfigurationProperties props;
	private String defaultNamespace;

	@Override
	public void init(FilterConfig fc) throws ServletException {
		ctx = fc.getServletContext();
		props = ConfigurationProperties.getBean(ctx);
		defaultNamespace = props.getProperty("Vitro.defaultNamespace");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		// If we're not authorized for this request, skip the chain and
		// redirect.
		if (!ModelSwitcher.authorizedForSpecialModel(req)) {
			VitroHttpServlet.redirectUnauthorizedRequest(req, resp);
			return;
		}

		if (!thisRequestNeedsModels(req) || modelsAreAlreadySetUp(req)) {
			filterChain.doFilter(req, resp);
		} else {
			RDFService rdfService = RDFServiceUtils.getRDFServiceFactory(ctx)
					.getShortTermRDFService();
			try {
				setUpTheRequestModels(rdfService, req);
				filterChain.doFilter(req, resp);
			} finally {
				rdfService.close();
			}
		}
	}

	private boolean thisRequestNeedsModels(HttpServletRequest req) {
		String requestURI = req.getRequestURI();
		for (Pattern skipPattern : skipPatterns) {
			if (skipPattern.matcher(requestURI).matches()) {
				log.debug("request matched skipPattern '" + skipPattern
						+ "', skipping RequestModelsPrep");
				return false;
			}
		}
		return true;
	}

	private boolean modelsAreAlreadySetUp(HttpServletRequest req) {
		String attributeName = RequestModelsPrep.class.getName() + "-setup";
		if (req.getAttribute(attributeName) != null) {
			return true;
		} else {
			req.setAttribute(attributeName, Boolean.TRUE);
			return false;
		}
	}

	private void setUpTheRequestModels(RDFService rawRdfService,
			HttpServletRequest req) {
		VitroRequest vreq = new VitroRequest(req);

		setRdfServicesAndDatasets(rawRdfService, vreq);

		RDFService rdfService = vreq.getRDFService();
		Dataset dataset = vreq.getDataset();
		
		setRawModels(vreq, dataset);
		
		// We need access to some language-neutral items - either because we need to see all
		// contents regardless of language, or because we need to see the blank nodes that
		// are removed during language filtering.
		vreq.setLanguageNeutralUnionFullModel(ModelAccess.on(vreq).getOntModel(ModelID.UNION_FULL));
		vreq.setLanguageNeutralWebappDaoFactory(new WebappDaoFactorySDB(
				rdfService, createLanguageNeutralOntModelSelector(vreq), createWadfConfig(vreq)));

		wrapModelsWithLanguageAwareness(vreq);
		
		setCollator(vreq);
		
		setWebappDaoFactories(vreq, rdfService);
	}

	/**
	 * Set language-neutral and language-aware versions of the RdfService and
	 * Dataset.
	 */
	private void setRdfServicesAndDatasets(RDFService rawRdfService,
			VitroRequest vreq) {
		vreq.setUnfilteredRDFService(rawRdfService);
		vreq.setUnfilteredDataset(new RDFServiceDataset(rawRdfService));

		RDFService rdfService = addLanguageAwareness(vreq, rawRdfService);
		vreq.setRDFService(rdfService);

		Dataset dataset = new RDFServiceDataset(rdfService);
		vreq.setDataset(dataset);
	}
	
	private void setRawModels(VitroRequest vreq, Dataset dataset) {
		// These are memory-mapped (fast), and read-mostly (low contention), so
		// just use the ones from the context.
		useModelFromContext(vreq, ModelID.APPLICATION_METADATA);
		useModelFromContext(vreq, ModelID.USER_ACCOUNTS);
		useModelFromContext(vreq, ModelID.DISPLAY);
		useModelFromContext(vreq, ModelID.DISPLAY_DISPLAY);
		useModelFromContext(vreq, ModelID.DISPLAY_TBOX);
		useModelFromContext(vreq, ModelID.BASE_TBOX);
		useModelFromContext(vreq, ModelID.INFERRED_TBOX);
		useModelFromContext(vreq, ModelID.UNION_TBOX);

		// Anything derived from the ABOX is not memory-mapped, so create
		// versions from the short-term RDF service.
		OntModel baseABoxModel = createNamedModelFromDataset(dataset,
				JENA_DB_MODEL);
		OntModel inferenceABoxModel = createNamedModelFromDataset(dataset,
				JENA_INF_MODEL);
		OntModel unionABoxModel = createCombinedBulkUpdatingModel(
				baseABoxModel, inferenceABoxModel);

		OntModel baseFullModel = createCombinedBulkUpdatingModel(baseABoxModel,
				ModelAccess.on(vreq).getOntModel(ModelID.BASE_TBOX));
		OntModel inferenceFullModel = createCombinedModel(inferenceABoxModel,
				ModelAccess.on(vreq).getOntModel(ModelID.INFERRED_TBOX));
		OntModel unionFullModel = ModelFactory.createOntologyModel(
				OntModelSpec.OWL_MEM, dataset.getDefaultModel());

		ModelAccess.on(vreq).setOntModel(ModelID.BASE_ABOX, baseABoxModel);
		ModelAccess.on(vreq).setOntModel(ModelID.INFERRED_ABOX, inferenceABoxModel);
		ModelAccess.on(vreq).setOntModel(ModelID.UNION_ABOX, unionABoxModel);
		ModelAccess.on(vreq).setOntModel(ModelID.BASE_FULL, baseFullModel);
		ModelAccess.on(vreq).setOntModel(ModelID.INFERRED_FULL, inferenceFullModel);
		ModelAccess.on(vreq).setOntModel(ModelID.UNION_FULL, unionFullModel);
	}

	private void useModelFromContext(VitroRequest vreq, ModelID modelId) {
		OntModel contextModel = ModelAccess.on(ctx).getOntModel(modelId);
		ModelAccess.on(vreq).setOntModel(modelId, contextModel);
	}
	
	private OntModel createNamedModelFromDataset(Dataset dataset, String name) {
    	return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, dataset.getNamedModel(name));
    }

	private OntModel createCombinedModel(OntModel oneModel, OntModel otherModel) {
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, 
        		ModelFactory.createUnion(oneModel, otherModel));
	}

	private OntModel createCombinedBulkUpdatingModel(OntModel baseModel,
			OntModel otherModel) {
		BulkUpdateHandler bulkUpdateHandler = baseModel.getGraph().getBulkUpdateHandler();
		Graph unionGraph = ModelFactory.createUnion(baseModel, otherModel).getGraph();
		Model unionModel = ModelFactory.createModelForGraph(
				new SpecialBulkUpdateHandlerGraph(unionGraph, bulkUpdateHandler));
		return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, unionModel);
	}

	/** Create an OntModelSelector that will hold the un-language-filtered models. */
	private OntModelSelector createLanguageNeutralOntModelSelector(
			VitroRequest vreq) {
		OntModelSelectorImpl oms = new OntModelSelectorImpl();
		oms.setABoxModel(ModelAccess.on(vreq).getOntModel(ModelID.UNION_ABOX));
		oms.setTBoxModel(ModelAccess.on(vreq).getOntModel(ModelID.UNION_TBOX));
		oms.setFullModel(ModelAccess.on(vreq).getOntModel(ModelID.UNION_FULL));
		oms.setApplicationMetadataModel(ModelAccess.on(vreq).getOntModel(ModelID.APPLICATION_METADATA));
		oms.setDisplayModel(ModelAccess.on(vreq).getOntModel(ModelID.DISPLAY));
		oms.setUserAccountsModel(ModelAccess.on(vreq).getOntModel(ModelID.USER_ACCOUNTS));
		return oms;
	}

	private void wrapModelsWithLanguageAwareness(VitroRequest vreq) {
		wrapModelWithLanguageAwareness(vreq, ModelID.DISPLAY);
		wrapModelWithLanguageAwareness(vreq, ModelID.APPLICATION_METADATA);
		wrapModelWithLanguageAwareness(vreq, ModelID.BASE_TBOX);
		wrapModelWithLanguageAwareness(vreq, ModelID.UNION_TBOX);
		wrapModelWithLanguageAwareness(vreq, ModelID.UNION_FULL);
		wrapModelWithLanguageAwareness(vreq, ModelID.BASE_FULL);
	}

	private void wrapModelWithLanguageAwareness(HttpServletRequest req,
			ModelID id) {
		if (isLanguageAwarenessEnabled()) {
			OntModel unaware = ModelAccess.on(req).getOntModel(id);
			OntModel aware = LanguageFilteringUtils
					.wrapOntModelInALanguageFilter(unaware, req);
			ModelAccess.on(req).setOntModel(id, aware);
		}
	}
	
	private void setWebappDaoFactories(VitroRequest vreq, RDFService rdfService) {
		WebappDaoFactoryConfig config = createWadfConfig(vreq);
		
		WebappDaoFactory unfilteredWadf = new WebappDaoFactorySDB(rdfService,
				ModelAccess.on(vreq).getUnionOntModelSelector(), config);
		ModelAccess.on(vreq).setWebappDaoFactory(FactoryID.UNFILTERED_UNION,
				unfilteredWadf);
		
		WebappDaoFactory unfilteredAssertionsWadf = new WebappDaoFactorySDB(
				rdfService, ModelAccess.on(vreq).getBaseOntModelSelector(),
				config, SDBDatasetMode.ASSERTIONS_ONLY);
		ModelAccess.on(vreq).setWebappDaoFactory(FactoryID.BASE,
				unfilteredAssertionsWadf);
		ModelAccess.on(vreq).setWebappDaoFactory(FactoryID.UNFILTERED_BASE,
				unfilteredAssertionsWadf);

		WebappDaoFactory wadf = new WebappDaoFactorySDB(rdfService, ModelAccess
				.on(vreq).getUnionOntModelSelector(), config);

		// Do model switching and replace the WebappDaoFactory with
		// a different version if requested by parameters
		WebappDaoFactory switchedWadf = new ModelSwitcher()
				.checkForModelSwitching(vreq, wadf);
		// Switch the language-neutral one also.
		vreq.setLanguageNeutralWebappDaoFactory(new ModelSwitcher()
				.checkForModelSwitching(vreq,
						vreq.getLanguageNeutralWebappDaoFactory()));

		HideFromDisplayByPolicyFilter filter = new HideFromDisplayByPolicyFilter(
				RequestIdentifiers.getIdBundleForRequest(vreq),
				ServletPolicyList.getPolicies(ctx));
		WebappDaoFactoryFiltering filteredWadf = new WebappDaoFactoryFiltering(
				switchedWadf, filter);
		ModelAccess.on(vreq).setWebappDaoFactory(FactoryID.UNION, filteredWadf);
	}

	private WebappDaoFactoryConfig createWadfConfig(HttpServletRequest req) {
		List<String> langs = getPreferredLanguages(req);
		WebappDaoFactoryConfig config = new WebappDaoFactoryConfig();
		config.setDefaultNamespace(defaultNamespace);
		config.setPreferredLanguages(langs);
		config.setUnderlyingStoreReasoned(isStoreReasoned(req));
		config.setCustomListViewConfigFileMap(getCustomListViewConfigFileMap(
		        req.getSession().getServletContext()));
		return config;
	}

	/**
	 * This method is also used by VitroHttpServlet to retrieve the right Collator
	 * instance for picklist sorting
	 * @param req
	 * @return
	 */
	public static Enumeration<Locale> getPreferredLocales(HttpServletRequest req) {
	    return req.getLocales();
	}
	
	private List<String> getPreferredLanguages(HttpServletRequest req) {
		log.debug("Accept-Language: " + req.getHeader("Accept-Language"));
		return LanguageFilteringUtils.localesToLanguages(getPreferredLocales(req));
	}

	/**
	 * Language awareness is disabled unless they explicitly enable it.
	 */
	private Boolean isLanguageAwarenessEnabled() {
		return Boolean.valueOf(props.getProperty("RDFService.languageFilter",
				"false"));
	}

	private RDFService addLanguageAwareness(HttpServletRequest req,
			RDFService rawRDFService) {
		List<String> langs = getPreferredLanguages(req);
		if (isLanguageAwarenessEnabled()) {
			return new LanguageFilteringRDFService(rawRDFService, langs);
		} else {
			return rawRDFService;
		}
	}
	
   private void setCollator(VitroRequest vreq) {
        Enumeration<Locale> locales = getPreferredLocales(vreq);
        while(locales.hasMoreElements()) {
            Locale locale = locales.nextElement();
            Collator collator = Collator.getInstance(locale);
            if(collator != null) {
                vreq.setCollator(collator);
                return;
            }
        }
        vreq.setCollator(Collator.getInstance());
    }

	private boolean isStoreReasoned(ServletRequest req) {
	    String isStoreReasoned = ConfigurationProperties.getBean(req).getProperty(
	            "VitroConnection.DataSource.isStoreReasoned", "true");
	    return ("true".equals(isStoreReasoned));
	}
	
	private Map<Pair<String,Pair<ObjectProperty, String>>, String> 
	        getCustomListViewConfigFileMap(ServletContext ctx) {
	    Map<Pair<String,Pair<ObjectProperty, String>>, String> map = 
	            (Map<Pair<String,Pair<ObjectProperty, String>>, String>) 
	                    ctx.getAttribute("customListViewConfigFileMap");
	    if (map == null) {
	        map = new ConcurrentHashMap<Pair<String,Pair<ObjectProperty, String>>, String>();
	        ctx.setAttribute("customListViewConfigFileMap", map);
	    }
	    return map;
	}

	@Override
	public void destroy() {
		// Nothing to destroy
	}

}
