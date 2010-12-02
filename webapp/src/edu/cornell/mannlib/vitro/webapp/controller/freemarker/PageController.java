/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * Controller for getting data for pages defined in the display model. 
 */
public class PageController extends FreemarkerHttpServlet{
    private static final Log log = LogFactory.getLog(PageController.class);
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        try {
            // get URL without hostname or servlet context
            String url = vreq.getRequestURI().substring(vreq.getContextPath().length()); 
        
            Map<String,Object> mapForTemplate = new HashMap<String,Object>();            
            String pageUri = "";                                        
            try {
                pageUri = getPageUri( vreq , url );
                mapForTemplate.putAll( getMapForPage( vreq, pageUri ) );                
            } catch (Throwable th) {
                return doNotFound(vreq);                
            }
            
            try{
                mapForTemplate.putAll( getAdditionalDataForPage( vreq, pageUri) );
            } catch( Throwable th){
                log.error(th,th);
                return doError(vreq);
            }
            
            return new TemplateResponseValues(getTemplate( mapForTemplate ), mapForTemplate);       
        } catch (Throwable e) {
            log.error(e);
            return new ExceptionResponseValues(e);
        }
    }

    private String getTemplate(Map<String, Object> mapForTemplate) {
        if( mapForTemplate.containsKey("bodyTemplate"))
            return (String) mapForTemplate.get("bodyTemplate");
        else
            return DEFAULT_BODY_TEMPLATE;        
    }

    private Map<String,Object> getAdditionalDataForPage(VitroRequest vreq, String pageUri) {
        return Collections.emptyMap();
    }

    private ResponseValues doError(VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title","Page could not be created");
        body.put("errorMessage", "There was an error while creating the page, please check the logs.");        
        return new TemplateResponseValues(Template.TITLED_ERROR_MESSAGE.toString(), body, HttpServletResponse.SC_NOT_FOUND);
    }

    private ResponseValues doNotFound(VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title","Page Not Found");
        body.put("errorMessage", "The page was not found in the system.");        
        return new TemplateResponseValues(Template.TITLED_ERROR_MESSAGE.toString(), body, HttpServletResponse.SC_NOT_FOUND);
    }

    private Map<String,Object> getMapForPage(VitroRequest vreq, String pageUri) {
        //do a query to the display model for attributes of this page.        
        return vreq.getWebappDaoFactory().getPageDao().getPage(pageUri);
    }

    /**
     * Gets the page URI from the request.  The page must be defined in the display model.  
     * @throws Exception 
     */
    private String getPageUri(VitroRequest vreq, String url) throws Exception {
        //check if there is a page URI in the request.  This would have
        //been added by a servlet Filter.
        String pageURI = (String) vreq.getAttribute("pageURI");
        if( pageURI != null && ! pageURI.isEmpty() )
            return pageURI;
        else
            throw new Exception("no page found for " + vreq.getRequestURI() );
    }
    
    public static void putPageUri(HttpServletRequest req, String pageUri){
        req.setAttribute("pageURI", pageUri);
    }
    
    protected final static String DEFAULT_TITLE = "Page";
    
    //not sure what this should default to. 
    protected final static String DEFAULT_BODY_TEMPLATE = "menupage.ftl";
}
