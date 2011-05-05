package edu.cornell.mannlib.vitro.webapp.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * This is a sparql servlet that is for use as a public sparql endpoint.
 * It should only allow querying from the basic abox.
 *  
 * @author bdc34
 *
 */
public class PublicSparqlServlet extends SparqlQueryServlet{

    /**
     * Don't allow the request to specify a graph.
     */
    @Override
    protected Dataset chooseDatasetForQuery(VitroRequest vreq) {
        Model model = vreq.getJenaOntModel();
        return DatasetFactory.create(model);      
    }
    
    /**
     * Allow the public to use this servlet.
     */
    @Override    
    protected boolean hasPermission(HttpServletRequest request, HttpServletResponse response){
      return true;
    }
}
