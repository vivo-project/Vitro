/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.menuManagement;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.ClassGroupPageData;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetterUtils;

/*
 * Handle processing of data retrieved from ClassGroupPage data getter to return to form template
 * and handle processing of form submission to create the appropriate individuals for classes data getter
 */
public class ProcessClassGroup implements ProcessDataGetter{
    private static final Log log = LogFactory.getLog(ProcessClassGroup.class);

  //template data represents what needs to be modified and returned to template
	//page data is data retrieved from data getter
    @Override
	public void populateTemplate(HttpServletRequest req, Map<String, Object> pageData, Map<String, Object> templateData) {
		//This is a class group page so 
		templateData.put("isClassGroupPage", true);
		templateData.put("includeAllClasses", true);
		
		//Get the class group from VClassGroup
		DataGetterUtils.getClassGroupForDataGetter(req, pageData, templateData);
	}
    
    
    //Process submission
    
    @Override
	public  Model processSubmission(VitroRequest vreq, Resource dataGetterResource) {
		Model dgModel = ModelFactory.createDefaultModel();
		String dataGetterTypeUri = DataGetterUtils.generateDataGetterTypeURI(ClassGroupPageData.class.getName());
		dgModel.add(dgModel.createStatement(dataGetterResource, 
				RDF.type, 
				ResourceFactory.createResource(dataGetterTypeUri)));
		return dgModel;
    }
    
}