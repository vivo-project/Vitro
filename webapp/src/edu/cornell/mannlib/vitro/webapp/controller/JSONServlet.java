/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
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
import edu.cornell.mannlib.vitro.webapp.controller.TabEntitiesController.PageRecord;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.SelectListGenerator;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.web.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.IndividualTemplateModel;

/**
 * This servlet is for servicing requests for JSON objects/data.
 * It could be generalized to get other types of data ex. XML, HTML etc
 * @author bdc34
 *
 */
public class JSONServlet extends VitroHttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
        VitroRequest vreq = new VitroRequest(req);

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
        }else if( vreq.getParameter("getLuceneIndividualsByVClass") != null ){
            getLuceneIndividualsByVClass(req,resp);
            return;
        }
        
    }

    private void getLuceneIndividualsByVClass( HttpServletRequest req, HttpServletResponse resp ){
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
            rObj = getLuceneIndividualsByVClass(vclass.getURI(),req,resp,getServletContext());
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
    
    protected static JSONObject getLuceneIndividualsByVClass(String vclassURI, HttpServletRequest req, HttpServletResponse resp, ServletContext context) throws Exception {
        
        VitroRequest vreq = new VitroRequest(req);        
        VClass vclass=null;
        JSONObject rObj = new JSONObject();
        
        DataProperty fNameDp = (new DataProperty());
        fNameDp.setURI("http://xmlns.com/foaf/0.1/firstName");
        DataProperty lNameDp = (new DataProperty());
        lNameDp.setURI("http://xmlns.com/foaf/0.1/lastName");
        DataProperty monikerDp = (new DataProperty());
        monikerDp.setURI( VitroVocabulary.MONIKER);
        //this property is vivo specific
        DataProperty perferredTitleDp = (new DataProperty());
        perferredTitleDp.setURI("http://vivoweb.org/ontology/core#preferredTitle");
        
        
        if( log.isDebugEnabled() ){
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
                
                                   
        String vitroClassIdStr = vreq.getParameter("vclassId");
        if ( vitroClassIdStr != null && !vitroClassIdStr.isEmpty()){                             
            vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(vitroClassIdStr);
            if (vclass == null) {
                log.debug("Couldn't retrieve vclass ");   
                throw new Exception ("Class " + vitroClassIdStr + " not found");
            }                           
        }else{
            log.debug("parameter vclassId URI parameter expected ");
            throw new Exception("parameter vclassId URI parameter expected ");
        }
        
        rObj.put("vclass", 
                new JSONObject().put("URI",vclass.getURI())
                                .put("name",vclass.getName()));
        
        if (vclass != null) {
            String alpha = EntityListController.getAlphaParamter(vreq);
            int page = EntityListController.getPageParameter(vreq);
            Map<String,Object> map = EntityListController.getResultsForVClass(
                    vclass.getURI(), 
                    page, 
                    alpha, 
                    vreq.getPortal(), 
                    vreq.getWebappDaoFactory().getPortalDao().isSinglePortal(), 
                    vreq.getWebappDaoFactory().getIndividualDao(), 
                    context);                                                
            
            rObj.put("totalCount", map.get("totalCount"));
            rObj.put("alpha", map.get("alpha"));
                            
            List<Individual> inds = (List<Individual>)map.get("entities");
            List<IndividualTemplateModel> indsTm = new ArrayList<IndividualTemplateModel>();
            JSONArray jInds = new JSONArray();
            for(Individual ind : inds ){
                JSONObject jo = new JSONObject();
                jo.put("URI", ind.getURI());
                jo.put("label",ind.getRdfsLabel());
                jo.put("name",ind.getName());
                jo.put("thumbUrl", ind.getThumbUrl());
                jo.put("imageUrl", ind.getImageUrl());
                jo.put("profileUrl", UrlBuilder.getIndividualProfileUrl(ind, vreq.getWebappDaoFactory()));
                
                String moniker = getDataPropertyValue(ind, monikerDp, fullWdf);
                jo.put("moniker", moniker);
                jo.put("vclassName", getVClassName(ind,moniker,fullWdf));
                                    
                jo.put("perferredTitle", getDataPropertyValue(ind, perferredTitleDp, fullWdf));
                jo.put("firstName", getDataPropertyValue(ind, fNameDp, fullWdf));                     
                jo.put("lastName", getDataPropertyValue(ind, lNameDp, fullWdf));
                
                jInds.put(jo);
            }
            rObj.put("individuals", jInds);
            
            JSONArray wpages = new JSONArray();
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
                                       
        return rObj;        
    }

    
    private static String getVClassName(Individual ind, String moniker,
            WebappDaoFactory fullWdf) {
        /* so the moniker frequently has a vclass name in it.  Try to return
         * the vclass name that is the same as the moniker so that the templates
         * can detect this. */
        if( (moniker == null || moniker.isEmpty()) ){
            if( ind.getVClass() != null && ind.getVClass().getName() != null )
                return ind.getVClass().getName();
            else
                return "";
        }
            
         List<VClass> vcList = ind.getVClasses();
         for( VClass vc : vcList){
             if( vc != null && moniker.equals( vc.getName() ))
                 return moniker;
         }
         
         // if we get here, then we didn't find a moniker that matched a vclass, 
         // so just return any vclass.name
         if( ind.getVClass() != null && ind.getVClass().getName() != null )
             return ind.getVClass().getName();
         else
             return "";        
    }

    static String getDataPropertyValue(Individual ind, DataProperty dp, WebappDaoFactory wdf){
        List<Literal> values = wdf.getDataPropertyStatementDao()
            .getDataPropertyValuesForIndividualByProperty(ind, dp);
        if( values == null || values.isEmpty() )
            return "";
        else{
            if( values.get(0) != null )
                return values.get(0).getLexicalForm();
            else
                return "";
        }
            
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
				DisplayVocabulary.PRIMARY_LUCENE_INDEX_URI, displayOntModel);
	     	editConfig.setProhibitedFromSearch(pfs);
    	}
	
        Map<String,String> options = SelectListGenerator.getOptions(editConfig, field, (new VitroRequest(req)).getFullWebappDaoFactory());
        resp.setContentType("application/json");
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
        List<Individual> entsInVClass = (List<Individual>) session.getAttribute(resKey);
        if( entsInVClass == null )
            throw new ServletException("Could not find List<Individual> for resultKey " + resKey);

        List<Individual> entsToReturn = new ArrayList<Individual>(REPLY_SIZE);
        boolean more = false;
        int count = 0;
        int size = REPLY_SIZE;
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
        resp.setContentType("application/json");
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
               
        // ServletOutputStream doesn't support UTF-8
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
        
        resp.setContentType("application/json");
        out.print( ja.toString() );
        
        log.debug("done with getEntitiesByVClass()");
        
    }

    private JSONArray individualsToJson(List<Individual> individuals) throws ServletException {
        JSONArray ja = new JSONArray();
        Iterator it = individuals.iterator();
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

    private static final int REPLY_SIZE = 256;

    private static final Log log = LogFactory.getLog(JSONServlet.class.getName());
}
