/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetter;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetterUtils;
import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.PageDataGetterUtils;
/**
 * Controller for getting data for pages defined in the display model. 
 * 
 * This controller passes these variables to the template: 
 * page: a map with information about the page from the display model.
 * pageUri: the URI of the page that identifies the page in the model 
 *  (note that this is not the URL address of the page).
 *    
 * See implementations of PageDataGetter for more variables. 
 */
public class PageController extends FreemarkerHttpServlet{
    private static final Log log = LogFactory.getLog(PageController.class);
    
    protected final static String DEFAULT_TITLE = "Page";        
    protected final static String DEFAULT_BODY_TEMPLATE = "emptyPage.ftl";     

    protected static final String DATA_GETTER_MAP = "pageTypeToDataGetterMap";
 
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) throws Exception {
                   
        Map<String,Object> mapForTemplate = new HashMap<String,Object>();                                
        Map<String,Object>page;
        
        //figure out what page we are trying to get            
        String pageUri = getPageUri( vreq );
        if( StringUtils.isEmpty( pageUri ) )
            return doNoPageSpecified(vreq);              
        else           
            mapForTemplate.put("pageUri", pageUri);
        
        //try to get the page RDF from the model
        try{
            page =  vreq.getWebappDaoFactory().getPageDao().getPage(pageUri);                
            mapForTemplate.put( "page", page);
            if( page.containsKey("title") ){
                mapForTemplate.put("title", page.get("title"));
            }
        }catch( Throwable th){
            return doNotFound(vreq);
        }
        
        executePageDataGetters( pageUri, vreq, getServletContext(), mapForTemplate );
        executeDataGetters( pageUri, vreq, getServletContext(), mapForTemplate);

        ResponseValues rv = new TemplateResponseValues(getTemplate( mapForTemplate ), mapForTemplate);            
        return rv;       
    }

    private void executeDataGetters(String pageUri, VitroRequest vreq, ServletContext context, Map<String, Object> mapForTemplate) 
    throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        List<DataGetter> dgList = DataGetterUtils.getDataGettersForPage(vreq.getDisplayModel(), pageUri);
                        
        for( DataGetter dg : dgList){            
            Map<String,Object> moreData = dg.getData(context,vreq,mapForTemplate);            
            if( moreData != null ){
                mapForTemplate.putAll(moreData);
            }
        }                       
    }

    private void executePageDataGetters(String pageUri, VitroRequest vreq, ServletContext context, Map<String, Object> mapForTemplate) {                
        mapForTemplate.putAll( PageDataGetterUtils.getDataForPage(pageUri, vreq, context) );        
    }


    private String getTemplate(Map<String, Object> mapForTemplate) {
        //first try to get the body template from the display model RDF
        if( mapForTemplate.containsKey("page") ){
            Map page = (Map) mapForTemplate.get("page");
            if( page != null && page.containsKey("bodyTemplate")){
                return (String) page.get("bodyTemplate");
            }
        }
        //next, try to get body template from the data getter values
        if( mapForTemplate.containsKey("bodyTemplate") ){
            return (String) mapForTemplate.get("bodyTemplate");            
        }
        
        //Nothing? then use a default empty page
        return DEFAULT_BODY_TEMPLATE;        
    }


    private ResponseValues doError(VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title","Page could not be created");
        body.put("errorMessage", "There was an error while creating the page, please check the logs.");        
        return new TemplateResponseValues(Template.TITLED_ERROR_MESSAGE.toString(), body, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    private ResponseValues doNotFound(VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title","Page Not Found");
        body.put("errorMessage", "The page was not found in the system.");        
        return new TemplateResponseValues(Template.TITLED_ERROR_MESSAGE.toString(), body, HttpServletResponse.SC_NOT_FOUND);
    }


    private ResponseValues doNoPageSpecified(VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title","No page URI specified");
        body.put("errorMessage", "Could not generate page beacause it was unclear what page was being requested.  A URL mapping may be missing.");        
        return new TemplateResponseValues(Template.TITLED_ERROR_MESSAGE.toString(), body, HttpServletResponse.SC_NOT_FOUND);
    }
    
    /**
     * Gets the page URI from the request.  The page must be defined in the display model.  
     * @throws Exception 
     */
    private String getPageUri(VitroRequest vreq) throws Exception {
        // get URL without hostname or servlet context
        //bdc34: why are we getting this?
        String url = vreq.getRequestURI().substring(vreq.getContextPath().length());
        
        // Check if there is a page URI in the request.  
        // This would have been added by a servlet Filter.
        String pageURI = (String) vreq.getAttribute("pageURI");        
        return pageURI;        
    }
      
    
    public static void putPageUri(HttpServletRequest req, String pageUri){
        req.setAttribute("pageURI", pageUri);
    }  
    
    
}
