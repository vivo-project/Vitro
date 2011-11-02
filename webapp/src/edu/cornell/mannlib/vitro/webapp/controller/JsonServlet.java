/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.SelectListGenerator;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.DataGetterUtils;

/**
 * This servlet is for servicing requests for JSON objects/data.
 * It could be generalized to get other types of data ex. XML, HTML etc
 * @author bdc34
 *
 */
public class JsonServlet extends VitroHttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(JsonServlet.class);
    private static final int REPLY_SIZE = 256;
    private static final int INDIVIDUALS_PER_PAGE = 30;
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
        VitroRequest vreq = new VitroRequest(req);

        try{
            if(vreq.getParameter("getEntitiesByVClass") != null ){
                if( vreq.getParameter("resultKey") == null) {
                    getEntitiesByVClass(req, resp);
                    return;
                } else {
                    getEntitiesByVClassContinuation( req, resp);
                    return;
                }
            }else if( vreq.getParameter("getN3EditOptionList") != null ){
                doN3EditOptionList(req,resp);
                return;
            }else if( vreq.getParameter("getSolrIndividualsByVClass") != null ){
                getSolrIndividualsByVClass(req,resp);
                return;
            }else if( vreq.getParameter("getVClassesForVClassGroup") != null ){
                getVClassesForVClassGroup(req,resp);
                return;
            } else if( vreq.getParameter("getSolrIndividualsByVClasses") != null ){
            	log.debug("AJAX request to retrieve individuals by vclasses");
            	getSolrIndividualsByVClasses(req,resp);
                return;
            } else if( vreq.getParameter("getDataForPage") != null ){
            	getDataForPage(req,resp);
            	return;
            }
        }catch(Exception ex){
            log.warn(ex,ex);            
        }        
    }

   
	private void getVClassesForVClassGroup(HttpServletRequest req, HttpServletResponse resp) throws IOException, JSONException {                
        JSONObject map = new JSONObject();           
        VitroRequest vreq = new VitroRequest(req);        
        String vcgUri = vreq.getParameter("classgroupUri");
        if( vcgUri == null ){
            log.debug("no URI passed for classgroupUri");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(getServletContext());
        VClassGroup vcg = vcgc.getGroup(vcgUri);
        if( vcg == null ){
            log.debug("Could not find vclassgroup: " + vcgUri);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }        
                        
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
                
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        Writer writer = resp.getWriter();
        writer.write(map.toString());                        
    }

    private void getSolrIndividualsByVClass( HttpServletRequest req, HttpServletResponse resp ){
        String errorMessage = null;
        JSONObject rObj = null;
        try{            
            VitroRequest vreq = new VitroRequest(req);
            VClass vclass=null;
            
            
            String vitroClassIdStr = vreq.getParameter("vclassId");            
            if ( vitroClassIdStr != null && !vitroClassIdStr.isEmpty()){                             
                vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(vitroClassIdStr);
                if (vclass == null) {
                    log.debug("Couldn't retrieve vclass ");   
                    throw new Exception (errorMessage = "Class " + vitroClassIdStr + " not found");
                }                           
            }else{
                log.debug("parameter vclassId URI parameter expected ");
                throw new Exception("parameter vclassId URI parameter expected ");
            }
            vreq.setAttribute("displayType", vitroClassIdStr);
            rObj = getSolrIndividualsByVClass(vclass.getURI(),req, getServletContext());
        }catch(Exception ex){
            errorMessage = ex.toString();
            log.error(ex,ex);
        }

        if( rObj == null )
            rObj = new JSONObject();
        
        try{
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            
            if( errorMessage != null ){
                rObj.put("errorMessage", errorMessage);
                resp.setStatus(500 /*HttpURLConnection.HTTP_SERVER_ERROR*/);
            }else{
                rObj.put("errorMessage", "");
            }            
            Writer writer = resp.getWriter();
            writer.write(rObj.toString());
        }catch(JSONException jse){
            log.error(jse,jse);
        } catch (IOException e) {
            log.error(e,e);
        }
        
    }
    
    public static JSONObject getSolrIndividualsByVClass(String vclassURI, HttpServletRequest req, ServletContext context) throws Exception {
        List<String> vclassURIs = new ArrayList<String>();
        vclassURIs.add(vclassURI);
        VitroRequest vreq = new VitroRequest(req);        
        VClass vclass=null;
        JSONObject rObj = new JSONObject();
        
        Map<String, Object> map = getSolrVClassIntersectionResults(vclassURIs, vreq, context);
        //last parameter indicates single vclass instead of multiple vclasses
        rObj = processVClassResults(map, vreq, context, false);                    
        return rObj;                                               
    }

    
    // Accepts multiple vclasses and returns individuals which correspond to the intersection of those classes (i.e. have all those types) 
    private void getSolrIndividualsByVClasses( HttpServletRequest req, HttpServletResponse resp ){
        log.debug("Executing retrieval of individuals by vclasses");
    	String errorMessage = null;
        JSONObject rObj = null;
        try{            
            VitroRequest vreq = new VitroRequest(req);
            VClass vclass=null;
            log.debug("Retrieving solr individuals by vclasses");
            // Could have multiple vclass ids sent in
            String[] vitroClassIdStr = vreq.getParameterValues("vclassId");  
            if ( vitroClassIdStr != null && vitroClassIdStr.length > 0){    
            	for(String vclassId: vitroClassIdStr) {
            		log.debug("Iterating throug vclasses, using VClass " + vclassId);
	                vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(vclassId);
	                if (vclass == null) {
	                    log.error("Couldn't retrieve vclass ");   
	                    throw new Exception (errorMessage = "Class " + vclassId + " not found");
	                }   
            	}
            }else{
                log.error("parameter vclassId URI parameter expected but not found");
                throw new Exception("parameter vclassId URI parameter expected ");
            }
            List<String> vclassIds = Arrays.asList(vitroClassIdStr);
            rObj = getSolrIndividualsByVClasses(vclassIds,req, getServletContext());
        }catch(Exception ex){
            errorMessage = ex.toString();
            log.error(ex,ex);
        }

        if( rObj == null )
            rObj = new JSONObject();
        
        try{
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            
            if( errorMessage != null ){
                rObj.put("errorMessage", errorMessage);
                resp.setStatus(500 /*HttpURLConnection.HTTP_SERVER_ERROR*/);
            }else{
                rObj.put("errorMessage", "");
            }            
            Writer writer = resp.getWriter();
            writer.write(rObj.toString());
        }catch(JSONException jse){
            log.error(jse,jse);
        } catch (IOException e) {
            log.error(e,e);
        }
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
         JSONObject rObj = DataGetterUtils.processVclassResultsJSON(map, vreq, multipleVclasses);
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
    
    /**
     * Gets an option list for a given EditConfiguration and Field.
     * Requires following HTTP query parameters:
     * editKey
     * field  
     */
    private void doN3EditOptionList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.debug("in doN3EditOptionList()");
        String field = req.getParameter("field");
        if( field == null ){
            log.debug("could not find query parameter 'field' for doN3EditOptionList");
            throw new IllegalArgumentException(" getN3EditOptionList requires parameter 'field'");
        }
        
        HttpSession sess = req.getSession(false);
        EditConfiguration editConfig = EditConfiguration.getConfigFromSession(sess, req);
        if( editConfig == null ) {
            log.debug("could not find query parameter 'editKey' for doN3EditOptionList");
            throw new IllegalArgumentException(" getN3EditOptionList requires parameter 'editKey'");
        }
        
        if( log.isDebugEnabled() )
            log.debug(" attempting to get option list for field '" + field + "'");            
        
        // set ProhibitedFromSearch object so picklist doesn't show
        // individuals from classes that should be hidden from list views
    	OntModel displayOntModel = 
		    (OntModel) getServletConfig().getServletContext()
		    .getAttribute("displayOntModel");
    	if (displayOntModel != null) {
	     	ProhibitedFromSearch pfs = new ProhibitedFromSearch(
				DisplayVocabulary.SEARCH_INDEX_URI, displayOntModel);
	     	editConfig.setProhibitedFromSearch(pfs);
    	}
	
        Map<String,String> options = SelectListGenerator.getOptions(editConfig, field, (new VitroRequest(req)).getFullWebappDaoFactory());
                        
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        
        ServletOutputStream out = resp.getOutputStream();
        out.println("[");                
        for(String key : options.keySet()){
            JSONArray jsonObj = new JSONArray();            
            jsonObj.put( options.get(key));
            jsonObj.put( key);
            out.println("    " + jsonObj.toString() + ",");
        }        
        out.println("]");                       
    }

    private void getEntitiesByVClassContinuation(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("in getEntitiesByVClassContinuation()");
        VitroRequest vreq = new VitroRequest(req);
        String resKey = vreq.getParameter("resultKey");
        if( resKey == null )
            throw new ServletException("Could not get resultKey");
        HttpSession session = vreq.getSession();
        if( session == null )
            throw new ServletException("there is no session to get the pervious results from");
        @SuppressWarnings("unchecked")
        List<Individual> entsInVClass = (List<Individual>) session.getAttribute(resKey);
        if( entsInVClass == null )
            throw new ServletException("Could not find List<Individual> for resultKey " + resKey);

        List<Individual> entsToReturn = new ArrayList<Individual>(REPLY_SIZE);
        boolean more = false;
        int count = 0;
        /* we have a large number of items to send back so we need to stash the list in the session scope */
        if( entsInVClass.size() > REPLY_SIZE){
            more = true;
            ListIterator<Individual> entsFromVclass = entsInVClass.listIterator();
            while ( entsFromVclass.hasNext() && count <= REPLY_SIZE ){
                entsToReturn.add( entsFromVclass.next());
                entsFromVclass.remove();
                count++;
            }
            if( log.isDebugEnabled() ) log.debug("getEntitiesByVClassContinuation(): Creating reply with continue token," +
            		" sending in this reply: " + count +", remaing to send: " + entsInVClass.size() );  
        } else {
            //send out reply with no continuation
            entsToReturn = entsInVClass;
            count = entsToReturn.size();
            session.removeAttribute(resKey);
            if( log.isDebugEnabled()) log.debug("getEntitiesByVClassContinuation(): sending " + count + " Ind without continue token");
        }

        //put all the entities on the JSON array
        JSONArray ja =  individualsToJson( entsToReturn );

        //put the responseGroup number on the end of the JSON array
        if( more ){
            try{
                JSONObject obj = new JSONObject();
                obj.put("resultGroup", "true");
                obj.put("size", count);

                StringBuffer nextUrlStr = req.getRequestURL();
                nextUrlStr.append("?")
                        .append("getEntitiesByVClass").append( "=1&" )
                        .append("resultKey=").append( resKey );
                obj.put("nextUrl", nextUrlStr.toString());

                ja.put(obj);
            }catch(JSONException je ){
                throw new ServletException(je.getMessage());
            }
        }        
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        ServletOutputStream out = resp.getOutputStream();
        out.print( ja.toString() );
        log.debug("done with getEntitiesByVClassContinuation()");
    }



    /**
     *  Gets a list of entities that are members of the indicated vClass.
     *
     * If the list is large then we will pass some token indicating that there is more
     * to come.  The results are sent back in 250 entity blocks.  To get all of the
     * entities for a VClass just keep requesting lists until there are not more
     * continue tokens.
     *
     * If there are more entities the last item on the returned array will be an object
     * with no id property. It will look like this:
     *
     * {"resultGroup":0,
     *  "resultKey":"2WEK2306",
     *  "nextUrl":"http://caruso.mannlib.cornell.edu:8080/vitro/dataservice?getEntitiesByVClass=1&resultKey=2WEK2306&resultGroup=1&vclassId=null",
     *  "entsInVClass":1752,
     *  "nextResultGroup":1,
     *  "standardReplySize":256}
     *
     */
    private void getEntitiesByVClass(HttpServletRequest req, HttpServletResponse resp)    
    throws ServletException, IOException{
        log.debug("in getEntitiesByVClass()");
        VitroRequest vreq = new VitroRequest(req);
        String vclassURI = vreq.getParameter("vclassURI");
        WebappDaoFactory daos = (new VitroRequest(req)).getFullWebappDaoFactory();
        resp.setCharacterEncoding("UTF-8");                      
                        
        PrintWriter out = resp.getWriter();
        resp.getWriter();
        
        if( vclassURI == null ){
            log.debug("getEntitiesByVClass(): no value for 'vclassURI' found in the HTTP request");
            out.print( (new JSONArray()).toString() ); return; 
        }

        VClass vclass = daos.getVClassDao().getVClassByURI( vclassURI );                       
        if( vclass == null ){
            log.debug("getEntitiesByVClass(): could not find vclass for uri '"+  vclassURI + "'");
            out.print( (new JSONArray()).toString() ); return; 
        }

        List<Individual> entsInVClass = daos.getIndividualDao().getIndividualsByVClass( vclass );
        if( entsInVClass == null ){
            log.debug("getEntitiesByVClass(): null List<Individual> retruned by getIndividualsByVClass() for "+vclassURI);
            out.print( (new JSONArray().toString() )); return ;
        }
        int numberOfEntsInVClass = entsInVClass.size();

        List<Individual> entsToReturn = new ArrayList<Individual>( REPLY_SIZE );
        String requestHash = null;
        int count = 0;
        boolean more = false;
        /* we have a large number of items to send back so we need to stash the list in the session scope */
        if( entsInVClass.size() > REPLY_SIZE){
            more = true;
            HttpSession session = vreq.getSession(true);
            requestHash = Integer.toString((vclassURI + System.currentTimeMillis()).hashCode());
            session.setAttribute(requestHash, entsInVClass );

            ListIterator<Individual> entsFromVclass = entsInVClass.listIterator();
            while ( entsFromVclass.hasNext() && count < REPLY_SIZE ){
                entsToReturn.add( entsFromVclass.next());
                entsFromVclass.remove();
                count++;
            }
            if( log.isDebugEnabled() ){ log.debug("getEntitiesByVClass(): Creating reply with continue token, found " + numberOfEntsInVClass + " Individuals"); } 
        }else{
            if( log.isDebugEnabled() ) log.debug("getEntitiesByVClass(): sending " + numberOfEntsInVClass +" Individuals without continue token");
            entsToReturn = entsInVClass;
            count = entsToReturn.size();
        }

        
        //put all the entities on the JSON array
        JSONArray ja =  individualsToJson( entsToReturn );
        
        //put the responseGroup number on the end of the JSON array
        if( more ){
            try{
                JSONObject obj = new JSONObject();
                obj.put("resultGroup", "true");
                obj.put("size", count);
                obj.put("total", numberOfEntsInVClass);

                StringBuffer nextUrlStr = req.getRequestURL();
                nextUrlStr.append("?")
                        .append("getEntitiesByVClass").append( "=1&" )
                        .append("resultKey=").append( requestHash );
                obj.put("nextUrl", nextUrlStr.toString());

                ja.put(obj);
            }catch(JSONException je ){
                throw new ServletException("unable to create continuation as JSON: " + je.getMessage());
            }
        }
        
        resp.setContentType("application/json;charset=UTF-8");
        out.print( ja.toString() );
        
        log.debug("done with getEntitiesByVClass()");
        
    }
    
    /**
     * Gets data based on data getter for page uri and returns in the form of Json objects
     * @param req
     * @param resp
     */
   private void getDataForPage(HttpServletRequest req, HttpServletResponse resp) {
	   VitroRequest vreq = new VitroRequest(req);
       String errorMessage = null;
       JSONObject rObj = null;
	   String pageUri = vreq.getParameter("pageUri");
	   if(pageUri != null && !pageUri.isEmpty()) {
		   ServletContext context = getServletContext();
		   Map<String,Object> data = DataGetterUtils.getDataForPage(pageUri, vreq, context);
		   //Convert to json version based on type of page
		   if(data != null) {
			 //Convert to json version based on type of page
			   rObj = DataGetterUtils.covertDataToJSONForPage(pageUri, data, vreq, context);
	   		}
	   }

	   if( rObj == null )
           rObj = new JSONObject();
	 //Send object
       try{
           resp.setCharacterEncoding("UTF-8");
           resp.setContentType("application/json;charset=UTF-8");
           
           if( errorMessage != null ){
               rObj.put("errorMessage", errorMessage);
               resp.setStatus(500 /*HttpURLConnection.HTTP_SERVER_ERROR*/);
           }else{
               rObj.put("errorMessage", "");
           }            
           Writer writer = resp.getWriter();
           writer.write(rObj.toString());
       }catch(JSONException jse){
           log.error(jse,jse);
       } catch (IOException e) {
           log.error(e,e);
       }
	   
    }

    private JSONArray individualsToJson(List<Individual> individuals) throws ServletException {
        JSONArray ja = new JSONArray();
        Iterator<Individual> it = individuals.iterator();
        try{
            while(it.hasNext()){
                Individual ent = (Individual) it.next();
                JSONObject entJ = new JSONObject();
                entJ.put("name", ent.getName());
                entJ.put("URI", ent.getURI());
                ja.put( entJ );
            }
        }catch(JSONException ex){
            throw new ServletException("could not convert list of Individuals into JSON: " +  ex);
        }

        return ja;
    }

}
