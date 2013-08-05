/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController.PageRecord;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.utils.log.LogUtils;

/**
 * This servlet is for servicing requests for JSON objects/data.
 * It could be generalized to get other types of data ex. XML, HTML etc
 * @author bdc34
 *
 * Moved most of the logic into a group of JsonProducer classes. jeb228
 */
public class JsonServlet extends VitroHttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(JsonServlet.class);
    private static final int INDIVIDUALS_PER_PAGE = 30;
    public static final int REPLY_SIZE = 256;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
        log.debug(LogUtils.formatRequestProperties(log, "debug", req));

        VitroRequest vreq = new VitroRequest(req);
        if (vreq.getParameter("getEntitiesByVClass") != null) {
            if( vreq.getParameter("resultKey") == null) {
                new GetEntitiesByVClass(vreq).process(resp);
            } else {
            	new GetEntitiesByVClassContinuation(vreq).process(resp);
            }
        }else if( vreq.getParameter("getN3EditOptionList") != null ){
        	throw new IllegalArgumentException("The call invoked deprecated classes " +
        			"and the parameter for this call appeared nowhere in the code base, " +
        			"so it was removed in May, 2012.");
        }else if( vreq.getParameter("getSolrIndividualsByVClass") != null ){
            new GetSolrIndividualsByVClass(vreq).process(resp);
        }else if( vreq.getParameter("getVClassesForVClassGroup") != null ){
            new GetVClassesForVClassGroup(vreq).process(resp);
        } else if( vreq.getParameter("getSolrIndividualsByVClasses") != null ){
        	log.debug("AJAX request to retrieve individuals by vclasses");
        	new	GetSolrIndividualsByVClasses(vreq).process(resp);
        } else if( vreq.getParameter("getDataForPage") != null ){
            throw new IllegalArgumentException("The call invoked deprecated classes " +
                    "and the parameter for this call appeared nowhere in the code base, " +
                    "so it was removed in Aug 5th 2013.");                        
        }else if( vreq.getParameter("getRenderedSolrIndividualsByVClass") != null ){
            new GetRenderedSolrIndividualsByVClass(vreq).process(resp);
        }else if( vreq.getParameter("getRandomSolrIndividualsByVClass") != null ){
            new GetRandomSolrIndividualsByVClass(vreq).process(resp);
        }
        
    }
    

    public static JSONObject getSolrIndividualsByVClass(String vclassURI, HttpServletRequest req, ServletContext context) throws Exception {
        List<String> vclassURIs = Collections.singletonList(vclassURI);
        VitroRequest vreq = new VitroRequest(req);        
        
        Map<String, Object> map = getSolrVClassIntersectionResults(vclassURIs, vreq, context);
        //last parameter indicates single vclass instead of multiple vclasses
        return processVclassResultsJSON(map, vreq, false);                    
    }

    public static JSONObject getSolrIndividualsByVClasses(List<String> vclassURIs, HttpServletRequest req, ServletContext context) throws Exception {
   	 	VitroRequest vreq = new VitroRequest(req);   
   	 	log.debug("Retrieve solr results for vclasses" + vclassURIs.toString());
        Map<String, Object> map = getSolrVClassIntersectionResults(vclassURIs, vreq, context);
        log.debug("Results returned from Solr for " + vclassURIs.toString() + " are of size " + map.size());
        
        return processVclassResultsJSON(map, vreq, true);        
   }
    
    //Including version for Solr query for Vclass Intersections
    private static Map<String,Object> getSolrVClassIntersectionResults(List<String> vclassURIs, VitroRequest vreq, ServletContext context){
        log.debug("Retrieving Solr intersection results for " + vclassURIs.toString());
    	String alpha = IndividualListController.getAlphaParameter(vreq);
        int page = IndividualListController.getPageParameter(vreq);
        log.debug("Alpha and page parameters are " + alpha + " and " + page);
        Map<String,Object> map = null;
        try {
	         map = IndividualListController.getResultsForVClassIntersections(
	                 vclassURIs, 
	                 page, INDIVIDUALS_PER_PAGE,
	                 alpha, 
	                 vreq.getWebappDaoFactory().getIndividualDao(), 
	                 context);  
        } catch(Exception ex) {
        	log.error("Error in retrieval of search results for VClass " + vclassURIs.toString(), ex);
        }
            
        return map;
   }
 
    // Map given to process method includes the actual individuals returned from the search
//    public static JSONObject processVClassResults(Map<String, Object> map, VitroRequest vreq, ServletContext context, boolean multipleVclasses) throws Exception{
//         JSONObject rObj = processVclassResultsJSON(map, vreq, multipleVclasses);
//         return rObj;
//    } 

    public static Collection<String> getMostSpecificTypes(Individual individual, WebappDaoFactory wdf) {
        ObjectPropertyStatementDao opsDao = wdf.getObjectPropertyStatementDao();
        Map<String, String> mostSpecificTypes = opsDao.getMostSpecificTypesInClassgroupsForIndividual(individual.getURI());  
        return mostSpecificTypes.values();
    }

    public static String getDataPropertyValue(Individual ind, DataProperty dp, WebappDaoFactory wdf){
        String value = ind.getDataValue(dp.getURI());
        if( value == null || value.isEmpty() )
            return "";
        else
            return value;            
    }
    
    public static JSONObject getRandomSolrIndividualsByVClass(String vclassURI, HttpServletRequest req, ServletContext context) throws Exception {
        VitroRequest vreq = new VitroRequest(req);        
        
        Map<String, Object> map = getRandomSolrVClassResults(vclassURI, vreq, context);
        //last parameter indicates single vclass instead of multiple vclasses
        return processVclassResultsJSON(map, vreq, false);                            
    }

     //Including version for Random Solr query for Vclass Intersections
     private static Map<String,Object> getRandomSolrVClassResults(String vclassURI, VitroRequest vreq, ServletContext context){
         log.debug("Retrieving random Solr intersection results for " + vclassURI);

         int page = IndividualListController.getPageParameter(vreq);
         int pageSize = Integer.parseInt(vreq.getParameter("pageSize"));
         log.debug("page and pageSize parameters = " + page + " and " + pageSize);
         Map<String,Object> map = null;
         try {
 	         map = IndividualListController.getRandomResultsForVClass(
 	                 vclassURI, 
 	                 page, 
 	                 pageSize, 
 	                 vreq.getWebappDaoFactory().getIndividualDao(), 
 	                 context);  
         } catch(Exception ex) {
         	log.error("Error in retrieval of search results for VClass " + vclassURI, ex);
         }

         return map;
    }
    

     /**
      * Process results related to VClass or vclasses. Handles both single and multiple vclasses being sent.
      */
     public static JSONObject processVclassResultsJSON(Map<String, Object> map, VitroRequest vreq, boolean multipleVclasses) {
         JSONObject rObj = new JSONObject();
         VClass vclass=null;         
         
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
                     String name = e.nextElement();
                     log.debug("parameter: " + name);
                     for( String value : vreq.getParameterValues(name) ){
                         log.debug("value for " + name + ": '" + value + "'");
                     }            
                 }
             }
               
             //need an unfiltered dao to get firstnames and lastnames
             WebappDaoFactory fullWdf = vreq.getUnfilteredWebappDaoFactory();
                       
             String[] vitroClassIdStr = vreq.getParameterValues("vclassId");                            
             if ( vitroClassIdStr != null && vitroClassIdStr.length > 0){    
                 for(String vclassId: vitroClassIdStr) {
                     vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(vclassId);
                     if (vclass == null) {
                         log.error("Couldn't retrieve vclass ");   
                         throw new Exception ("Class " + vclassId + " not found");
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
                 //For now, utilize very last VClass (assume that that is the one to be employed)
                 //TODO: Find more general way of dealing with this
                 //put multiple ones in?
                 if(vclassIds.size() > 0) {
                     int numberVClasses = vclassIds.size();
                     vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(vclassIds.get(numberVClasses - 1));
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
}
