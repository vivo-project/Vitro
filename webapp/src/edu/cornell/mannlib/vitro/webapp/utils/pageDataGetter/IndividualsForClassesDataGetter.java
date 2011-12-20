/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.PageDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassGroupTemplateModel;

/**
 * This will pass these variables to the template:
 * classGroupUri: uri of the classgroup associated with this page.
 * vClassGroup: a data structure that is the classgroup associated with this page.     
 */
public class IndividualsForClassesDataGetter implements PageDataGetter{
    private static final Log log = LogFactory.getLog(IndividualsForClassesDataGetter.class);
    protected static String restrictClassesTemplateName = null;
    public Map<String,Object> getData(ServletContext context, VitroRequest vreq, String pageUri, Map<String, Object> page ){
        this.setTemplateName();
    	HashMap<String, Object> data = new HashMap<String,Object>();
        //This is the old technique of getting class intersections
        PageDao pageDao = vreq.getWebappDaoFactory().getPageDao();
        Map<String, Object> classIntersectionsMap = getClassIntersectionsMap(pageDao, pageUri);
        
        try{
        	List<String> classes = retrieveClasses(context, classIntersectionsMap);
        	List<String> restrictClasses = retrieveRestrictClasses(context, classIntersectionsMap);
        	log.debug("Retrieving classes for " + classes.toString() + " and restricting by " + restrictClasses.toString());
        	processClassesAndRestrictions(vreq, context, data, classes, restrictClasses);
        	 //Also add data service url
            //Hardcoding for now, need a more dynamic way of doing this
            data.put("dataServiceUrlIndividualsByVClass", this.getDataServiceUrl());
            //this is the class group associated with the data getter utilized for display on menu editing, not the custom one created
            data.put("classGroupUri", pageDao.getClassGroupPage(pageUri));
        } catch(Exception ex) {
        	log.error("An error occurred retrieving Vclass Intersection individuals", ex);
        }
             
        return data;
    }        
   
    protected void setTemplateName() {
    	this.restrictClassesTemplateName = "restricted";
    }
    
    protected Map<String, Object> getClassIntersectionsMap(PageDao pageDao, 
			String pageUri) {
		// TODO Auto-generated method stub
    	return pageDao.getClassesAndRestrictionsForPage(pageUri);
	}
    
    protected List<String> retrieveClasses(
			ServletContext context, Map<String, Object> classIntersectionsMap) {
    	List<String> restrictClasses = (List<String>) classIntersectionsMap.get("classes");
		return restrictClasses;
	}    
    
    protected List<String> retrieveRestrictClasses(
			ServletContext context, Map<String, Object> classIntersectionsMap) {
    	List<String> restrictClasses = (List<String>) classIntersectionsMap.get("restrictClasses");
		return restrictClasses;
	}        
    
    protected void processClassesAndRestrictions(VitroRequest vreq, ServletContext context, 
    		HashMap<String, Object> data, List<String> classes, List<String> restrictClasses ) {
    	processClassesForDisplay(vreq, context, data, classes);
    	processRestrictionClasses(vreq, context, data, restrictClasses);
    	processIntersections(vreq, context, data);
    	
    }
    


	//At this point, data specifices whether or not intersections included
    private void processIntersections(VitroRequest vreq,
			ServletContext context, HashMap<String, Object> data) {
    	VClassGroup classesGroup = (VClassGroup) data.get("vClassGroup");
    	List<VClass> vclassList = classesGroup.getVitroClassList();
    	List<VClass> restrictClasses = (List<VClass>) data.get("restrictVClasses");
    	//if there are restrict classes, then update counts
    	if(restrictClasses.size() > 0) {
    		log.debug("Restriction classes exist");
    		List<VClass> newVClassList = new ArrayList<VClass>();
    		//Iterate through vclasses and get updated counts, iterated and saved in same order as initially included
    		for(VClass v: vclassList) {
    			int oldCount = v.getEntityCount();
    			//Making a copy so as to ensure we don't touch the values in the cache
    			VClass copyVClass = makeCopyVClass(v);
    			int count = retrieveCount(vreq, context, v, restrictClasses);
    			if(oldCount != count) {
    				log.debug("Old count was " + v.getEntityCount() + " and New count for " + v.getURI() + " is " + count);
    				copyVClass.setEntityCount(count);
    			} 
    			newVClassList.add(copyVClass);
    		}
    		classesGroup.setVitroClassList(newVClassList);
    		//TODO: Do we need to do this again or will this already be reset?
    		data.put("vClassGroup", classesGroup);
    	}
	}

    private VClass makeCopyVClass(VClass v) {
    	VClass copyVClass = new VClass(v.getURI());
		copyVClass.setLocalName(copyVClass.getLocalName());
		copyVClass.setDisplayRank(v.getDisplayRank());
		copyVClass.setName(v.getName());
		copyVClass.setNamespace(v.getNamespace());
		copyVClass.setEntityCount(v.getEntityCount());
		return copyVClass;
    }
    
    //update class count based on restrict classes
	private int retrieveCount(VitroRequest vreq, ServletContext context, VClass v, List<VClass> restrictClasses) {
		//Execute solr query that returns only count of individuals
		log.debug("Entity count is " + v.getEntityCount());
		List<String> classUris = new ArrayList<String>();
		classUris.add(v.getURI());
		for(VClass r: restrictClasses) {
			classUris.add(r.getURI());
		}
		long count =  DataGetterUtils.getIndividualCountForIntersection(vreq, context, classUris);
		return new Long(count).intValue();

	}

	private void processClassesForDisplay(VitroRequest vreq, ServletContext context, HashMap<String, Object> data, List<String> classes) {
    	VClassGroup classesGroup = new VClassGroup();
    	classesGroup.setURI("displayClasses");
    	log.debug("Processing classes that will be displayed");
    	List<VClass> vClasses = new ArrayList<VClass>();
  
    	VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(context);
    	for(String classUri: classes) {
    		//Retrieve vclass from cache to get the count
    		VClass vclass = vcgc.getCachedVClass(classUri);
    		//if not found in cache, possibly due to not being in any class group
			if(vclass == null) {
				vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(classUri);
			}
    		if(vclass != null) {
    			log.debug("VClass does exist for " + classUri + " and entity count is " + vclass.getEntityCount());
    			vClasses.add(vclass);
    		} else {
    			log.debug("Vclass " + classUri + " does not exist in the cache");
    			log.error("Error occurred, vclass does not exist for this uri " + classUri);
    			//Throw exception here
    		}
    	}
    	//Sort these classes
    	Collections.sort(vClasses);
    	log.debug("Sorting complete for V Classes");
    	classesGroup.setVitroClassList(vClasses);
    	log.debug("Returning vitro class list in data for template");
    	//Set vclass group
    	data.put("vClassGroup", classesGroup);
    }	
    
    private void processRestrictionClasses(VitroRequest vreq, ServletContext context, 
    		HashMap<String, Object> data, List<String> restrictClasses) {
    	try {
	    	VClassGroup restrictClassesGroup = new VClassGroup();
	    	restrictClassesGroup.setURI("restrictClasses");
	    	
	    	List<VClass> restrictVClasses = new ArrayList<VClass>();
	    	
	    	List<String> urlEncodedRestrictClasses = new ArrayList<String>();
	    	VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(context);

	    	if(restrictClasses.size() > 0) {
	    		//classes for restriction are not displayed so don't need to include their class individual counts
	    		for(String restrictClassUri: restrictClasses) {
	    			//Also uses cache to remain consistent with process classes and also allow
	    			//vclasses to be returned even if switched to display model, although
	    			//uris used within display model editing and not vclass objects
	    			VClass vclass = vcgc.getCachedVClass(restrictClassUri);
	    			//if not found in cache, possibly due to not being in any class group
	    			if(vclass == null) {
	    				vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(restrictClassUri);
	    			}
	        		if(vclass != null) {
	        			log.debug("Found restrict class and adding to list " + restrictClassUri);
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
	    	log.debug("Variable name for including restriction classes " +  getRestrictClassesTemplateName());
	    	data.put(getRestrictClassesTemplateName(), StringUtils.join(restrictClassesArray, ","));
	    	data.put("restrictVClasses", restrictVClasses);
	    	//not sure if this is useful
	    	data.put("restrictVClassGroup", restrictClassesGroup);
    	} catch(Exception ex) {
    		log.error("An error occurred in processing restriction classes ", ex);
    	}
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
        return DataGetterUtils.generateDataGetterTypeURI(IndividualsForClassesDataGetter.class.getName());
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
    
    protected static String getAlphaParameter(VitroRequest request){
        return request.getParameter("alpha");
    }
    
    protected static int getPageParameter(VitroRequest request) {
        String pageStr = request.getParameter("page");
        if( pageStr != null ){
            try{
                return Integer.parseInt(pageStr);                
            }catch(NumberFormatException nfe){
                log.debug("could not parse page parameter");
                return 1;
            }                
        }else{                   
            return 1;
        }
    }
    
    //Get template parameter
    private static String getRestrictClassesTemplateName() {
    	return restrictClassesTemplateName;

    }
}