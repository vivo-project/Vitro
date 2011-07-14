/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassGroupTemplateModel;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;

/**
 * This will pass these variables to the template:
 * classGroupUri: uri of the classgroup associated with this page.
 * vClassGroup: a data structure that is the classgroup associated with this page.     
 */
public class InternalClassesDataGetter extends IndividualsForClassesDataGetter{
    private static final Log log = LogFactory.getLog(InternalClassesDataGetter.class);
    
    @Override
    public Map<String,Object> getData(ServletContext context, VitroRequest vreq, String pageUri, Map<String, Object> page ){
        HashMap<String, Object> data = new HashMap<String,Object>();
        //This is the old technique of getting class intersections
        Map<String, Object> classIntersectionsMap = vreq.getWebappDaoFactory().getPageDao().getClassesAndCheckInternal(pageUri);
        
        
        //Use Individual List Controller to get all the individuals and related data                
        List<Individual> inds = new ArrayList<Individual>();
        try{
        	List<String> classes = (List<String>)classIntersectionsMap.get("classes");
        	List<String> restrictClasses = retrieveRestrictClasses(context, classIntersectionsMap);
        	processClassesAndRestrictions(vreq, context, data, classes, restrictClasses);
        	 //Also add data service url
            //Hardcoding for now, need a more dynamic way of doing this
            data.put("dataServiceUrlIndividualsByVClass", this.getDataServiceUrl());
        } catch(Exception ex) {
        	log.error("An error occurred retrieving Vclass Intersection individuals", ex);
        }
             
        return data;
    }

	private List<String> retrieveRestrictClasses(
			ServletContext context, Map<String, Object> classIntersectionsMap) {
		List<String> restrictClasses = new ArrayList<String>();
		String internalClass = (String) classIntersectionsMap.get("isInternal");
		//how should this be stored? boolean or otherwise?
		if(internalClass.equals("true")) {
			//Get internal class
			Model mainModel = ModelContext.getBaseOntModelSelector(context).getTBoxModel();;
			StmtIterator internalIt = mainModel.listStatements(null, ResourceFactory.createProperty(VitroVocabulary.IS_INTERNAL_CLASSANNOT), (RDFNode) null);
			//Checks for just one statement 
			if(internalIt.hasNext()){
				Statement s = internalIt.nextStatement();
				//The class IS an internal class so the subject is what we're looking for
				String internalClassUri = s.getSubject().getURI();
				log.debug("Found internal class uri " + internalClassUri);
				restrictClasses.add(internalClassUri);
			}
		}
			
		return restrictClasses;
	}        
	
	@Override
    public String getType(){
        return DisplayVocabulary.CLASSINDIVIDUALS_INTERNAL_TYPE;
    } 
    
}