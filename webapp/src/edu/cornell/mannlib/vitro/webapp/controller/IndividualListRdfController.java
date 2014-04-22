/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.controller;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;

public class IndividualListRdfController extends VitroHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(IndividualListRdfController.class.getName());
    
	public static final int ENTITY_LIST_CONTROLLER_MAX_RESULTS = 30000;
	    
    @Override
	public void doGet (HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
    	    
    	// Make the query
    	String vclassUri = req.getParameter("vclass");
    	String queryStr = VitroSearchTermNames.RDFTYPE + ":\"" + vclassUri + "\"";
    	SearchQuery query = ApplicationUtils.instance().getSearchEngine().createQuery(queryStr);
    	query.setStart(0)
    	     .setRows(ENTITY_LIST_CONTROLLER_MAX_RESULTS)
    	     .addFields(VitroSearchTermNames.URI);
    	     // For now, we're only displaying the url, so no need to sort.
    	     //.addSortField(VitroSearchTermNames.NAME_LOWERCASE_SINGLE_VALUED);

    	// Execute the query
		SearchEngine solr = ApplicationUtils.instance().getSearchEngine();
        SearchResponse response = null;
        
        try {
            response = solr.query(query);            
        } catch (Throwable t) {
            log.error(t, t);            
        }

        if ( response == null ) {         
            throw new ServletException("Could not run search in IndividualListRdfController");        
        }

        SearchResultDocumentList docs = response.getResults();
        
        if (docs == null) {
            throw new ServletException("Could not run search in IndividualListRdfController");    
        }

        Model model = ModelFactory.createDefaultModel();
        for (SearchResultDocument doc : docs) {
            String uri = doc.getStringValue(VitroSearchTermNames.URI);
            Resource resource = ResourceFactory.createResource(uri);
            RDFNode node = ResourceFactory.createResource(vclassUri);
            model.add(resource, RDF.type, node);
        }

        res.setContentType(RDFXML_MIMETYPE); 
    	model.write(res.getOutputStream(), "RDF/XML");
    }
    
    @Override
	public void doPost (HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException{
    	doGet(req,res);
    }
    
}
