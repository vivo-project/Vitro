/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
import edu.cornell.mannlib.vitro.webapp.controller.individuallist.IndividualListResults;
import edu.cornell.mannlib.vitro.webapp.controller.individuallist.IndividualListResultsUtils;
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
        }else if( vreq.getParameter("getSearchIndividualsByVClass") != null ){
            new GetSearchIndividualsByVClass(vreq).process(resp);
        }else if( vreq.getParameter("getVClassesForVClassGroup") != null ){
            new GetVClassesForVClassGroup(vreq).process(resp);
        } else if( vreq.getParameter("getSearchIndividualsByVClasses") != null ){
        	log.debug("AJAX request to retrieve individuals by vclasses");
        	new	GetSearchIndividualsByVClasses(vreq).process(resp);
        } else if( vreq.getParameter("getDataForPage") != null ){
            throw new IllegalArgumentException("The call invoked deprecated classes " +
                    "and the parameter for this call appeared nowhere in the code base, " +
                    "so it was removed in Aug 5th 2013.");                        
        }else if( vreq.getParameter("getRenderedSearchIndividualsByVClass") != null ){
            new GetRenderedSearchIndividualsByVClass(vreq).process(resp);
        }else if( vreq.getParameter("getRandomSearchIndividualsByVClass") != null ){
            new GetRandomSearchIndividualsByVClass(vreq).process(resp);
        } else if( vreq.getParameter("getAllVClasses") != null ){
            new GetAllVClasses(vreq).process(resp);
        }
        
    }
    

    public static JSONObject getSearchIndividualsByVClass(String vclassURI, HttpServletRequest req) throws Exception {
        List<String> vclassURIs = Collections.singletonList(vclassURI);
        VitroRequest vreq = new VitroRequest(req);        
        
        IndividualListResults vcResults = getSearchVClassIntersectionResults(vclassURIs, vreq);
        //last parameter indicates single vclass instead of multiple vclasses
        return IndividualListResultsUtils.wrapIndividualListResultsInJson(vcResults, vreq, false);                    
    }

    public static JSONObject getSearchIndividualsByVClasses(List<String> vclassURIs, HttpServletRequest req) throws Exception {
   	 	VitroRequest vreq = new VitroRequest(req);   
   	 	log.debug("Retrieve search results for vclasses" + vclassURIs.toString());
        IndividualListResults vcResults = getSearchVClassIntersectionResults(vclassURIs, vreq);
        log.debug("Results returned from search engine for " + vclassURIs.toString() + " are of size " + vcResults.getTotalCount());
        
        return IndividualListResultsUtils.wrapIndividualListResultsInJson(vcResults, vreq, true);        
   }
    
    //Including version for search query for Vclass Intersections
    private static IndividualListResults getSearchVClassIntersectionResults(List<String> vclassURIs, VitroRequest vreq){
        log.debug("Retrieving search intersection results for " + vclassURIs.toString());
    	String alpha = IndividualListController.getAlphaParameter(vreq);
        int page = IndividualListController.getPageParameter(vreq);
        log.debug("Alpha and page parameters are " + alpha + " and " + page);
        try {
	         return IndividualListController.getResultsForVClassIntersections(
	                 vclassURIs, 
	                 page, INDIVIDUALS_PER_PAGE,
	                 alpha, 
	                 vreq);  
        } catch(Exception ex) {
        	log.error("Error in retrieval of search results for VClass " + vclassURIs.toString(), ex);
        	return IndividualListResults.EMPTY;
        }
   }
 
    public static String getDataPropertyValue(Individual ind, DataProperty dp, WebappDaoFactory wdf){
        String value = ind.getDataValue(dp.getURI());
        if( value == null || value.isEmpty() )
            return "";
        else
            return value;            
    }
    
    public static JSONObject getRandomSearchIndividualsByVClass(String vclassURI, HttpServletRequest req) throws Exception {
        VitroRequest vreq = new VitroRequest(req);        
        
        IndividualListResults vcResults = getRandomSearchVClassResults(vclassURI, vreq);
        //last parameter indicates single vclass instead of multiple vclasses
        return IndividualListResultsUtils.wrapIndividualListResultsInJson(vcResults, vreq, false);                            
    }

     //Including version for Random search query for Vclass Intersections
     private static IndividualListResults getRandomSearchVClassResults(String vclassURI, VitroRequest vreq){
         log.debug("Retrieving random search intersection results for " + vclassURI);

         int page = IndividualListController.getPageParameter(vreq);
         int pageSize = Integer.parseInt(vreq.getParameter("pageSize"));
         log.debug("page and pageSize parameters = " + page + " and " + pageSize);
         try {
 	         return IndividualListController.getRandomResultsForVClass(
 	                 vclassURI, 
 	                 page, 
 	                 pageSize, 
 	                 vreq);  
         } catch(Exception ex) {
         	log.error("Error in retrieval of search results for VClass " + vclassURI, ex);
         	return IndividualListResults.EMPTY;
         }
    }

}
