/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.individual;

import static edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet.JSON_LD_MIMETYPE;
import static edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet.JSON_MIMETYPE;
import static edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet.N3_MIMETYPE;
import static edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet.RDFXML_MIMETYPE;
import static edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet.TTL_MIMETYPE;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.config.ContextPath;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;

/**
 * All sorts of requests are fielded by the IndividualController. Look at this
 * request and figure out what type it is, and what data we need in order to
 * respond.
 */
public class IndividualRequestAnalyzer {
	private static final Log log = LogFactory
			.getLog(IndividualRequestAnalyzer.class);


	private static Pattern RDF_REQUEST = Pattern.compile("^/individual/([^/]+)/\\1\\.(rdf|n3|ttl|jsonld)$");
    private static Pattern HTML_REQUEST = Pattern.compile("^/display/([^/]+)$");
	private static Pattern LINKED_DATA_URL = Pattern.compile("^/individual/([^/]+)$");

	private final VitroRequest vreq;
	private final IndividualRequestAnalysisContext analysisContext;
	private final String url;

	public IndividualRequestAnalyzer(VitroRequest vreq,
			IndividualRequestAnalysisContext analysisContext) {
		this.vreq = vreq;

		// get URL without hostname or servlet context
		this.url = vreq.getRequestURI().substring(ContextPath.getPath(vreq).length());

		this.analysisContext = analysisContext;
	}

	public IndividualRequestInfo analyze() {
		// If this is a request for RDF using an accept header, redirect it to
		// the preferred URL.
		String redirectUrl = checkForRedirect();
		if (redirectUrl != null) {
			return IndividualRequestInfo.buildRdfRedirectInfo(redirectUrl);
		}

		// Figure out what individual we are talking about. If we can't figure
		// it out, complain.
		Individual individual = getIndividualFromRequest();
		if (individual == null) {
			return IndividualRequestInfo.buildNoIndividualInfo();
		}

		// If the requested individual is a FileBytestream, redirect to its
		// "alias URL".
		redirectUrl = getAliasUrlForBytestreamIndividual(individual);
		if (redirectUrl != null) {
			return IndividualRequestInfo.buildBytestreamRedirectInfo(redirectUrl);
		}

		// Check to see whether Linked Data was requested.
		ContentType rdfFormat = checkUrlForLinkedDataRequest();
		if (rdfFormat != null) {
			return IndividualRequestInfo.buildLinkedDataInfo(individual, rdfFormat);
		}

		// No redirect, no Linked Data; no problem.
		return IndividualRequestInfo.buildDefaultInfo(individual);
	}

	/*
	 * Following recipe 3 from
	 * "Best Practice Recipes for Publishing RDF Vocabularies." See
	 * http://www.w3.org/TR/swbp-vocab-pub/#recipe3. The basic idea is that a
	 * URI like http://vivo.cornell.edu/individual/n1234 identifies a real world
	 * individual. HTTP cannot send that as the response to a GET request
	 * because it can only send bytes and not things. The server sends a 303, to
	 * mean "you asked for something I cannot send you, but I can send you this
	 * other stream of bytes about that thing." In the case of a request like
	 * http://vivo.cornell.edu/individual/n1234/n1234.rdf or
	 * http://vivo.cornell.edu/individual/n1234?format=rdfxml, the request is
	 * for a set of bytes rather than an individual, so no 303 is needed.
	 */
	private static Pattern URI_PATTERN = Pattern
			.compile("^/individual/([^/]*)$");

	private String checkForRedirect() {
		// A "format" parameter is special, and is dealt with elsewhere.
		String formatParam = getRequestParameter("format", "");
		if (!formatParam.isEmpty()) {
			return null;
		}

		// Is it "/individual/" followed by a single group?
		Matcher m = URI_PATTERN.matcher(url);
		if (!m.matches() || m.groupCount() < 1) {
			return null;
		}

		// Then, use the "accept" header to decide how to redirect it.
		ContentType c = checkAcceptHeaderForLinkedDataRequest();
		if (c != null) {
			String mediaType = c.getMediaType();
			if (RDFXML_MIMETYPE.equals(mediaType)) {
				return "/individual/" + m.group(1) + "/" + m.group(1) + ".rdf";
			} else if (N3_MIMETYPE.equals(mediaType)) {
				return "/individual/" + m.group(1) + "/" + m.group(1) + ".n3";
			} else if (TTL_MIMETYPE.equals(mediaType)) {
				return "/individual/" + m.group(1) + "/" + m.group(1) + ".ttl";
			} else if (JSON_MIMETYPE.equals(mediaType) || JSON_LD_MIMETYPE.equals(mediaType)){
                return "/individual/" + m.group(1) + "/" + m.group(1) + ".jsonld";
            }
		}
		// or redirect to the canonical URL for HTML representation.
		return "/display/" + m.group(1);
	}

	/**
	 * Check the accept header. This request will trigger a redirect with a 303
	 * ("see also"), because the request is for an individual but the server can
	 * only provide a set of bytes.
	 */
	protected ContentType checkAcceptHeaderForLinkedDataRequest() {
		String acceptHeader = vreq.getHeader("Accept");
		if (acceptHeader == null)
		    acceptHeader = vreq.getHeader("accept");
		if (acceptHeader == null)
			return null;

		try {
			Map<String, Float> typesAndQ = ContentType
					.getTypesAndQ(acceptHeader);
			String ctStr = ContentType.getBestContentType(typesAndQ,
					IndividualController.ACCEPTED_CONTENT_TYPES);

			if (RDFXML_MIMETYPE.equals(ctStr) || N3_MIMETYPE.equals(ctStr)
					|| TTL_MIMETYPE.equals(ctStr) || JSON_MIMETYPE.equals(ctStr)
					|| JSON_LD_MIMETYPE.equals(ctStr)) {
				return new ContentType(ctStr);
			}
		} catch (Throwable th) {
			log.error("Problem while checking accept header ", th);
		}
		return null;
	}

	/**
	 * Gets the entity id from the request. Works for the following styles of
	 * URLs:
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
	 *     /individual/localname/localname.jsonld
	 * </pre>
	 *
	 * @return null on failure.
	 */
	public Individual getIndividualFromRequest() {
		try {
			// Check for "uri" parameter.
			String uri = getRequestParameter("uri", "");
			if (!uri.isEmpty()) {
				return getIndividualByUri(uri);
			}

			// Check for "netId" or "netid" parameter
			String netId = getRequestParameter("netId",
					getRequestParameter("netid", ""));
			if (!netId.isEmpty()) {
				return getIndividualByNetId(netId);
			}

			// Check for just a local name
			Matcher linkedDataMatch = LINKED_DATA_URL.matcher(url);
			if (linkedDataMatch.matches() && linkedDataMatch.groupCount() == 1) {
				return getIndividualByLocalname(linkedDataMatch.group(1));
			}

			// Check for the canonical HTML request.
			Matcher htmlMatch = HTML_REQUEST.matcher(url);
			if (htmlMatch.matches() && htmlMatch.groupCount() == 1) {
				return getIndividualByLocalname(htmlMatch.group(1));
			}

			// Check for a request for RDF.
			Matcher rdfMatch = RDF_REQUEST.matcher(url);
			if (rdfMatch.matches() && rdfMatch.groupCount() == 2) {
				return getIndividualByLocalname(rdfMatch.group(1));
			}

			// Couldn't match it to anything.
			return null;
		} catch (Throwable e) {
			log.error("Problems trying to find Individual", e);
			return null;
		}
	}

	private String getAliasUrlForBytestreamIndividual(Individual individual) {
		String aliasUrl =  analysisContext.getAliasUrlForBytestreamIndividual(individual);

		if (individual.getURI().equals(aliasUrl)) {
			// Avoid a tight loop; if the alias URL is equal to the URI,
			// then don't recognize it as a FileBytestream.
			return null;
		} else {
			return aliasUrl;
		}
	}

	/**
	 * @return null if this is not a linked data request, returns content type
	 *         if it is a linked data request.
	 *
	 *         These are Vitro-specific ways of requesting rdf, unrelated to
	 *         semantic web standards. They do not trigger a redirect with a
	 *         303, because the request is for a set of bytes rather than an
	 *         individual.
	 */
	protected ContentType checkUrlForLinkedDataRequest() {
		/*
		 * Check for url param specifying format. Example:
		 * http://vivo.cornell.edu/individual/n23?format=rdfxml
		 */
		String formatParam = getRequestParameter("format", "");
		if (formatParam.contains("rdfxml")) {
			return ContentType.RDFXML;
		}
		if (formatParam.contains("n3")) {
			return ContentType.N3;
		}
		if (formatParam.contains("ttl")) {
			return ContentType.TURTLE;
		}
        if (formatParam.contains("jsonld") || formatParam.contains("json")){
            return ContentType.JSON;
        }

		/*
		 * Check for parts of URL that indicate request for RDF. Examples:
		 * http://vivo.cornell.edu/individual/n23/n23.rdf
		 * http://vivo.cornell.edu/individual/n23/n23.n3
		 * http://vivo.cornell.edu/individual/n23/n23.ttl
		 * http://vivo.cornell.edu/individual/n23/n23.jsonld
		 */
		Matcher rdfMatch = RDF_REQUEST.matcher(url);
		if (rdfMatch.matches() && rdfMatch.groupCount() == 2) {
			String rdfType = rdfMatch.group(2);
			if ("rdf".equals(rdfType)) {
				return ContentType.RDFXML;
			}
			if ("n3".equals(rdfType)) {
				return ContentType.N3;
			}
			if ("ttl".equals(rdfType)) {
				return ContentType.TURTLE;
			}
			if ("jsonld".equals(rdfType)) {
				return ContentType.JSON;
			}
		}

		return null;
	}



	private String getRequestParameter(String key, String defaultValue) {
		String value = vreq.getParameter(key);
		if ((value == null) || value.isEmpty()) {
			return defaultValue;
		} else {
			return value;
		}
	}

	private Individual getIndividualByUri(String uri) {
		return analysisContext.getIndividualByURI(uri);
	}

	private Individual getIndividualByLocalname(String localname) {
		String defaultNamespace = analysisContext.getDefaultNamespace();
		String uri = defaultNamespace + localname;
		return getIndividualByUri(uri);
	}

	private Individual getIndividualByNetId(String netId) {
		return analysisContext.getIndividualByNetId(netId);
	}
}

