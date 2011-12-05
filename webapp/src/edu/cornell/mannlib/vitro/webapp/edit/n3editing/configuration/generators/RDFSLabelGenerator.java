/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;

import com.hp.hpl.jena.vocabulary.XSD;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.ontology.OntModel;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.Field;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.AntiXssValidation;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditN3GeneratorVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.SelectListGeneratorVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
/**
 * Generates the edit configuration for RDFS Label form.  
 *
 */
public class RDFSLabelGenerator implements EditConfigurationGenerator {
	
	private Log log = LogFactory.getLog(DefaultObjectPropertyFormGenerator.class);
	
	private String subjectUri = null;
	private String predicateUri = null;	
	private Integer dataHash = null;

	private String literalName = "label";

	private String template = "rdfsLabelForm.ftl";

    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq, HttpSession session) {
    	//Check if an edit configuration exists and return that, otherwise create a new one
    	EditConfigurationVTwo editConfiguration = EditConfigurationVTwo.getConfigFromSession(session, vreq);
    	if(editConfiguration == null) {
    		log.debug("No editConfig in session. Making new editConfig.");
    		editConfiguration = setupEditConfiguration(vreq, session);
    	} else {
    		log.debug("Edit Configuration object already exists and will be returned");
    	}
    	return editConfiguration;
    }
    
     private EditConfigurationVTwo setupEditConfiguration(VitroRequest vreq, HttpSession session) {
    	EditConfigurationVTwo editConfiguration = new EditConfigurationVTwo();
    	
    	//process subject, predicate, object parameters
    	this.initProcessParameters(vreq, session, editConfiguration);
    	
      	//Assumes this is a simple case of subject predicate var
    	editConfiguration.setN3Required(this.generateN3Required(vreq));
    	    	
    	//n3 optional
    	editConfiguration.setN3Optional(this.generateN3Optional());
    	
    	//Todo: what do new resources depend on here?
    	//In original form, these variables start off empty
    	editConfiguration.setNewResources(new HashMap<String, String>());
    	//In scope
    	this.setUrisAndLiteralsInScope(editConfiguration);
    	
    	//on Form
    	this.setUrisAndLiteralsOnForm(editConfiguration, vreq);
    	
    	editConfiguration.setFilesOnForm(new ArrayList<String>());
    	
    	//Sparql queries
    	this.setSparqlQueries(editConfiguration);
    	
    	//set fields
    	setFields(editConfiguration, vreq, EditConfigurationUtils.getPredicateUri(vreq));
    	
    //	No need to put in session here b/c put in session within edit request dispatch controller instead
    	//placing in session depends on having edit key which is handled in edit request dispatch controller
    	prepareForUpdate(vreq, session, editConfiguration);

    	editConfiguration.addValidator(new AntiXssValidation());
    	
    	//Form title and submit label now moved to edit configuration template
    	setTemplate(editConfiguration, vreq);
    	//Set edit key
    	setEditKey(editConfiguration, vreq);
    	return editConfiguration;
    }
    private void setEditKey(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
    	String editKey = EditConfigurationUtils.getEditKey(vreq);	
    	editConfiguration.setEditKey(editKey);
    }
    
	private void setTemplate(EditConfigurationVTwo editConfiguration,
			VitroRequest vreq) {
    	editConfiguration.setTemplate(template);
	}

	//Initialize setup: process parameters
	//As this is a data property, don't require any additional processing for object properties
    private void initProcessParameters(VitroRequest vreq, HttpSession session, EditConfigurationVTwo editConfiguration) {
    	String formUrl = EditConfigurationUtils.getFormUrlWithoutContext(vreq);

    	subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
    	predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
    
    	editConfiguration.setFormUrl(formUrl);
        	
    	editConfiguration.setVarNameForSubject("subject");
    	editConfiguration.setSubjectUri(subjectUri);
    	editConfiguration.setEntityToReturnTo(subjectUri);
    	editConfiguration.setVarNameForPredicate("predicate");
    	editConfiguration.setPredicateUri(predicateUri);

    	this.initDataParameters(vreq, session);
    	this.processDataPropForm(vreq, editConfiguration);
    }
    
   
    
    private void initDataParameters(VitroRequest vreq, HttpSession session) {
    	dataHash = EditConfigurationUtils.getDataHash(vreq);	    	    
	}
    
    private void processDataPropForm(VitroRequest vreq, EditConfigurationVTwo editConfiguration) {
    	//set data prop value, data prop key str, 
    	editConfiguration.setDatapropKey( EditConfigurationUtils.getDataHash(vreq) );
    	editConfiguration.setVarNameForObject(literalName);    	    
    }
    
    //Get N3 required 
    //Handles both object and data property    
    private List<String> generateN3Required(VitroRequest vreq) {
    	List<String> n3ForEdit = new ArrayList<String>();
    	String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
    	String editString = "?subject <" + predicateUri + "> ?label .";
    	n3ForEdit.add(editString);
    	return n3ForEdit;
    }
    
    //in this case, no n3 optional
    private List<String> generateN3Optional() {
    	List<String> n3Inverse = new ArrayList<String>();
    	return n3Inverse;
    	
    }
    
  
    
    private void setUrisAndLiteralsInScope(EditConfigurationVTwo editConfiguration) {
    	HashMap<String, List<String>> urisInScope = new HashMap<String, List<String>>();
    	//note that at this point the subject, predicate, and object var parameters have already been processed
    	urisInScope.put(editConfiguration.getVarNameForSubject(), 
    			Arrays.asList(new String[]{editConfiguration.getSubjectUri()}));
    	urisInScope.put(editConfiguration.getVarNameForPredicate(), 
    			Arrays.asList(new String[]{editConfiguration.getPredicateUri()}));
    	editConfiguration.setUrisInScope(urisInScope);
    	//Uris in scope include subject, predicate
    	editConfiguration.setLiteralsInScope(new HashMap<String, List<Literal>>());
    }
    
    //n3 should look as follows
    //?subject ?predicate ?objectVar 
    
    private void setUrisAndLiteralsOnForm(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
    	List<String> urisOnForm = new ArrayList<String>();
    	List<String> literalsOnForm = new ArrayList<String>();
    	literalsOnForm.add(literalName);
    	editConfiguration.setUrisOnform(urisOnForm);
    	editConfiguration.setLiteralsOnForm(literalsOnForm);
    }
    
    //This is for various items
    private void setSparqlQueries(EditConfigurationVTwo editConfiguration) {
    	//Sparql queries defining retrieval of literals etc.
    	editConfiguration.setSparqlForAdditionalLiteralsInScope(new HashMap<String, String>());
    	editConfiguration.setSparqlForAdditionalUrisInScope(new HashMap<String, String>());
    	
    	editConfiguration.setSparqlForExistingLiterals(generateSparqlForExistingLiterals());
    	editConfiguration.setSparqlForExistingUris(generateSparqlForExistingUris());
    }
    
    
    //Get page uri for object
    private HashMap<String, String> generateSparqlForExistingUris() {
    	HashMap<String, String> map = new HashMap<String, String>();
    	return map;
    }
    
    private HashMap<String, String> generateSparqlForExistingLiterals() {
    	HashMap<String, String> map = new HashMap<String, String>();
    	return map;
    }

    
    private void setFields(EditConfigurationVTwo editConfiguration, VitroRequest vreq, String predicateUri) {
    	Map<String, FieldVTwo> fields = new HashMap<String, FieldVTwo>();
    	fields = getDataPropertyField(editConfiguration, vreq);
    	editConfiguration.setFields(fields);
    }
    
    private Map<String, FieldVTwo> getDataPropertyField(
			EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
		Map<String, FieldVTwo> fields = new HashMap<String, FieldVTwo>();
		FieldVTwo field = new FieldVTwo();
    	field.setName(literalName);
    	//queryForExisting is not being used anywhere in Field
    	String rangeDatatypeUri = getRangeDatatypeUri(editConfiguration, vreq);
    	String rangeLang = getRangeLang(editConfiguration, vreq);
    	
    	List<String> validators = getFieldValidators(editConfiguration, vreq);
    	
    	field.setValidators(validators);
    	
    	//subjectUri and subjectClassUri are not being used in Field
    	
    	field.setOptionsType("UNDEFINED");
    	//why isn't predicate uri set for data properties?
    	field.setPredicateUri(null);
    	field.setObjectClassUri(null);
    	field.setRangeDatatypeUri(rangeDatatypeUri);
    	//have either range datatype uri or range lang, otherwise results in error
    	if(rangeDatatypeUri == null) {
    		field.setRangeLang(rangeLang);
    	}
    	field.setLiteralOptions(getLiteralOptions(editConfiguration, vreq));
    	    	
    	fields.put(field.getName(), field);
    	return fields;
	}

	private List<List<String>> getLiteralOptions(
			EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
		   DataPropertyStatement dps =EditConfigurationUtils.getDataPropertyStatement(vreq, vreq.getSession(), dataHash, predicateUri);
		   List<List<String>> literalOptions = new ArrayList<List<String>>();
		return literalOptions;
	}

	private String getRangeLang(EditConfigurationVTwo editConfiguration,
			VitroRequest vreq) {
		String rangeLang = null;
		
	   DataPropertyStatement dps =EditConfigurationUtils.getDataPropertyStatement(vreq, vreq.getSession(), dataHash, predicateUri);
	   if (dps != null) { 
	        rangeLang = dps.getLanguage();
	        if( rangeLang == null ) {            
	            log.debug("no language attribute on rdfs:label statement for property " + predicateUri + "in RDFSLabelGenerator");
	            rangeLang = "";
	        } else {
	            log.debug("language attribute of ["+rangeLang+"] on rdfs:label statement for property " + predicateUri + "in RDFSLabelGenerator");
	        }
	        
	    } 
	   if( rangeLang != null && rangeLang.trim().length() == 0)
           rangeLang = null;
	    return rangeLang;
	}

	private String getRangeDatatypeUri(EditConfigurationVTwo editConfiguration,
			VitroRequest vreq) {
		Individual subject = EditConfigurationUtils.getSubjectIndividual(vreq);
		DataProperty prop = EditConfigurationUtils.getDataProperty(vreq);
		String rangeDatatypeUri = null;
		//rangeDefaultJson goes into literalk options
		//validations include dataype:rangedatatypeurijson
		//rangeDatatypeUri is rangeDAttypeUriJson
		//rangeLang = rangeLanJson
	   DataPropertyStatement dps =EditConfigurationUtils.getDataPropertyStatement(vreq, vreq.getSession(), dataHash, predicateUri);
	   if (dps != null) {
	        
	        rangeDatatypeUri = dps.getDatatypeURI();        
	        if (rangeDatatypeUri == null) {
	            log.debug("no range datatype uri set on rdfs:label statement for property " + predicateUri + "in RDFSLabelGenerator");
	        } else {
	            log.debug("range datatype uri of [" + rangeDatatypeUri + "] on rdfs:label statement for property " + predicateUri + "in RDFSLabelGenerator");
	        }        
	        
	    } else {
	        log.debug("No incoming rdfs:label statement for property "+predicateUri+"; adding a new statement");  
	        rangeDatatypeUri = XSD.xstring.getURI();
	    }
	   
	    if( rangeDatatypeUri != null && rangeDatatypeUri.trim().length() == 0)
            rangeDatatypeUri = null;
	    
	    return rangeDatatypeUri;
	}

	private List<String> getFieldValidators(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
		List<String> validatorList = new ArrayList<String>();
		String predicateUri =EditConfigurationUtils.getPredicateUri(vreq);
	    if (predicateUri.equals(VitroVocabulary.LABEL) || predicateUri.equals(VitroVocabulary.RDF_TYPE)) {
	        validatorList.add("nonempty");       
	    }
	    String rangeDatatypeUri = getRangeDatatypeUri(editConfiguration, vreq);
	    if (rangeDatatypeUri != null && !rangeDatatypeUri.isEmpty()) {
	        validatorList.add("datatype:" +  rangeDatatypeUri);
	    }
		return validatorList;    
	}

	private void prepareForUpdate(VitroRequest vreq, HttpSession session, EditConfigurationVTwo editConfiguration) {
    	//Here, retrieve model from 
    	Model model = (Model) session.getServletContext().getAttribute("jenaOntModel");
		if( editConfiguration.isDataPropertyUpdate() ){
    		editConfiguration.prepareForDataPropUpdate(model, vreq.getWebappDaoFactory().getDataPropertyDao());	
		}
    }
    
}
