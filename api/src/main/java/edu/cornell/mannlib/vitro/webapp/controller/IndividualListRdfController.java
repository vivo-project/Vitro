/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.vitro.webapp.controller;

import static edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery.RdfResultMediaType.RDF_XML;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.RDF_TYPE;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_ASSERTIONS;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.ARQException;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.ObjectPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.api.VitroApiServlet;
import edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery.InvalidQueryTypeException;
import edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery.RdfResultMediaType;
import edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery.SparqlQueryApiExecutor;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.http.AcceptHeaderParsingException;
import edu.cornell.mannlib.vitro.webapp.utils.http.NotAcceptableException;

@WebServlet(name = "IndividualListRdf", urlPatterns = {"/listrdf/*"} )
public class IndividualListRdfController extends VitroApiServlet {
	private static final Log log = LogFactory
			.getLog(IndividualListRdfController.class);

	private static final String QUERY_TEMPLATE = "CONSTRUCT { ?s a ?vclass . } WHERE { ?s a ?vclass . }";

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			String vclassUri = getVClassParameter(req);
			RdfResultMediaType mediaType = parseAcceptHeader(req);
			log.debug("Requested class is '" + vclassUri + "', media type is "
					+ mediaType);

			if (isVclassRestricted(vclassUri, req)) {
				sendEmptyModel(mediaType, resp);
			} else {
				executeQuery(buildQuery(vclassUri), mediaType.getContentType(),
						getRdfService(req), resp);
			}
		} catch (BadParameterException e) {
			sendShortResponse(SC_BAD_REQUEST, e.getMessage(), resp);
		} catch (ARQException e) {
		    log.error(e, e);
		    sendShortResponse(SC_BAD_REQUEST, "", resp);
		} catch (NotAcceptableException | AcceptHeaderParsingException e) {
			sendShortResponse(SC_NOT_ACCEPTABLE,
					"The accept header does not include any "
							+ "available content type.", e, resp);
		} catch (Exception e) {
			sendShortResponse(SC_INTERNAL_SERVER_ERROR,
					"Failed to obtain the list.", e, resp);
		}
	}

	private String getVClassParameter(HttpServletRequest req)
			throws BadParameterException {
		String vclass = req.getParameter("vclass");
		if (vclass == null) {
			throw new BadParameterException(
					"vclass parameter was not supplied.");
		}
		if (vclass.trim().isEmpty()) {
			throw new BadParameterException("vclass parameter is empty.");
		}
		return vclass;
	}

	private RdfResultMediaType parseAcceptHeader(HttpServletRequest req)
			throws NotAcceptableException, AcceptHeaderParsingException {
		String defaultType = RDF_XML.getContentType();
		Collection<String> availableTypes = RdfResultMediaType.contentTypes();
		String contentType = parseAcceptHeader(req, availableTypes, defaultType);
		return RdfResultMediaType.fromContentType(contentType);
	}

	private boolean isVclassRestricted(String vclassUri, HttpServletRequest req) {
		ObjectProperty property = new ObjectProperty();
		property.setURI(RDF_TYPE);
		AccessObject dops = new ObjectPropertyStatementAccessObject(ModelAccess
				.on(req).getOntModel(FULL_ASSERTIONS), AccessObject.SOME_URI, property,
				vclassUri);
		return !PolicyHelper.isAuthorizedForActions(req, dops, AccessOperation.PUBLISH);
	}

	private void sendEmptyModel(RdfResultMediaType mediaType,
			HttpServletResponse resp) throws IOException {
		resp.setContentType(mediaType.getContentType());
		Model m = ModelFactory.createDefaultModel();
		m.write(resp.getOutputStream(), mediaType.getJenaResponseFormat());
	}

	private String buildQuery(String vclassUri) {
		ParameterizedSparqlString pss = new ParameterizedSparqlString(QUERY_TEMPLATE);
		pss.setIri("vclass", vclassUri);
		return pss.toString();
	}

	private RDFService getRdfService(HttpServletRequest req) {
		return RDFServiceUtils.getRDFService(new VitroRequest(req));
	}

	private void executeQuery(String query, String contentType,
			RDFService rdfService, HttpServletResponse resp)
			throws QueryParseException, NotAcceptableException,
			InvalidQueryTypeException, AcceptHeaderParsingException,
			RDFServiceException, IOException {
		SparqlQueryApiExecutor executor = SparqlQueryApiExecutor.instance(
				rdfService, query, contentType);
		resp.setContentType(executor.getMediaType());
		executor.executeAndFormat(resp.getOutputStream());
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException {
		doGet(req, res);
	}

}
