/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONFIGURATION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONTENT;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao.FullPropertyKey;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.JoinedOntModelCache;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.TripleSource;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringUtils;
import edu.cornell.mannlib.vitro.webapp.triplesource.ShortTermCombinedTripleSource;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * The simple implementation of ShortTermCombinedTripleSource.
 * 
 * The short-term RDFServices are cached, lest we somehow create duplicates for
 * the same request. Similarly with the short-term OntModels.
 */
public class BasicShortTermCombinedTripleSource implements
		ShortTermCombinedTripleSource {
	private static final Log log = LogFactory
			.getLog(BasicShortTermCombinedTripleSource.class);

	private final HttpServletRequest req;
	private final ServletContext ctx;
	private final ConfigurationProperties props;
	private final BasicCombinedTripleSource parent;
	private final Map<WhichService, TripleSource> sources;
	private final Map<WhichService, RDFService> rdfServices;
	private final OntModelCache ontModelCache;

	public BasicShortTermCombinedTripleSource(HttpServletRequest req,
			BasicCombinedTripleSource parent,
			final Map<WhichService, TripleSource> sources) {
		this.req = req;
		this.ctx = req.getSession().getServletContext();
		this.props = ConfigurationProperties.getBean(ctx);
		this.parent = parent;
		this.sources = sources;
		this.rdfServices = populateRdfServicesMap();
		this.ontModelCache = createOntModelCache();
	}

	private Map<WhichService, RDFService> populateRdfServicesMap() {
		Map<WhichService, RDFService> map = new EnumMap<>(WhichService.class);
		for (WhichService which : WhichService.values()) {
			map.put(which, parent.getRDFServiceFactory(which)
					.getShortTermRDFService());
		}
		return Collections.unmodifiableMap(map);
	}

	private OntModelCache createOntModelCache() {
		return new JoinedOntModelCache(shortModels(CONTENT),
				shortModels(CONFIGURATION));
	}

	/**
	 * Ask each triple source what short-term models should mask their long-term
	 * counterparts.
	 */
	private OntModelCache shortModels(WhichService which) {
		return sources.get(which).getShortTermOntModels(rdfServices.get(which),
				parent.getOntModels(which));
	}

	@Override
	public RDFService getRDFService(WhichService whichService) {
		return rdfServices.get(whichService);
	}

	@Override
	public OntModelCache getOntModelCache() {
		return ontModelCache;
	}

	@Override
	public WebappDaoFactoryConfig getWebappDaoFactoryConfig() {
		List<String> langs = getPreferredLanguages();
		WebappDaoFactoryConfig config = new WebappDaoFactoryConfig();
		config.setDefaultNamespace(props.getProperty("Vitro.defaultNamespace"));
		config.setPreferredLanguages(langs);
		config.setUnderlyingStoreReasoned(isStoreReasoned());
		config.setCustomListViewConfigFileMap(getCustomListViewConfigFileMap());
		return config;
	}

	private List<String> getPreferredLanguages() {
		log.debug("Accept-Language: " + req.getHeader("Accept-Language"));
		return LanguageFilteringUtils.localesToLanguages(getPreferredLocales());
	}

	@SuppressWarnings("unchecked")
	private Enumeration<Locale> getPreferredLocales() {
		return req.getLocales();
	}

	private boolean isStoreReasoned() {
		String isStoreReasoned = props.getProperty(
				"VitroConnection.DataSource.isStoreReasoned", "true");
		return ("true".equals(isStoreReasoned));
	}

	private Map<FullPropertyKey, String> getCustomListViewConfigFileMap() {
		@SuppressWarnings("unchecked")
		Map<FullPropertyKey, String> map = (Map<FullPropertyKey, String>) ctx
				.getAttribute("customListViewConfigFileMap");
		if (map == null) {
			map = new ConcurrentHashMap<FullPropertyKey, String>();
			ctx.setAttribute("customListViewConfigFileMap", map);
		}
		return map;
	}

	@Override
	public void close() {
		for (WhichService which : WhichService.values()) {
			rdfServices.get(which).close();
		}
	}

	@Override
	public String toString() {
		return "BasicShortTermCombinedTripleSource[" + ToString.hashHex(this)
				+ ", req=" + ToString.hashHex(req) + ", sources=" + sources
				+ ", ontModels=" + ontModelCache + "]";
	}

}
