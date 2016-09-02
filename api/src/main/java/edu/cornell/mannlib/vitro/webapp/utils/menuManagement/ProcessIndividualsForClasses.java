/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.menuManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetterUtils;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.IndividualsForClassesDataGetter;

/*
 * Handle processing of data retrieved from IndividualsForClasses data getter to return to form template
 * and handle processing of form submission to create the appropriate individuals for classes data getter
 */
public class ProcessIndividualsForClasses implements ProcessDataGetter {
    private static final Log log = LogFactory.getLog(ProcessIndividualsForClasses.class);

   /**Retrieve and populate**/
    
  //Based on institutional internal page and not general individualsForClasses
    @Override
	public void populateTemplate(HttpServletRequest req, Map<String, Object> pageData, Map<String, Object> templateData) {
		initTemplateData(templateData);
		populateIncludedClasses(pageData, templateData);
		populateRestrictedClasses(pageData, templateData);
		//Also save the class group for display
		DataGetterUtils.getClassGroupForDataGetter(req, pageData, templateData); 

	}
    
    protected void initTemplateData(Map<String,Object> templateData) {
    	templateData.put("isIndividualsForClassesPage", true);
		templateData.put("isClassGroupPage", false);
		templateData.put("includeAllClasses", false);
    }
    
    protected void populateIncludedClasses(Map<String, Object> pageData, Map<String, Object> templateData) {
		//what classes are to be included on page, note this should be a list of string uris
		VClassGroup includedClasses = (VClassGroup) pageData.get("vClassGroup");
		templateData.put("includeClasses", getClassUrisAsList(includedClasses));
    }
    
    protected void populateRestrictedClasses(Map<String, Object> pageData, Map<String, Object> templateData) {
		VClassGroup restrictedClasses = (VClassGroup) pageData.get("restrictVClassGroup");
		templateData.put("restricted", getClassUrisAsList(restrictedClasses));
    }
    
    protected List<String> getClassUrisAsList(VClassGroup includedClasses) {
		List<String> classUris = new ArrayList<String>();
		List<VClass> classList = includedClasses.getVitroClassList();
		for(VClass v:classList) {
			classUris.add(v.getURI());
		}
		return classUris;
	}
    
	/**Process submission**/
	//Check and see if we should use this process
	//Use this if either internal class is selected or all classes have been selected
    //No separate inputs for classes to restrict by yet so check if this includes a subset of classes
	public boolean useProcessor(VitroRequest vreq) {
		return(!allClassesSelected(vreq));
	}
	@Override
	public  Model processSubmission(VitroRequest vreq, Resource dataGetterResource) {
		String[] selectedClasses = vreq.getParameterValues("classInClassGroup");
		String dataGetterTypeUri = DataGetterUtils.generateDataGetterTypeURI(IndividualsForClassesDataGetter.class.getName());
		Model dgModel = ModelFactory.createDefaultModel();
		dgModel.add(dgModel.createStatement(dataGetterResource, 
				RDF.type, 
				ResourceFactory.createResource(dataGetterTypeUri)));
		for(String classUri: selectedClasses) {
			dgModel.add(dgModel.createStatement(
					dataGetterResource, 
					ResourceFactory.createProperty(DisplayVocabulary.GETINDIVIDUALS_FOR_CLASS),
					ResourceFactory.createResource(classUri)));
		}
		
		//This code can be uncommented when the form includes inputs for restricted class uris
		//At that time, use the input that returns the uris for restriction classes and replace below
		/*
		if(restrictionClassesSelected(vreq)) {
			String[] restrictedClasses = vreq.getParameterValues("restrictedClassUris");
			for(String restrictedClassUri: restrictedClasses) {
				dgModel.add(dgModel.createStatement(
						dataGetterResource, 
						ResourceFactory.createProperty(DisplayVocabulary.RESTRICT_RESULTS_BY),
						dgModel.createLiteral(restrictedClassUri)));
			}
		}*/
		return dgModel;
	}
	
	private  boolean allClassesSelected(VitroRequest vreq) {
			String allClasses = vreq.getParameter("allSelected");
			return (allClasses != null && !allClasses.isEmpty());
	}
		
	//This code can be uncommented when the form includes inputs for restricted class uris
	//At that time, use the input that returns the uris for restriction classes and replace below
	/*
	private  boolean restrictionClassesSelected(VitroRequest vreq) {
	   String restrictedClasses = vreq.getParameter("restrictedClassUri");
		return (restrictedClasses != null && !restrictedClasses.isEmpty());
	}
	*/		
		
}