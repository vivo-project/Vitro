/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.DataPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.IndividualAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.SparqlUtils;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.SparqlUtils.AjaxControllerException;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;

/**
 * DataAutocompleteController generates autocomplete content
 * for data properties using sparql queries
 * .
 */

@WebServlet(name = "DataAutocompleteController", urlPatterns = {"/dataautocomplete"} )
public class DataAutocompleteController extends VitroAjaxController {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(DataAutocompleteController.class);

    private static final String PARAM_QUERY = "term";
    //To get the data property
    private static final String PARAM_PROPERTY = "property";
    
    private static final int DEFAULT_MAX_HIT_COUNT = 1000;

    private static final String QUERY_TEXT = ""
            + "SELECT DISTINCT ?subject ?prop ?object ?dataLiteral \n"
            + "WHERE {\n"
            + "  ?subject ?prop ?object .\n"
            + "  VALUES ?prop { ?property }\n"
            + "  BIND(STR(?object) as ?dataLiteral )\n"
            + "  FILTER(REGEX(?dataLiteral, ?term, \"i\"))\n"
            + "}\n"
            + "ORDER BY ?dataLiteral\n "
            + "LIMIT " + DEFAULT_MAX_HIT_COUNT;

    String NORESULT_MSG = "";

    @Override
    protected AuthorizationRequest requiredActions(VitroRequest vreq) {
    	return SimplePermission.USE_BASIC_AJAX_CONTROLLERS.ACTION;
    }

    @Override
    protected void doRequest(VitroRequest vreq, HttpServletResponse response)
        throws IOException, ServletException {

        try {
            //This is the literal being searched for
            String qtxt = vreq.getParameter(PARAM_QUERY);
            String property = vreq.getParameter(PARAM_PROPERTY);
            //Get sparql query for this property
            String sparqlQuery = getSparqlQuery(qtxt, property);
            //Get Model and execute query
            OntModel model = getModel(vreq);
            Query query = SparqlUtils.createQuery(sparqlQuery);
			outputResults(response, query, model, vreq);
        } catch(AjaxControllerException ex) {
        	log.error(ex, ex);
			response.sendError(ex.getStatusCode());
        }
        catch (Throwable e) {
            log.error(e, e);
            //doSearchError(response);
        }
    }

    private void outputResults(HttpServletResponse response, Query query,
			OntModel model, VitroRequest vreq)     throws IOException{
    	Dataset dataset = DatasetFactory.create(model);
    	//Iterate through results and print out array of strings
    	List<String> outputResults = new ArrayList<String>();
		QueryExecution qe = QueryExecutionFactory.create(query, dataset);
		try {
			ResultSet results = qe.execSelect();
			while(results.hasNext()) {
	    		QuerySolution qs = results.nextSolution();
	    		RDFNode subject = qs.get("subject");
	    		if (!subject.isResource() || subject.isAnon()) {
	    		    continue;
	    		}
	    		RDFNode property = qs.get("prop");
	    		if (!property.isResource() || property.isAnon()) {
	    		    continue;
	    		}
	    		RDFNode object = qs.get("object");
	    		if (!object.isLiteral()) {
	    		    continue;
	    		}
	    		String subjectUri = subject.asResource().getURI();
	    		String propertyUri = property.asResource().getURI();
	    		String objectValue = object.asLiteral().getLexicalForm();
	    		if (!isAuthorized(vreq, qs, model, subjectUri, propertyUri, objectValue)) {
	    		    continue;
	    		}
	    		Literal dataLiteral = qs.getLiteral("dataLiteral");
	    		String dataValue = dataLiteral.getString();
	    		outputResults.add(dataValue);
	    	}
	        ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();
			for (String res : outputResults) {
				jsonArray.add(res);
			}
	        try {
	        	response.getWriter().write(jsonArray.toString());
	        } catch (Throwable e) {
	            log.error(e, e);
	            doSearchError(response);
	        }
		} finally {
			qe.close();
		}
	}

	private boolean isAuthorized(VitroRequest vreq, QuerySolution qs, OntModel model, String subjectUri, String propertyUri, String objectValue) {
	    AccessObject dataPropertyStatementAccessObject = new DataPropertyStatementAccessObject(model, subjectUri, propertyUri, objectValue);
	    AuthorizationRequest dataPropertyRequest = new SimpleAuthorizationRequest(dataPropertyStatementAccessObject, AccessOperation.DISPLAY);
	    if (VitroVocabulary.LABEL.equals(propertyUri)) {
	        AccessObject individualAccessObject = new IndividualAccessObject(subjectUri);
	        individualAccessObject.setModel(model);
	        AuthorizationRequest individualRequest = new SimpleAuthorizationRequest(individualAccessObject, AccessOperation.DISPLAY);
	        AuthorizationRequest requests = dataPropertyRequest.and(individualRequest);
	        return PolicyHelper.isAuthorizedForActions(vreq, requests);
	    }
	    return PolicyHelper.isAuthorizedForActions(vreq, dataPropertyRequest);
	}

	private OntModel getModel(VitroRequest vreq) throws AjaxControllerException{
		OntModel model = vreq.getJenaOntModel();

		if (model == null) {
			throw new AjaxControllerException(SC_INTERNAL_SERVER_ERROR,
					"Model '' not found.");
		}
		return model;
    }

	private String getSparqlQuery(String qtxt, String property) {
		//Searches for data literals whose string version begins with the text denoted
	    ParameterizedSparqlString pss = new ParameterizedSparqlString(QUERY_TEXT);
	    pss.setIri(PARAM_PROPERTY, property);
	    pss.setLiteral(PARAM_QUERY, "^" + qtxt);
	    return pss.toString();
	}


	 private void doSearchError(HttpServletResponse response) throws IOException {
	        // For now, we are not sending an error message back to the client because
	        // with the default autocomplete configuration it chokes.
	        doNoSearchResults(response);
	    }

	    private void doNoSearchResults(HttpServletResponse response) throws IOException {
	        response.getWriter().write("[]");
	    }

}
