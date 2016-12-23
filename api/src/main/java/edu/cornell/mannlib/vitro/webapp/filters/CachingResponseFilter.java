/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.utils.searchengine.FieldMap;
import edu.cornell.mannlib.vitro.webapp.utils.searchengine.SearchQueryUtils;
import edu.cornell.mannlib.vitro.webapp.utils.searchengine.SearchResultsParser;

/**
 * Assist in cache management for individual profile pages.
 * 
 * Must be enabled in runtime.properties.
 * 
 * Only works for users who are not logged in.
 * 
 * The search index must be configured to keep an ETAG on each individual's
 * record. The ETAG is a hash of the record's content and is updated each time
 * the individual is re-indexed.
 * 
 * But this ETAG is not sufficient, since the page may have different versions
 * for different languages. So we append a hash of the Locales from the request
 * to the ETAG to make it unique. NOTE: If we allow users to choose their
 * preferred languages, the LocalSelectionFilter must execute before this one.
 * 
 * When an external cache (e.g. Squid) is asked for an individual's profile
 * page, it will ask VIVO whether the version in the cache is still current, and
 * to provide a new version if it is not. This is a conditional request.
 * 
 * When a conditional request is received, this filter will check to see whether
 * the request is on behalf of a logged-in user. If so, a fresh response is
 * generated, with a Cache-Control header that should prevent the cache from
 * storing that response.
 * 
 * If the requesting user is not logged in, this filter will ask the search
 * engine for the ETAG on the requested individual. If it is the same as the
 * ETAG supplied by the cache in the request, then the response is 304 Not
 * Modified. Otherwise, a fresh response is generated.
 * 
 * An unconditional request may mean that there is no external cache, or that
 * the cache doesn't have a copy of this particular page.
 */
public class CachingResponseFilter implements Filter {
	private static final Log log = LogFactory
			.getLog(CachingResponseFilter.class);

	private static final String PROPERTY_DEFAULT_NAMESPACE = "Vitro.defaultNamespace";
	private static final String PROPERTY_ENABLE_CACHING = "http.createCacheHeaders";
	private static final String ETAG_FIELD = "etag";

	private static final FieldMap parserFieldMap = SearchQueryUtils.fieldMap()
			.put(ETAG_FIELD, ETAG_FIELD);

	private ServletContext ctx;
	private String defaultNamespace;
	private boolean enabled;

	@Override
	public void init(FilterConfig fc) throws ServletException {
		ctx = fc.getServletContext();
		ConfigurationProperties props = ConfigurationProperties.getBean(ctx);
		defaultNamespace = props.getProperty(PROPERTY_DEFAULT_NAMESPACE);
		enabled = Boolean.valueOf(props.getProperty(PROPERTY_ENABLE_CACHING));
	}

	@Override
	public void destroy() {
		// Nothing to tear down.
	}

	/**
	 * Process an HTTP request.
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		/*
		 * If caching is disabled, if this request is not for a profile page, or
		 * if the individual doesn't appear in the search index, create a basic,
		 * cache-neutral response.
		 */
		if (!enabled) {
			produceBasicResponse(req, resp, chain);
			return;
		}
		String individualUri = figureIndividualUriFromRequest(req);
		if (individualUri == null) {
			produceBasicResponse(req, resp, chain);
			return;
		}
		String rawEtag = findEtagForIndividual(individualUri);
		String etag = produceLanguageSpecificEtag(req, rawEtag);
		if (etag == null) {
			produceBasicResponse(req, resp, chain);
			return;
		}

		/*
		 * If a logged-in user asks for an individual profile page, the response
		 * should not come from the cache, nor should it be stored in the cache.
		 */
		if (userIsLoggedIn(req)) {
			produceUncacheableResponse(req, resp, chain);
			return;
		}

		/*
		 * If the request is not conditional then there is no cached version of
		 * this page. If the request is conditional and the condition is met,
		 * then the cached version is stale. In either case, create a fresh
		 * response to be stored in the cache.
		 */
		if (!isConditionalRequest(req)) {
			produceCacheableResponse(req, resp, chain, etag);
			return;
		}
		if (cacheIsStale(req, etag)) {
			produceCacheableResponse(req, resp, chain, etag);
			return;
		}

		/*
		 * If the request is conditional and the condition is not met
		 * (individual has not changed), send a "not-modified" response, so the
		 * cached version will be used.
		 */
		produceCacheHitResponse(resp, etag);
	}

	private boolean isConditionalRequest(HttpServletRequest req) {
		if (req.getHeader("If-None-Match") == null) {
			log.debug("Not conditional request.");
			return false;
		} else {
			log.debug("Conditional request.");
			return true;
		}
	}

	private boolean userIsLoggedIn(HttpServletRequest req) {
		UserAccount currentUser = LoginStatusBean.getCurrentUser(req);
		if (currentUser == null) {
			log.debug("Not logged in.");
			return false;
		} else {
			log.debug("Logged in as '" + currentUser.getEmailAddress() + "'");
			return true;
		}
	}

	/**
	 * This rejects some of the requests as being obviously not individuals, and
	 * then assumes that the last part of any request is a Localname.
	 * 
	 * This is not always true, of course, but it will work because we will
	 * prepend the default namespace and look for the resulting "URI" in the URI
	 * field of the search index. If we find it there, then it is valid.
	 * 
	 * If we were to make this more rigorous, it would reduce the number of
	 * unnecessary searches.
	 */
	private String figureIndividualUriFromRequest(HttpServletRequest req) {
		String requestPath = req.getRequestURI();
		if (requestPath == null) {
			return null;
		}

		if (!mightBeProfileRequest(requestPath)) {
			return null;
		}

		String[] pathParts = requestPath.split("/");
		String uri = defaultNamespace + pathParts[pathParts.length - 1];

		log.debug("Request path = '" + requestPath + "', uri = '" + uri + "'");
		return uri;
	}

	/**
	 * Requests for profile pages come in many forms, but we can still narrow
	 * them down.
	 * 
	 * Eliminate CSS files, JavaScript files, and images.
	 * 
	 * That leaves these acceptable forms:
	 * 
	 * <pre>
	 *     /individual?uri=urlencodedURI
	 *     /individual?netId=bdc34
	 *     /individual?netid=bdc34
	 *     /individual/localname         
	 *     /display/localname
	 *     /individual/localname/localname.rdf
	 *     /individual/localname/localname.n3
	 *     /individual/localname/localname.ttl
	 * </pre>
	 */
	private boolean mightBeProfileRequest(String requestPath) {
		String path = requestPath.toLowerCase();
		String[] extensions = { ".css", ".js", ".gif", ".png", ".jpg", ".jpeg" };
		for (String ext : extensions) {
			if (path.endsWith(ext)) {
				return false;
			}
		}
		return requestPath.endsWith("/individual")
				|| requestPath.contains("/individual/")
				|| requestPath.contains("/display/");
	}

	/**
	 * Ask the search engine whether it has an ETAG for this URI.
	 */
	private String findEtagForIndividual(String individualUri) {
		SearchEngine search = ApplicationUtils.instance().getSearchEngine();
		SearchQuery query = search.createQuery("URI:" + individualUri).addFields(
				ETAG_FIELD);

		try {
			SearchResponse response = search.query(query);
			List<Map<String, String>> maps = new SearchResultsParser(response,
					parserFieldMap).parse();
			log.debug("Search response for '" + query.getQuery() + "' was "
					+ maps);

			if (maps.isEmpty()) {
				return null;
			} else {
				return maps.get(0).get(ETAG_FIELD);
			}
		} catch (SearchEngineException e) {
			log.warn(
					"Search query '" + query.getQuery() + "' threw an exception",
					e);
			return null;
		}
	}

	/**
	 * The ETAG from the search index is not specific enough, since we may have
	 * different versions for different languages. Add the Locales from the
	 * request to make it unique.
	 */
	private String produceLanguageSpecificEtag(HttpServletRequest req,
			String rawEtag) {
		if (rawEtag == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<Locale> locales = EnumerationUtils.toList(req.getLocales());

		StringBuilder buffer = new StringBuilder("\"").append(rawEtag);
		for (Locale locale : locales) {
			buffer.append(locale.toString());
		}
		buffer.append("\"");

		String etag = buffer.toString().replaceAll("\\s", "");
		log.debug("Language-specific ETAG = " + etag);
		return etag;
	}

	/**
	 * If the etag does not match any of the etags in any of the "If-None-Match"
	 * headers, then they are all stale. An asterisk matches anything.
	 */
	private boolean cacheIsStale(HttpServletRequest req, String etag) {
		for (Enumeration<?> values = req.getHeaders("If-None-Match"); values
				.hasMoreElements();) {
			String value = (String) values.nextElement();
			log.debug("If-None-Match: " + value);

			String[] matches = value.split("\\s*,\\s*");
			for (String match : matches) {
				if (etag.equalsIgnoreCase(match) || "*".equals(match)) {
					log.debug("Cache is not stale: etag=" + match);
					return false;
				}
			}
		}
		log.debug("Cache is stale.");
		return true;
	}

	private void produceBasicResponse(HttpServletRequest req,
			HttpServletResponse resp, FilterChain chain) throws IOException,
			ServletException {
		chain.doFilter(req, resp);
	}

	private void produceUncacheableResponse(HttpServletRequest req,
			HttpServletResponse resp, FilterChain chain) throws IOException,
			ServletException {
		String etag = generateArbitraryUniqueEtag(req);
		log.debug("Produce uncacheable response: etag='" + etag + "'");

		resp.addHeader("ETag", etag);
		resp.addHeader("Vary", "*");
		resp.addHeader("Cache-Control", "no-store");
		chain.doFilter(req, resp);
	}

	private void produceCacheableResponse(HttpServletRequest req,
			HttpServletResponse resp, FilterChain chain, String etag)
			throws IOException, ServletException {
		log.debug("Produce cacheable response: etag='" + etag + "'");
		resp.addHeader("ETag", etag);
		resp.addHeader("Vary", "Accept-Language");
		chain.doFilter(req, resp);
	}

	/**
	 * Technically, if the request is not GET or HEAD, we should return 412
	 * PreconditionFailed. However, we usually treat GET and POST as equivalent.
	 */
	private void produceCacheHitResponse(HttpServletResponse resp, String etag)
			throws IOException {
		log.debug("Produce cache hit response: etag='" + etag + "'");
		resp.addHeader("ETag", etag);
		resp.addHeader("Vary", "Accept-Language");
		resp.sendError(HttpServletResponse.SC_NOT_MODIFIED, "Not Modified");
	}

	private String generateArbitraryUniqueEtag(HttpServletRequest req) {
		return String.format("%s-%d", req.getSession().getId(),
				System.currentTimeMillis());
	}

}
