/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController.PageRecord;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassGroupTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.ListedIndividualTemplateModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;

/**
 * This will pass these variables to the template:
 * classGroupUri: uri of the classgroup associated with this page.
 * vClassGroup: a data structure that is the classgroup associated with this page.     
 */
public class ClassIntersectionDataGetter implements PageDataGetter{
    private static final Log log = LogFactory.getLog(ClassIntersectionDataGetter.class);
    
    public Map<String,Object> getData(ServletContext context, VitroRequest vreq, String pageUri, Map<String, Object> page, String type ){
        HashMap<String, Object> data = new HashMap<String,Object>();
        List<String> classIntersections = vreq.getWebappDaoFactory().getPageDao().getClassIntersections(pageUri);
        data.put("classIntersections", classIntersections);
        //Use Individual List Controller to get all the individuals and related data
        String alpha = IndividualListController.getAlphaParameter(vreq);
        int pageParam = IndividualListController.getPageParameter(vreq);
        try{
        	Map<String, Object> results = IndividualListController.getResultsForVClassIntersections(classIntersections, pageParam, alpha, vreq.getWebappDaoFactory().getIndividualDao(), context);
        	data.putAll(results);
        	//NOTE: Below is copied from Individual List Controller's processing as some of these are used in the template
        	//below may not be necessary if using a different template
            List<Individual> inds = (List<Individual>)data.get("entities");
            List<ListedIndividualTemplateModel> indsTm = new ArrayList<ListedIndividualTemplateModel>();
            for(Individual ind : inds ){
                indsTm.add(new ListedIndividualTemplateModel(ind,vreq));
            }
            data.put("individuals", indsTm);
            
            List<TemplateModel> wpages = new ArrayList<TemplateModel>();
            List<PageRecord> pages = (List<PageRecord>)data.get("pages");
            BeansWrapper wrapper = new BeansWrapper();
            for( PageRecord pr: pages ){
                wpages.add( wrapper.wrap(pr) );
            }

           
            data.put("rdfUrl", vreq.getContextPath()+"/listrdf/");
        	
        } catch(Exception ex) {
        	log.error("An error occurred retrieving Vclass Intersection individuals", ex);
        }
        

        
        
        //TODO: Check if need class group at all here?
        //data.put("vClassGroup", group);  //may put null            
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
        return DisplayVocabulary.CLASSINTERSECTION_PAGE_TYPE;
    } 
    
    protected static void setAllClassCountsToZero(VClassGroup vcg){
        for(VClass vc : vcg){
            vc.setEntityCount(0);
        }
    }
}