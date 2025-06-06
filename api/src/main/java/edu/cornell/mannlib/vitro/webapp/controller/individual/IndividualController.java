/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.individual;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.IndividualAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.NotFoundException;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;

/**
 * Handles requests for entity information.
 */
@WebServlet(name = "individual", urlPatterns = {"/display","/display/*","/entity","/entity/*","/individual","/individual/*"})
public class IndividualController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory
			.getLog(IndividualController.class);

	@Deprecated
	private static final String PROPERTY_EXTENDED_LOD = "serveExtendedLinkedData";

	/**
	 * Use this map to decide which MIME type is suited for the "accept" header.
	 */
	public static final Map<String, Float> ACCEPTED_CONTENT_TYPES = initializeContentTypes();

	private static Map<String, Float> initializeContentTypes() {
		HashMap<String, Float> map = new HashMap<String, Float>();
		map.put(HTML_MIMETYPE, 0.5f);
		map.put(XHTML_MIMETYPE, 0.5f);
		map.put("application/xml", 0.5f);
        map.put(JSON_MIMETYPE, 1.0f);
        map.put(JSON_LD_MIMETYPE, 1.0f);
		map.put(RDFXML_MIMETYPE, 1.0f);
		map.put(RDFXML_MIMETYPE, 1.0f);
		map.put(N3_MIMETYPE, 1.0f);
		map.put(TTL_MIMETYPE, 1.0f);
		return Collections.unmodifiableMap(map);
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		try {
			/*
			 * What type of request is this?
			 */
			IndividualRequestInfo requestInfo = analyzeTheRequest(vreq);

			switch (requestInfo.getType()) {
			case RDF_REDIRECT:
				/*
				 * If someone expects RDF by asking for the individual with an
				 * "accept" HTTP header, redirect them to the preferred URL.
				 */
				return new RedirectResponseValues(requestInfo.getRedirectUrl(),
						HttpServletResponse.SC_SEE_OTHER);
			case NO_INDIVIDUAL:
				/*
				 * If we can't figure out what individual you want, or if there
				 * is no such individual, show an informative error page.
				 */
				throw new NotFoundException();
			case BYTESTREAM_REDIRECT:
				/*
				 * If the Individual requested is a FileBytestream, redirect
				 * them to the direct download URL, so they will get the correct
				 * filename, etc.
				 */
				return new RedirectResponseValues(requestInfo.getRedirectUrl(),
						HttpServletResponse.SC_SEE_OTHER);
			case LINKED_DATA:
				/*
				 * If they are asking for RDF using the preferred URL, give it
				 * to them.
				 */
				if (useExtendedLOD(vreq)) {
					return new ExtendedRdfAssembler(vreq,
							requestInfo.getIndividual(),
							requestInfo.getRdfFormat()).assembleRdf();
				} else {
					return new IndividualRdfAssembler(vreq,
							requestInfo.getIndividual().getURI(),
							requestInfo.getRdfFormat()).assembleRdf();
				}
			default:
				/*
				 * Otherwise, prepare an HTML response for the requested
				 * individual.
				 */
				return new IndividualResponseBuilder(vreq,
						requestInfo.getIndividual()).assembleResponse();
			}
		} catch (NotFoundException e) {
			throw e;
		} catch (Throwable e) {
			log.error(e, e);
			return new ExceptionResponseValues(e);
		}
	}
	
    @Override
    public AuthorizationRequest requiredActions(VitroRequest vreq) {
        try {
            IndividualRequestInfo requestInfo = analyzeTheRequest(vreq);
            switch (requestInfo.getType()) {
                case RDF_REDIRECT:
                case NO_INDIVIDUAL:
                case BYTESTREAM_REDIRECT:
                    return AuthorizationRequest.AUTHORIZED;
                default:
                    AccessObject ao = new IndividualAccessObject(requestInfo.getIndividual().getURI());
                    ao.setModel(vreq.getJenaOntModel());
                    AuthorizationRequest request = new SimpleAuthorizationRequest(ao, AccessOperation.DISPLAY);
                    return request;
            }
        } catch (Throwable e) {
            return AuthorizationRequest.UNAUTHORIZED;
        }
    }

	private IndividualRequestInfo analyzeTheRequest(VitroRequest vreq) {
		return new IndividualRequestAnalyzer(vreq,
				new IndividualRequestAnalysisContextImpl(vreq)).analyze();
	}

	private boolean useExtendedLOD(HttpServletRequest req) {
		ConfigurationProperties props = ConfigurationProperties.getBean(req);
		return Boolean.valueOf(props.getProperty(PROPERTY_EXTENDED_LOD));
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
