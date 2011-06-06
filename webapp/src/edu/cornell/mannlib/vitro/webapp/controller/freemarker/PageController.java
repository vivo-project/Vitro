/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.BrowseDataGetter;
import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.PageDataGetter;
import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.ClassGroupPageData;

/**
 * Controller for getting data for pages defined in the display model. 
 * 
 * This controller passes these variables to the template: 
 * page: a map with information about the page from the display model.
 *  
 * See implementations of PageDataGetter for more variables. 
 */
public class PageController extends FreemarkerHttpServlet{
    private static final Log log = LogFactory.getLog(PageController.class);
    
    protected final static String DEFAULT_TITLE = "Page";        
    protected final static String DEFAULT_BODY_TEMPLATE = "menupage.ftl";     

    protected static final String DATA_GETTER_MAP = "pageTypeToDataGetterMap";
 
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        try {            
            // get URL without hostname or servlet context
            String url = vreq.getRequestURI().substring(vreq.getContextPath().length()); 
        
            Map<String,Object> mapForTemplate = new HashMap<String,Object>();            
            String pageUri = "";                     
            Map<String,Object>page;
            try {
                pageUri = getPageUri( vreq , url );
                page = getMapForPage( vreq, pageUri );
                mapForTemplate.put( "page", page);
                if( page.containsKey("title") ){
                    mapForTemplate.put("title", page.get("title"));
                }
                mapForTemplate.put("dataServiceUrlIndividualsByVClass", UrlBuilder.getUrl("/dataservice?getSolrIndividualsByVClass=1&vclassId="));
            } catch (Throwable th) {
                return doNotFound(vreq);                
            }
            
            try{
                mapForTemplate.putAll( getAdditionalDataForPage( vreq, pageUri, page) );
            } catch( Throwable th){
                log.error(th,th);
                return doError(vreq);
            }
            
            ResponseValues rv = new TemplateResponseValues(getTemplate( mapForTemplate ), mapForTemplate);            
            return rv;
        } catch (Throwable e) {
            log.error(e, e);
            return new ExceptionResponseValues(e);
        }
    }

    private String getTemplate(Map<String, Object> mapForTemplate) {
        if( mapForTemplate.containsKey("page") ){
            Map page = (Map) mapForTemplate.get("page");
            if( page != null && page.containsKey("bodyTemplate"))
                return (String) page.get("bodyTemplate");
            else
                return DEFAULT_BODY_TEMPLATE;
        }else
            return DEFAULT_BODY_TEMPLATE;        
    }

    protected Map<String,Object> getAdditionalDataForPage(VitroRequest vreq, String pageUri, Map<String,Object>page ) {
        /* figure out if the page needs additional data added */
        Map<String,Object> data = new HashMap<String,Object>();
        List<String> types = (List<String>)page.get("types");
        if( types != null ){
            for( String type : types){
                Map<String,Object> moreData = null;
                try{
                    moreData = getAdditionalData(vreq,pageUri,page,type);
                    if( moreData != null)
                        data.putAll(moreData);
                }catch(Throwable th){
                    log.error(th,th);
                }                    
            }            
        }        
        return data;
    }

    protected Map<String,Object> getAdditionalData(
            VitroRequest vreq, String pageUri, Map<String, Object> page, String type) {        
        if(type == null || type.isEmpty())
            return Collections.emptyMap();
            
        PageDataGetter getter = getPageDataGetterMap(getServletContext()).get(type);
        
        if( getter != null ){
            try{
                return getter.getData(getServletContext(), vreq, pageUri, page, type);
            }catch(Throwable th){
                log.error(th,th);
                return Collections.emptyMap();
            }
        } else {
            return Collections.emptyMap();
        }
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

    public static Map<String,PageDataGetter> getPageDataGetterMap(ServletContext sc){
        setupDataGetters(sc);
        return (Map<String,PageDataGetter>)sc.getAttribute(DATA_GETTER_MAP);
    }        
    
    public static void putPageUri(HttpServletRequest req, String pageUri){
        req.setAttribute("pageURI", pageUri);
    }  

    public static void setupDataGetters(ServletContext context ){
        if( context != null && context.getAttribute(DATA_GETTER_MAP) == null ){
            context.setAttribute(DATA_GETTER_MAP, new HashMap<String,PageDataGetter>());
            
            /* register all page data getters with the PageController servlet.  
             * There should be a better way of doing this. */                        
            ClassGroupPageData cgpd = new ClassGroupPageData();
            getPageDataGetterMap(context).put(cgpd.getType(), cgpd);      
            BrowseDataGetter bdg = new BrowseDataGetter();
            getPageDataGetterMap(context).put(bdg.getType(), bdg);
        }
    }
}
