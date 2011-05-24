/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.controller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
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

import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc.VitroLuceneTermNames;
import edu.cornell.mannlib.vitro.webapp.search.solr.SolrSetup;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;

public class IndividualListRdfController extends VitroHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(IndividualListRdfController.class.getName());
    
    private static final String PATH = "listrdf";    
    private static final String FILENAME = PATH;
    public static final String URL = "/" + PATH + "/";
    
	public static final int ENTITY_LIST_CONTROLLER_MAX_RESULTS = 30000;
	    
    public void doGet (HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
    	    
    	String url = req.getRequestURI().substring(req.getContextPath().length());
    	ContentType contentType = checkForRequestType(req.getHeader("accept"));
    	    	
    	if (url.equals(URL)) {
    		String redirectURL = URL + FILENAME;
    		if (contentType!=null) {
    			if (RDFXML_MIMETYPE.equals(contentType.getMediaType()))
    				redirectURL += ".rdf";
    			else if (N3_MIMETYPE.equals(contentType.getMediaType()))
    				redirectURL += ".n3";
    			else if (TTL_MIMETYPE.equals(contentType.getMediaType()))
    				redirectURL += ".ttl";
    		} else {
    			redirectURL += ".rdf";
    		}
    		
    		String hn = req.getHeader("Host");
            if (req.isSecure()) {
                res.setHeader("Location", res.encodeURL("https://" + hn
                        + req.getContextPath() + redirectURL));
                log.info("doRedirect by using HTTPS");
            } else {
                res.setHeader("Location", res.encodeURL("http://" + hn
                        + req.getContextPath() + redirectURL));
                log.info("doRedirect by using HTTP");
            }
    	    res.setStatus(HttpServletResponse.SC_SEE_OTHER);
    		return;
    	}
    	
    	// Make the query
    	String classUri = (String) getServletContext().getAttribute("classuri");
    	String queryStr = VitroLuceneTermNames.RDFTYPE + ":\"" + classUri + "\"";
    	SolrQuery query = new SolrQuery(queryStr);
    	query.setStart(0)
    	     .setRows(ENTITY_LIST_CONTROLLER_MAX_RESULTS)
    	     .setFields(VitroLuceneTermNames.URI)
    	     .setSortField(VitroLuceneTermNames.NAME_LOWERCASE_SINGLE_VALUED, SolrQuery.ORDER.asc);

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
            String uri = doc.get(VitroLuceneTermNames.URI).toString();
            Resource resource = ResourceFactory.createResource(uri);
            RDFNode node = (RDFNode) ResourceFactory.createResource(classUri);
            model.add(resource, RDF.type, node);
        }

    	String format = ""; 
    	if(contentType != null){	
    		if ( RDFXML_MIMETYPE.equals(contentType.getMediaType()))
    			format = "RDF/XML";
    		else if( N3_MIMETYPE.equals(contentType.getMediaType()))
    			format = "N3";
    		else if ( TTL_MIMETYPE.equals(contentType.getMediaType()))
    			format ="TTL";
    		res.setContentType(contentType.getMediaType());
    	}
    	else{
    		res.setContentType(RDFXML_MIMETYPE);
    		format = "RDF/XML";
    	}
    	model.write(res.getOutputStream(), format);
    }
    
    public void doPost (HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException{
    	doGet(req,res);
    }
    
    protected ContentType checkForRequestType(String acceptHeader) {		
    	try {
    		//check the accept header			
    		if (acceptHeader != null) {
    			List<ContentType> actualContentTypes = new ArrayList<ContentType>();				
    			actualContentTypes.add(new ContentType( XHTML_MIMETYPE ));
    			actualContentTypes.add(new ContentType( HTML_MIMETYPE ));				
    			
    			actualContentTypes.add(new ContentType( RDFXML_MIMETYPE ));
    			actualContentTypes.add(new ContentType( N3_MIMETYPE ));
    			actualContentTypes.add(new ContentType( TTL_MIMETYPE ));
    			
    							
    			ContentType best = ContentType.getBestContentType(acceptHeader,actualContentTypes);
    			if (best!=null && (
    					RDFXML_MIMETYPE.equals(best.getMediaType()) || 
    					N3_MIMETYPE.equals(best.getMediaType()) ||
    					TTL_MIMETYPE.equals(best.getMediaType()) ))
    				return best;				
    		}
    	}
    	catch (Throwable th) {
    		log.error("problem while checking accept header " , th);
    	}
    	return null;
    }
}
