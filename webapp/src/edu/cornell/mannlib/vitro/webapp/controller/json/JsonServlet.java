/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.utils.log.LogUtils;
import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.PageDataGetterUtils;

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
            new GetDataForPage(vreq).process(resp);
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
        return processVClassResults(map, vreq, context, false);                    
    }

    public static JSONObject getSolrIndividualsByVClasses(List<String> vclassURIs, HttpServletRequest req, ServletContext context) throws Exception {
   	 	VitroRequest vreq = new VitroRequest(req);   
   	 	log.debug("Retrieve solr results for vclasses" + vclassURIs.toString());
        Map<String, Object> map = getSolrVClassIntersectionResults(vclassURIs, vreq, context);
        log.debug("Results returned from Solr for " + vclassURIs.toString() + " are of size " + map.size());
        JSONObject rObj = processVClassResults(map, vreq, context, true);                    
        return rObj;     
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
    public static JSONObject processVClassResults(Map<String, Object> map, VitroRequest vreq, ServletContext context, boolean multipleVclasses) throws Exception{
         JSONObject rObj = PageDataGetterUtils.processVclassResultsJSON(map, vreq, multipleVclasses);
         return rObj;
    } 

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
        return processVClassResults(map, vreq, context, false);                    
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
    

}
