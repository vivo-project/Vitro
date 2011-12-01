/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaOutputUtils;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;

public class OntologyController extends VitroHttpServlet{
    private static final Log log = LogFactory.getLog(OntologyController.class.getName());
    
    private String default_jsp      = Controllers.BASIC_JSP;
    private ApplicationBean appBean;
    
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException,IOException{
		doGet(request, response);
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException,IOException{
		super.doGet(req, res);            
       
        //get URL without hostname or servlet context
        String url = req.getRequestURI().substring(req.getContextPath().length()); 
        
        String redirectURL = checkForRedirect ( url, req.getHeader("accept") );
       
        if( redirectURL != null ){
        	doRedirect( req, res, redirectURL );
        	return;
        }            	    
		
        ContentType rdfFormat = checkForLinkedDataRequest(url,req.getHeader("accept"));
   
       if( rdfFormat != null ){
        	doRdf(req, res, rdfFormat );
        	return;
        }         
	}
	
	private static Pattern RDF_REQUEST = Pattern.compile("^/ontology/([^/]*)/([^/]*).rdf$");
    private static Pattern N3_REQUEST = Pattern.compile("^/ontology/([^/]*)/([^/]*).n3$");
    private static Pattern TTL_REQUEST = Pattern.compile("^/ontology/([^/]*)/([^/]*).ttl$");
    private static Pattern HTML_REQUEST = Pattern.compile("^/ontology/([^/]*)$");
	
	protected ContentType checkForLinkedDataRequest(String url, String acceptHeader) {		
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
			
			/*
			 * check for parts of URL that indicate request for RDF
			   http://vivo.cornell.edu/ontology/(ontologyname)/n23.rdf
			   http://vivo.cornell.edu/ontology/(ontologyname)/n23.n3
			   http://vivo.cornell.edu/ontology/(ontologyname)/n23.ttl
			 */
						
			Matcher m = RDF_REQUEST.matcher(url);
			if( m.matches() ){
				return new ContentType(RDFXML_MIMETYPE);}
			m = N3_REQUEST.matcher(url);
			if( m.matches() ){
				return new ContentType(N3_MIMETYPE);}
			m = TTL_REQUEST.matcher(url);
			if( m.matches() ){
				return new ContentType(TTL_MIMETYPE);}
			
		} catch (Throwable th) {
			log.error("problem while checking accept header " , th);
		} 
		//return null;
		// Returning null would default to html in the calling method.
		// But since we don't have a useful html representation yet,
		// we're going to default to returning RDF/XML.
		return new ContentType(RDFXML_MIMETYPE);
	}  
	
	private void doRdf(HttpServletRequest req, HttpServletResponse res,
			ContentType rdfFormat) throws IOException, ServletException {   
		
		VitroRequest vreq = new VitroRequest(req);
		int index = vreq.getRequestURL().lastIndexOf("/");
	     String ontology = vreq.getRequestURL().substring(0, index);
	     String classOrProperty = vreq.getRequestURL().substring(index+1);
	     if(classOrProperty.lastIndexOf(".")!= -1){
	    	 int indexx = classOrProperty.lastIndexOf(".");
	    	 classOrProperty = classOrProperty.substring(0, indexx);
	     }
	     String url = ontology;
	     
	    		
		OntModel ontModel = null;
		HttpSession session = vreq.getSession(false);
		if( session != null )
			ontModel =(OntModel)session.getAttribute("jenaOntModel");		
		if( ontModel == null)
			ontModel = (OntModel)getServletContext().getAttribute("jenaOntModel");

        boolean found = false;
        Model newModel = ModelFactory.createDefaultModel();			
		ontModel.enterCriticalSection(Lock.READ);
        try{
            OntResource ontResource = ontModel.getOntResource(url);
            if(ontResource == null)
                ontResource = ontModel.getOntResource(url + "/");
            if(ontResource != null){
                found = true;
                Resource resource = (Resource)ontResource;
                QueryExecution qexec = null;
                try{
                    String queryString = "Describe <" + resource.getURI() + ">"; 
                    qexec = QueryExecutionFactory.create(QueryFactory.create(queryString), ontModel);
                    newModel = qexec.execDescribe();
                } finally{
                    qexec.close();
                }
            } else {
                found = false;
            }
        }finally{
            ontModel.leaveCriticalSection();
        }

        if( ! found ){
            //respond to HTTP outside of critical section
            doNotFound(req,res);
            return;
        } else {	
        	JenaOutputUtils.setNameSpacePrefixes(newModel,vreq.getWebappDaoFactory());
            res.setContentType(rdfFormat.getMediaType());
            String format = ""; 
            if ( RDFXML_MIMETYPE.equals(rdfFormat.getMediaType()))
                format = "RDF/XML";
            else if( N3_MIMETYPE.equals(rdfFormat.getMediaType()))
                format = "N3";
            else if ( TTL_MIMETYPE.equals(rdfFormat.getMediaType()))
                format ="TTL";
		
            newModel.write( res.getOutputStream(), format );		
            return;
        }
	}
	
	private static Pattern URI_PATTERN = Pattern.compile("^/ontology/([^/]*)/([^/]*)$");
    //Redirect if the request is for http://hostname/individual/localname
    // if accept is nothing or text/html redirect to ???
    // if accept is some RDF thing redirect to the URL for RDF
	private String checkForRedirect(String url, String acceptHeader) {
		ContentType c = checkForLinkedDataRequest(url, acceptHeader);	
		Matcher m = URI_PATTERN.matcher(url);
		if( m.matches() && m.groupCount() <=2 ){
			String group2="";
			
			if(m.group(2).indexOf(".")!=-1){
				group2 = m.group(2).substring(0, m.group(2).indexOf("."));
				System.out.println("group2 " + group2);
				System.out.println("group1 " + m.group(1));}
			
			
			if( c != null && !group2.trim().equals(m.group(1).trim()) ){
				String redirectUrl = null;
				if(m.group(2).isEmpty() || m.group(2) == null){
					redirectUrl = "/ontology/" + m.group(1) + "/" + m.group(1); }
				else{
					redirectUrl = "/ontology/" + m.group(1) + "/" + m.group(2) + "/" + m.group(2) ; }
					
				
				if( RDFXML_MIMETYPE.equals( c.getMediaType())  ){
					return redirectUrl + ".rdf";
				}else if( N3_MIMETYPE.equals( c.getMediaType() )){
					return redirectUrl + ".n3";
				}else if( TTL_MIMETYPE.equals( c.getMediaType() )){
					return redirectUrl + ".ttl";
				}//else send them to html													
			}
			//else redirect to HTML representation
			return null;
		}else{			
			return null;
		}
	}
	
	 private void doRedirect(HttpServletRequest req, HttpServletResponse res,
	            String redirectURL) throws IOException {
	        //It seems like there must be a more standard way to do a redirect in tomcat.
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
	    }
	 
	 
	private void doNotFound(HttpServletRequest req, HttpServletResponse res)
    throws IOException, ServletException {
        VitroRequest vreq = new VitroRequest(req);
        
        ApplicationBean appBean = ApplicationBean.getAppBean(getServletContext());

        //set title before we do the highlighting so we don't get markup in it.
        req.setAttribute("title","not found");
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);

        String css = "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
            + appBean.getThemeDir() + "css/entity.css\"/>"
            + "<script language='JavaScript' type='text/javascript' src='js/toggle.js'></script>";
        req.setAttribute("css",css);

        req.setAttribute("bodyJsp","/"+Controllers.ENTITY_NOT_FOUND_JSP);

        RequestDispatcher rd = req.getRequestDispatcher(Controllers.BASIC_JSP);
        rd.forward(req,res);
    }
	
	
	
	
}
