/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;

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

    protected static Map<String,PageDataGetter> typeToDataGetter;
    
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
            } catch (Throwable th) {
                return doNotFound(vreq);                
            }
            
            try{
                mapForTemplate.putAll( getAdditionalDataForPage( vreq, pageUri, page) );
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
            
        PageDataGetter getter = typeToDataGetter.get(type);
        
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
    
    public static void putPageUri(HttpServletRequest req, String pageUri){
        req.setAttribute("pageURI", pageUri);
    }
    
    private interface PageDataGetter{
        Map<String,Object> getData(ServletContext contect, VitroRequest vreq, String pageUri, Map<String, Object> page, String type );
        
        /** Gets the type that this class applies to */
        String getType();
    }
    
    /**
     * This will pass these variables to the template:
     * classGroupUri: uri of the classgroup associated with this page.
     * vClassGroup: a data structure that is the classgroup associated with this page.     
     */
    static private class ClassGroupPageData implements PageDataGetter{
        public Map<String,Object> getData(ServletContext context, VitroRequest vreq, String pageUri, Map<String, Object> page, String type ){
            HashMap<String, Object> data = new HashMap<String,Object>();
            String classGroupUri = vreq.getWebappDaoFactory().getPageDao().getClassGroupPage(pageUri);
            data.put("classGroupUri", classGroupUri);

            VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(context);            
            List<VClassGroup> vcgList = vcgc.getGroups(vreq.getPortalId());
            VClassGroup group = null;
            for( VClassGroup vcg : vcgList){
                if( vcg.getURI() != null && vcg.getURI().equals(classGroupUri)){
                    group = vcg;
                    break;
                }
            }
            if( classGroupUri != null && !classGroupUri.isEmpty() && group == null ){ 
                /*This could be for two reasons: one is that the classgroup doesn't exist
                 * The other is that there are no individuals in any of the classgroup's classes */
                group = vreq.getWebappDaoFactory().getVClassGroupDao().getGroupByURI(classGroupUri);
                if( group != null ){
                    List<VClassGroup> vcgFullList = vreq.getWebappDaoFactory().getVClassGroupDao()
                        .getPublicGroupsWithVClasses(false, true, false);
                    for( VClassGroup vcg : vcgFullList ){
                        if( classGroupUri.equals(vcg.getURI()) ){
                            group = vcg;
                            break;
                        }                                
                    }
                    if( group == null ){
                        log.error("Cannot get classgroup '" + classGroupUri + "' for page '" + pageUri + "'");
                    }
                }
                
            }
                        
            data.put("vClassGroup", group);  //may put null            
            return data;
        }
        
        public String getType(){
            return DisplayVocabulary.CLASSGROUP_PAGE_TYPE;
        } 
    }
    
    /* register all page data getters with the PageController servlet */
    static{ 
        typeToDataGetter = new HashMap<String,PageDataGetter>();
        ClassGroupPageData cgpd = new ClassGroupPageData();
        typeToDataGetter.put(cgpd.getType(), cgpd);
    }
    
}
