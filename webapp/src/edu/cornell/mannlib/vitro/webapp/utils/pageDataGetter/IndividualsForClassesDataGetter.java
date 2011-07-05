/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

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
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.SolrIndividualListController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController.PageRecord;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassGroupTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.BaseListedIndividual;
import edu.cornell.mannlib.vitro.webapp.controller.JSONServlet;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;

/**
 * This will pass these variables to the template:
 * classGroupUri: uri of the classgroup associated with this page.
 * vClassGroup: a data structure that is the classgroup associated with this page.     
 */
public class IndividualsForClassesDataGetter implements PageDataGetter{
    private static final Log log = LogFactory.getLog(IndividualsForClassesDataGetter.class);
    
    public Map<String,Object> getData(ServletContext context, VitroRequest vreq, String pageUri, Map<String, Object> page ){
        HashMap<String, Object> data = new HashMap<String,Object>();
        //This is the old technique of getting class intersections
        Map<String, List<String>> classIntersectionsMap = vreq.getWebappDaoFactory().getPageDao().getClassesAndRestrictionsForPage(pageUri);
        
        
        //Use Individual List Controller to get all the individuals and related data
        String alpha = IndividualListController.getAlphaParameter(vreq);
        int pageParam = IndividualListController.getPageParameter(vreq);
        List<Individual> inds = new ArrayList<Individual>();
        try{
        	List<String> classes = classIntersectionsMap.get("classes");
        	List<String> restrictClasses = classIntersectionsMap.get("restrictClasses");
        	//Get vclass group
        	//Anonymous vclass group
        	VClassGroup classesGroup = new VClassGroup();
        	classesGroup.setURI("displayClasses");
        	VClassGroup restrictClassesGroup = new VClassGroup();
        	restrictClassesGroup.setURI("restrictClasses");
        	List<VClass> vClasses = new ArrayList<VClass>();
        	List<VClass> restrictVClasses = new ArrayList<VClass>();
        	VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(context);
        	for(String classUri: classes) {
        		//VClass vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(classUri);
        		//Retrieve vclass from cache to get the count
        		VClass vclass = vcgc.getCachedVClass(classUri);
        		if(vclass != null) {
        			
        			log.debug("VClass does exist for " + classUri + " and entity count is " + vclass.getEntityCount());
        			vClasses.add(vclass);
        		} else {
        			log.debug("Vclass " + classUri + " does not exist in the cache");
        			log.error("Error occurred, vclass does not exist for this uri " + classUri);
        			//Throw exception here
        		}
        	}
        	classesGroup.setVitroClassList(vClasses);
        	//What is individual count? Total?
        	//classesGroup.setIndividualCount(vClasses.size());
        	data.put("vClassGroup", classesGroup);
        	List<String> urlEncodedRestrictClasses = new ArrayList<String>();
        	if(restrictClasses.size() > 0) {
        		//classes for restriction are not displayed so don't need to include their class individual counts
        		for(String restrictClassUri: restrictClasses) {
        			VClass vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(restrictClassUri);
            		if(vclass != null) {
            			restrictVClasses.add(vclass);
            		} else {
            			log.error("Error occurred, vclass does not exist for this uri " + restrictClassUri);
            		}
            		//Assuming utf-8?
            		urlEncodedRestrictClasses.add(URLEncoder.encode(restrictClassUri, "UTF-8"));
        		}
        	
        		restrictClassesGroup.setVitroClassList(restrictVClasses);
        		restrictClassesGroup.setIndividualCount(restrictVClasses.size());
        	} else {
        		
        	}
        	String[] restrictClassesArray = new String[urlEncodedRestrictClasses.size()];
        	restrictClassesArray = urlEncodedRestrictClasses.toArray(restrictClassesArray);
        	
        	//In case just want uris
        	data.put("restrictClasses", StringUtils.join(restrictClassesArray, ","));
        	data.put("restrictVClasses", restrictVClasses);
        	//not sure if this is useful
        	data.put("restrictVClassGroup", restrictClassesGroup);
        	 //Also add data service url
            //Hardcoding for now, need a more dynamic way of doing this
            data.put("dataServiceUrlIndividualsByVClass", this.getDataServiceUrl());
        } catch(Exception ex) {
        	log.error("An error occurred retrieving Vclass Intersection individuals", ex);
        }
             
        return data;
    }        
    
    public static VClassGroupTemplateModel getClassGroup(String classGroupUri, ServletContext context, VitroRequest vreq){
        
        VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(context);
        List<VClassGroup> vcgList = vcgc.getGroups();
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
                    log.error("Cannot get classgroup '" + classGroupUri + "'");
                    return null;
                }else{
                    setAllClassCountsToZero(group);
                }
            }else{
                log.error("classgroup " + classGroupUri + " does not exist in the system");
                return null;
            }            
        }
        
        return new VClassGroupTemplateModel(group);
    }
    
    public String getType(){
        return DisplayVocabulary.CLASSINDIVIDUALS_PAGE_TYPE;
    } 
    
    //Get data servuice
    public String getDataServiceUrl() {
    	return UrlBuilder.getUrl("/dataservice?getSolrIndividualsByVClasses=1&vclassId=");
    }
    /**
     * For processig of JSONObject
     */
    public JSONObject convertToJSON(Map<String, Object> map, VitroRequest vreq) {
    	JSONObject rObj = DataGetterUtils.processVclassResultsJSON(map, vreq, true);
    	return rObj;
    }
    
    protected static void setAllClassCountsToZero(VClassGroup vcg){
        for(VClass vc : vcg){
            vc.setEntityCount(0);
        }
    }
}