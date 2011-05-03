/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.controller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneIndexFactory;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;



public class EntityURLController extends VitroHttpServlet {
	 private static final Log log = LogFactory.getLog(EntityURLController.class.getName());
	 public static final int ENTITY_LIST_CONTROLLER_MAX_RESULTS = 30000;
	 
public void doGet (HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException{
	 
	String url = req.getRequestURI().substring(req.getContextPath().length());
	ContentType contentType = checkForRequestType(req.getHeader("accept"));
	
	if(Pattern.compile("^/entityurl/$").matcher(url).matches()){
		String redirectURL = null;
		if(contentType!=null){
			if ( RDFXML_MIMETYPE.equals(contentType.getMediaType()))
				redirectURL = "/entityurl/entityurl.rdf";
			else if( N3_MIMETYPE.equals(contentType.getMediaType()))
				redirectURL = "/entityurl/entityurl.n3";
			else if ( TTL_MIMETYPE.equals(contentType.getMediaType()))
				redirectURL = "/entityurl/entityurl.ttl";
		}
		else{
			redirectURL = "/entityurl/entityrurl.rdf";
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
	       res.setStatus(res.SC_SEE_OTHER);
		return;
	}
	
	String classUri = (String) getServletContext().getAttribute("classuri");
	BooleanQuery query = new BooleanQuery();
	 query.add(
             new TermQuery( new Term(Entity2LuceneDoc.term.RDFTYPE, classUri)),
             BooleanClause.Occur.MUST );     
	 
	 IndexSearcher index = LuceneIndexFactory.getIndexSearcher(getServletContext());
     TopDocs docs = index.search(query, null, 
             ENTITY_LIST_CONTROLLER_MAX_RESULTS, 
             new Sort(Entity2LuceneDoc.term.NAMELOWERCASE));   
     
     if( docs == null ){
         log.error("Search of lucene index returned null");
         throw new ServletException("Search of lucene index returned null");
     }
	 
     int ii = 0;
     int size = docs.totalHits;
     Resource resource = null;
     RDFNode node = null;
     Model model = ModelFactory.createDefaultModel();
     while( ii < size ){
         ScoreDoc hit = docs.scoreDocs[ii];
         if (hit != null) {
             Document doc = index.doc(hit.doc);
             if (doc != null) {                                                                                        
                 String uri = doc.getField(Entity2LuceneDoc.term.URI).stringValue();
                 resource = ResourceFactory.createResource(uri);
                 node = (RDFNode) ResourceFactory.createResource(classUri);
                 model.add(resource, RDF.type, node);
             } else {
                 log.warn("no document found for lucene doc id " + hit.doc);
             }
         } else {
             log.debug("hit was null");
         }                         
         ii++;            
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
