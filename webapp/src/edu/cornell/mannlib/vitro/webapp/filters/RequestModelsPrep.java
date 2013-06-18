/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.util.List;
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
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.HideFromDisplayByPolicyFilter;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
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
			Pattern.compile(".*\\.(gif|GIF|jpg|jpeg)$"),
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
		HttpSession session = req.getSession();
		VitroRequest vreq = new VitroRequest(req);

		vreq.setUnfilteredRDFService(rawRdfService);

		List<String> langs = getPreferredLanguages(req);
		RDFService rdfService = addLanguageAwareness(langs, rawRdfService);
		vreq.setRDFService(rdfService);

		Dataset dataset = new RDFServiceDataset(rdfService);
		vreq.setDataset(dataset);

		WebappDaoFactoryConfig config = createWadfConfig(langs);
		
		WebappDaoFactory assertions = new WebappDaoFactorySDB(rdfService,
				ModelAccess.on(ctx).getBaseOntModelSelector(), config,
				SDBDatasetMode.ASSERTIONS_ONLY);
		ModelAccess.on(vreq).setBaseWebappDaoFactory(assertions);

		ModelAccess.on(vreq).setJenaOntModel(
				ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
						dataset.getDefaultModel()));

		OntModelSelector oms = ModelAccess.on(ctx).getUnionOntModelSelector();
		WebappDaoFactory wadf = new WebappDaoFactorySDB(rdfService, oms, config);
		vreq.setUnfilteredWebappDaoFactory(wadf);
		
		wadf = new WebappDaoFactorySDB(rdfService, ModelAccess.on(vreq).getUnionOntModelSelector(), config);
		if (isLanguageAwarenessEnabled()) {
			ModelAccess.on(vreq).setDisplayModel(
					LanguageFilteringUtils.wrapOntModelInALanguageFilter(
							ModelAccess.on(session).getDisplayModel(), req));
		}

		// Do model switching and replace the WebappDaoFactory with
		// a different version if requested by parameters
		WebappDaoFactory switchedWadf = new ModelSwitcher()
				.checkForModelSwitching(vreq, wadf);

		HideFromDisplayByPolicyFilter filter = new HideFromDisplayByPolicyFilter(
				RequestIdentifiers.getIdBundleForRequest(req),
				ServletPolicyList.getPolicies(ctx));
		WebappDaoFactoryFiltering filteredWadf = new WebappDaoFactoryFiltering(
				switchedWadf, filter);
		ModelAccess.on(vreq).setWebappDaoFactory(filteredWadf);
	}

	private WebappDaoFactoryConfig createWadfConfig(List<String> langs) {
		WebappDaoFactoryConfig config = new WebappDaoFactoryConfig();
		config.setDefaultNamespace(defaultNamespace);
		config.setPreferredLanguages(langs);
		return config;
	}

	private List<String> getPreferredLanguages(HttpServletRequest req) {
		log.debug("Accept-Language: " + req.getHeader("Accept-Language"));
		return LanguageFilteringUtils.localesToLanguages(req.getLocales());
	}

	/**
	 * Language awareness is enabled unless they explicitly disable it.
	 */
	private Boolean isLanguageAwarenessEnabled() {
		return Boolean.valueOf(props.getProperty("RDFService.languageFilter",
				"true"));
	}

	private RDFService addLanguageAwareness(List<String> langs,
			RDFService rawRDFService) {
		if (isLanguageAwarenessEnabled()) {
			return new LanguageFilteringRDFService(rawRDFService, langs);
		} else {
			return rawRDFService;
		}
	}

	@Override
	public void destroy() {
		// Nothing to destroy
	}

}
