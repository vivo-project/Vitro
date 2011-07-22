/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.JsonServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController.PageRecord;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class DataGetterUtils {
    protected static final String DATA_GETTER_MAP = "pageTypeToDataGetterMap";
    private static final Log log = LogFactory.getLog(DataGetterUtils.class);

    public static Map<String,Object> getDataForPage(String pageUri, VitroRequest vreq, ServletContext context) {
        //Based on page type get the appropriate data getter
        Map<String, Object> page = getMapForPage(vreq, pageUri);
        //Get data getters map
        Map<String, PageDataGetter> dataGetterMap = getPageDataGetterMap(context);
        //Get types associated with page
        Map<String,Object> data = new HashMap<String,Object>();
        List<String> dataGetters = (List<String>)page.get("dataGetters");
        log.debug("Retrieved data getters for Page " + pageUri + " = " + dataGetters.toString());
        if( dataGetters != null ){
            for( String dataGetter : dataGetters){
                Map<String,Object> moreData = null;
                PageDataGetter getter = dataGetterMap.get(dataGetter);
                log.debug("Retrieved data getter for " + dataGetter);
                try{
                    moreData = getAdditionalData(pageUri, dataGetter, page, vreq, getter, context);
                    if( moreData != null)
                        data.putAll(moreData);
                }catch(Throwable th){
                    log.error(th,th);
                }                    
            }            
        }        
        return data;
    }

    /**
     * 
     * Convert data to JSON for page uri based on type and related datagetters
     * TODO: How to handle different data getters?  Will this replace json fields or add to them?
     */
    public static JSONObject covertDataToJSONForPage(String pageUri, Map<String, Object> data, VitroRequest vreq, ServletContext context) {
        //Based on page type get the appropriate data getter
        Map<String, Object> page = getMapForPage(vreq, pageUri);
        //Get data getters map
        Map<String, PageDataGetter> dataGetterMap = getPageDataGetterMap(context);
        //Get types associated with page
        JSONObject rObj = null;
        List<String> types = (List<String>)page.get("types");
        if( types != null ){
            for( String type : types){
                JSONObject typeObj = null;
                PageDataGetter getter = dataGetterMap.get(type);
                try{
                    typeObj = getter.convertToJSON(data, vreq);
                    if( typeObj != null) {
                        //Copy over everything from this type Obj to 
                        //TODO: Review how to handle duplicate keys, etc.
                        if(rObj != null) {
                            //For now, just nests as separate entry
                            rObj.put(type, typeObj);
                        } else {
                            rObj = typeObj;
                        }
                    }
                        
                }catch(Throwable th){
                    log.error(th,th);
                }                    
            }            
        }        
        return rObj;
    }
    /*
     * Returns map with all page attributes from display model
     */
    public static Map<String, Object> getMapForPage(VitroRequest vreq, String pageUri) {
        //do a query to the display model for attributes of this page.        
        return vreq.getWebappDaoFactory().getPageDao().getPage(pageUri);
    }
    
    public static void setupDataGetters(ServletContext context){
        if( context != null && context.getAttribute(DATA_GETTER_MAP) == null ){
            context.setAttribute(DATA_GETTER_MAP, new HashMap<String,PageDataGetter>());
                
            /* register all page data getters with the PageController servlet.  
             * There should be a better way of doing this. */                        
            ClassGroupPageData cgpd = new ClassGroupPageData();
            getPageDataGetterMap(context).put(cgpd.getType(), cgpd);      
            BrowseDataGetter bdg = new BrowseDataGetter();
            getPageDataGetterMap(context).put(bdg.getType(), bdg);
            //TODO: Check if can include by type here
            IndividualsForClassesDataGetter cidg =  new IndividualsForClassesDataGetter();
            getPageDataGetterMap(context).put(cidg.getType(), cidg);
                
        }
    }
    
    public static Map<String,PageDataGetter> getPageDataGetterMap(ServletContext sc){
        setupDataGetters(sc);
        return (Map<String,PageDataGetter>)sc.getAttribute(DATA_GETTER_MAP);
    }
    /*
    //Based on page Uri, do conversions
    public static PageDataGetter getDataGetterForType(String type, ServletContext sc) {
        Map<String, PageDataGetter> map = getPageDataGetterMap(sc);
        PageDataGetter pdg = (PageDataGetter) map.get(type);
        return pdg;
    }*/
    
    protected static Map<String,Object> getAdditionalData(
            String pageUri, String dataGetterName, Map<String, Object> page, VitroRequest vreq, PageDataGetter getter, ServletContext context) {        
        if(dataGetterName == null || dataGetterName.isEmpty())
            return Collections.emptyMap();
           
      
        if( getter != null ){
            try{
            	log.debug("Retrieve data for this data getter for " + pageUri);
                return getter.getData(context, vreq, pageUri, page);
            }catch(Throwable th){
                log.error(th,th);
                return Collections.emptyMap();
            }
        } else {
            return Collections.emptyMap();
        }
    }
    
    /**
     * Get Individual count for Solr query for intersection of multiple classes
     */
    public static long getIndividualCountForIntersection(VitroRequest vreq, ServletContext context, List<String> classUris) {
    	 return IndividualListController.getIndividualCount(classUris, vreq.getWebappDaoFactory().getIndividualDao(), context);
    }
    
    
    /**
     * Process results related to VClass or vclasses. Handles both single and multiple vclasses being sent.
     */
    public static JSONObject processVclassResultsJSON(Map<String, Object> map, VitroRequest vreq, boolean multipleVclasses) {
        JSONObject rObj = new JSONObject();
        VClass vclass=null; 
        String errorMessage = null;
        
        try { 
              
            // Properties from ontologies used by VIVO - should not be in vitro
            DataProperty fNameDp = (new DataProperty());                         
            fNameDp.setURI("http://xmlns.com/foaf/0.1/firstName");
            DataProperty lNameDp = (new DataProperty());
            lNameDp.setURI("http://xmlns.com/foaf/0.1/lastName");
            DataProperty preferredTitleDp = (new DataProperty());
            preferredTitleDp.setURI("http://vivoweb.org/ontology/core#preferredTitle");
              
            if( log.isDebugEnabled() ){
                @SuppressWarnings("unchecked")
                Enumeration<String> e = vreq.getParameterNames();
                while(e.hasMoreElements()){
                    String name = (String)e.nextElement();
                    log.debug("parameter: " + name);
                    for( String value : vreq.getParameterValues(name) ){
                        log.debug("value for " + name + ": '" + value + "'");
                    }            
                }
            }
              
            //need an unfiltered dao to get firstnames and lastnames
            WebappDaoFactory fullWdf = vreq.getFullWebappDaoFactory();
                      
            String[] vitroClassIdStr = vreq.getParameterValues("vclassId");                            
            if ( vitroClassIdStr != null && vitroClassIdStr.length > 0){    
                for(String vclassId: vitroClassIdStr) {
                    vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(vclassId);
                    if (vclass == null) {
                        log.error("Couldn't retrieve vclass ");   
                        throw new Exception (errorMessage = "Class " + vclassId + " not found");
                    }  
                  }
            }else{
                log.error("parameter vclassId URI parameter expected ");
                throw new Exception("parameter vclassId URI parameter expected ");
            }
            List<String> vclassIds = Arrays.asList(vitroClassIdStr);                           
            //if single vclass expected, then include vclass. This relates to what the expected behavior is, not size of list 
            if(!multipleVclasses) {
                //currently used for ClassGroupPage
                rObj.put("vclass", 
                          new JSONObject().put("URI",vclass.getURI())
                                  .put("name",vclass.getName()));
            } else {
                //For now, utilize very first first VClass (assume that that is the one to be employed)
                //TODO: Find more general way of dealing with this
                //put multiple ones in?
                if(vclassIds.size() > 0) {
                    vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(vclassIds.get(0));
                    rObj.put("vclass", new JSONObject().put("URI",vclass.getURI())
                              .put("name",vclass.getName()));
                } 
                // rObj.put("vclasses",  new JSONObject().put("URIs",vitroClassIdStr)
                //                .put("name",vclass.getName()));
            }
            if (vclass != null) {                                    
                  
                rObj.put("totalCount", map.get("totalCount"));
                rObj.put("alpha", map.get("alpha"));
                                  
                List<Individual> inds = (List<Individual>)map.get("entities");
                log.debug("Number of individuals returned from request: " + inds.size());
                JSONArray jInds = new JSONArray();
                for(Individual ind : inds ){
                    JSONObject jo = new JSONObject();
                    jo.put("URI", ind.getURI());
                    jo.put("label",ind.getRdfsLabel());
                    jo.put("name",ind.getName());
                    jo.put("thumbUrl", ind.getThumbUrl());
                    jo.put("imageUrl", ind.getImageUrl());
                    jo.put("profileUrl", UrlBuilder.getIndividualProfileUrl(ind, vreq));
                      
                    jo.put("mostSpecificTypes", JsonServlet.getMostSpecificTypes(ind,fullWdf));                                          
                    jo.put("preferredTitle", JsonServlet.getDataPropertyValue(ind, preferredTitleDp, fullWdf));                    
                      
                    jInds.put(jo);
                }
                rObj.put("individuals", jInds);
                  
                JSONArray wpages = new JSONArray();
                //Made sure that PageRecord here is SolrIndividualListController not IndividualListController
                List<PageRecord> pages = (List<PageRecord>)map.get("pages");                
                for( PageRecord pr: pages ){                    
                    JSONObject p = new JSONObject();
                    p.put("text", pr.text);
                    p.put("param", pr.param);
                    p.put("index", pr.index);
                    wpages.put( p );
                }
                rObj.put("pages",wpages);    
                  
                JSONArray jletters = new JSONArray();
                List<String> letters = Controllers.getLetters();
                for( String s : letters){
                    JSONObject jo = new JSONObject();
                    jo.put("text", s);
                    jo.put("param", "alpha=" + URLEncoder.encode(s, "UTF-8"));
                    jletters.put( jo );
                }
                rObj.put("letters", jletters);
            }            
        } catch(Exception ex) {
             log.error("Error occurred in processing JSON object", ex);
        }
        return rObj;
    }
    
    /*
     * Copied from JSONServlet as expect this to be related to VitroClassGroup
     */
    public static JSONObject processVClassGroupJSON(VitroRequest vreq, ServletContext context, VClassGroup vcg) {
        JSONObject map = new JSONObject();           
        try {
            ArrayList<JSONObject> classes = new ArrayList<JSONObject>(vcg.size());
            for( VClass vc : vcg){
                JSONObject vcObj = new JSONObject();
                vcObj.put("name", vc.getName());
                vcObj.put("URI", vc.getURI());
                vcObj.put("entityCount", vc.getEntityCount());
                classes.add(vcObj);
            }
            map.put("classes", classes);                
            map.put("classGroupName", vcg.getPublicName());
            map.put("classGroupUri", vcg.getURI());
        
        } catch(Exception ex) {
            log.error("Error occurred in processing VClass group ", ex);
        }
        return map;        
    }
    
    
    
}