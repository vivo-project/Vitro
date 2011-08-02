/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.JsonServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController.PageRecord;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;

/*
 * Handle processing of data retrieved from ClassGroupPage data getter to return to form template
 * and handle processing of form submission to create the appropriate individuals for classes data getter
 */
public class ProcessClassGroup implements ProcessDataGetter{
    private static final Log log = LogFactory.getLog(ProcessClassGroup.class);

  //template data represents what needs to be modified and returned to template
	//page data is data retrieved from data getter
    public void populateTemplate(ServletContext context, Map<String, Object> pageData, Map<String, Object> templateData) {
		//This is a class group page so 
		templateData.put("isClassGroupPage", true);
		templateData.put("includeAllClasses", true);
		
		//Get the class group from VClassGroup
		DataGetterUtils.getClassGroupForDataGetter(context, pageData, templateData);
	}
    
    
    //Process submission
    
    public  Model processSubmission(VitroRequest vreq, Resource dataGetterResource) {
    	ClassGroupPageData cpg = new ClassGroupPageData();
		Model dgModel = ModelFactory.createDefaultModel();
		String dataGetterTypeUri = cpg.getType();
		dgModel.add(dgModel.createStatement(dataGetterResource, 
				RDF.type, 
				ResourceFactory.createResource(dataGetterTypeUri)));
		return dgModel;
    }
    
}