/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.controller;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.solr.SolrSetup;

public class IndividualListRdfController extends VitroHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(IndividualListRdfController.class.getName());
    
	public static final int ENTITY_LIST_CONTROLLER_MAX_RESULTS = 30000;
	    
    public void doGet (HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
    	    
    	// Make the query
    	String vclassUri = req.getParameter("vclass");
    	String queryStr = VitroSearchTermNames.RDFTYPE + ":\"" + vclassUri + "\"";
    	SolrQuery query = new SolrQuery(queryStr);
    	query.setStart(0)
    	     .setRows(ENTITY_LIST_CONTROLLER_MAX_RESULTS)
    	     .setFields(VitroSearchTermNames.URI);
    	     // For now, we're only displaying the url, so no need to sort.
    	     //.setSortField(VitroSearchTermNames.NAME_LOWERCASE_SINGLE_VALUED);

    	// Execute the query
        SolrServer solr = SolrSetup.getSolrServer(getServletContext());
        QueryResponse response = null;
        
        try {
            response = solr.query(query);            
        } catch (Throwable t) {
            log.error(t, t);            
        }

        if ( response == null ) {         
            throw new ServletException("Could not run search in IndividualListRdfController");        
        }

        SolrDocumentList docs = response.getResults();
        
        if (docs == null) {
            throw new ServletException("Could not run search in IndividualListRdfController");    
        }

        Model model = ModelFactory.createDefaultModel();
        for (SolrDocument doc : docs) {
            String uri = doc.get(VitroSearchTermNames.URI).toString();
            Resource resource = ResourceFactory.createResource(uri);
            RDFNode node = (RDFNode) ResourceFactory.createResource(vclassUri);
            model.add(resource, RDF.type, node);
        }

        res.setContentType(RDFXML_MIMETYPE); 
    	model.write(res.getOutputStream(), "RDF/XML");
    }
    
    public void doPost (HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException{
    	doGet(req,res);
    }
    
}
