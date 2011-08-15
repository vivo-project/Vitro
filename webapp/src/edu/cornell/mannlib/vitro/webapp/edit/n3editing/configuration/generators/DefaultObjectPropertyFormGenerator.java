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


import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;

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
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditN3GeneratorVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.SelectListGeneratorVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils;

/**
 * Generates the edit configuration for a default property form.
 *
 */
public class DefaultObjectPropertyFormGenerator implements EditConfigurationGenerator {
	
	private Log log = LogFactory.getLog(DefaultObjectPropertyFormGenerator.class);
	private boolean isObjectPropForm = false;
	private String subjectUri = null;
	private String predicateUri = null;
	private String objectUri = null;
	private String datapropKeyStr= null;
	private int dataHash = 0;
	private DataPropertyStatement dps = null;
	private String dataLiteral = null;
	private String objectPropertyTemplate = "defaultPropertyForm.ftl";
	private String dataPropertyTemplate = "defaultDataPropertyForm.ftl";
	private static HashMap<String,String> defaultsForXSDtypes ;
	  static {
		defaultsForXSDtypes = new HashMap<String,String>();
		//defaultsForXSDtypes.put("http://www.w3.org/2001/XMLSchema#dateTime","2001-01-01T12:00:00");
		defaultsForXSDtypes.put("http://www.w3.org/2001/XMLSchema#dateTime","#Unparseable datetime defaults to now");
	  }
    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
            HttpSession session) {
        //Generate a edit conf for the default object property form and return it.	
    	EditConfigurationVTwo editConfiguration = new EditConfigurationVTwo();
    	//Set n3 generator
    	editConfiguration.setN3Generator(new EditN3GeneratorVTwo(editConfiguration));
    	
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
    //	editConfiguration.putConfigInSession(editConfiguration, session);

    	prepareForUpdate(vreq, session, editConfiguration);
    	
    	//Form title and submit label now moved to edit configuration template
    	//TODO: check if edit configuration template correct place to set those or whether
    	//additional methods here should be used and reference instead, e.g. edit configuration template could call
    	//default obj property form.populateTemplate or some such method
    	//Select from existing also set within template itself
    	setTemplate(editConfiguration, vreq);
    	return editConfiguration;
    }
    
	private void setTemplate(EditConfigurationVTwo editConfiguration,
			VitroRequest vreq) {
		String template = objectPropertyTemplate;
    	if(EditConfigurationUtils.isDataProperty(editConfiguration.getPredicateUri(), vreq)){
    		template = dataPropertyTemplate;
    	} 
    	editConfiguration.setTemplate(template);
		
	}

	//Initialize setup: process parameters
    private void initProcessParameters(VitroRequest vreq, HttpSession session, EditConfigurationVTwo editConfiguration) {
    	String formUrl = EditConfigurationUtils.getFormUrl(vreq);

    	subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
    	predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
    
    	editConfiguration.setFormUrl(formUrl);
    	
    	editConfiguration.setUrlPatternToReturnTo("/individual");
    	
    	editConfiguration.setVarNameForSubject("subject");
    	editConfiguration.setSubjectUri(subjectUri);
    	editConfiguration.setEntityToReturnTo(subjectUri);
    	editConfiguration.setVarNameForPredicate("predicate");
    	editConfiguration.setPredicateUri(predicateUri);
    	
    	
    	
    	//this needs to be set for the editing to be triggered properly, otherwise the 'prepare' method
    	//pretends this is a data property editing statement and throws an error
    	//"object"       : [ "objectVar" ,  "${objectUriJson}" , "URI"],
    	if(EditConfigurationUtils.isObjectProperty(predicateUri, vreq)) {
    		//not concerned about remainder, can move into default obj prop form if required
    		this.isObjectPropForm = true;
    		this.initObjectParameters(vreq);
    		this.processObjectPropForm(vreq, editConfiguration);
    	} else {
    		this.isObjectPropForm = false;
    		this.initDataParameters(vreq, session);
    	   this.processDataPropForm(vreq, editConfiguration);
    	}
    }
    
    private void initDataParameters(VitroRequest vreq, HttpSession session) {
    	dataLiteral = getDataLiteral(vreq);
    	datapropKeyStr = EditConfigurationUtils.getDataPropKey(vreq);
	    if( datapropKeyStr != null ){
	        try {
	            dataHash = Integer.parseInt(datapropKeyStr);
	            log.debug("Found a datapropKey in parameters and parsed it to int: " + dataHash);
	         } catch (NumberFormatException ex) {
	            //return doHelp(vreq, "Cannot decode incoming datapropKey value "+datapropKeyStr+" as an integer hash in EditDataPropStmtRequestDispatchController");
	        }
	    }
	    dps = EditConfigurationUtils.getDataPropertyStatement(vreq, session, dataHash, predicateUri);
	}


    
	private void initObjectParameters(VitroRequest vreq) {
		//in case of object property
    	objectUri = EditConfigurationUtils.getObjectUri(vreq);
	}

	private void processObjectPropForm(VitroRequest vreq, EditConfigurationVTwo editConfiguration) {
    	editConfiguration.setVarNameForObject("objectVar");    	
    	editConfiguration.setObject(objectUri);
    	//this needs to be set for the editing to be triggered properly, otherwise the 'prepare' method
    	//pretends this is a data property editing statement and throws an error
    	//TODO: Check if null in case no object uri exists but this is still an object property
    	if(objectUri != null) {
    		editConfiguration.setObjectResource(true);
    	}
    }
    
    private void processDataPropForm(VitroRequest vreq, EditConfigurationVTwo editConfiguration) {
    	editConfiguration.setObjectResource(false);
    	//set data prop value, data prop key str, 
    	editConfiguration.setDatapropKey((datapropKeyStr==null)?"":datapropKeyStr);
    	DataProperty prop = EditConfigurationUtils.getDataProperty(vreq);
    	editConfiguration.setVarNameForObject(dataLiteral);
    	//original set datapropValue, which in this case would be empty string but no way here
    	editConfiguration.setDatapropValue("");
    	editConfiguration.setUrlPatternToReturnTo("/entity");
    }
    
    //Get N3 required 
    //Handles both object and data property    
    private List<String> generateN3Required(VitroRequest vreq) {
    	List<String> n3ForEdit = new ArrayList<String>();
    	String editString = "?subject ?predicate ";
    	if(this.isObjectPropForm) {
    		editString += "?objectVar";
    	} else {
    		 DataProperty prop = EditConfigurationUtils.getDataProperty(vreq);
    		 String localName = prop.getLocalName();
    		 String dataLiteral = localName + "Edited";
    		 editString += "?"+dataLiteral;
    	}
    	editString += " .";
    	n3ForEdit.add(editString);
    	return n3ForEdit;
    }
    
    private List<String> generateN3Optional() {
    	List<String> n3Inverse = new ArrayList<String>();
    	//Note that for proper substitution, spaces expected between variables, i.e. string
    	//of n3 format
    	n3Inverse.add("?objectVar ?inverseProp ?subject .");
    	return n3Inverse;
    	
    }
    
    //Set queries
    private String retrieveQueryForInverse () {
    	String queryForInverse = "PREFIX owl:  <http://www.w3.org/2002/07/owl#>"
			+ " SELECT ?inverse_property "
			+ "    WHERE { ?inverse_property owl:inverseOf ?predicate } ";
    	return queryForInverse;
    }
    
    private void setUrisAndLiteralsInScope(EditConfigurationVTwo editConfiguration) {
    	HashMap<String, List<String>> urisInScope = new HashMap<String, List<String>>();
    	//note that at this point the subject, predicate, and object var parameters have already been processed
    	urisInScope.put(editConfiguration.getVarNameForSubject(), 
    			Arrays.asList(new String[]{editConfiguration.getSubjectUri()}));
    	urisInScope.put(editConfiguration.getVarNameForPredicate(), 
    			Arrays.asList(new String[]{editConfiguration.getPredicateUri()}));
    	//this shoudl happen in edit configuration prepare for object prop update
    	//urisInScope.put(editConfiguration.getVarNameForObject(), 
    	//		Arrays.asList(new String[]{editConfiguration.getObject()}));
    	//inverse property uris should be included in sparql for additional uris in edit configuration
    	editConfiguration.setUrisInScope(urisInScope);
    	//Uris in scope include subject, predicate, and object var
    	
    	editConfiguration.setLiteralsInScope(new HashMap<String, List<Literal>>());
    }
    
    //n3 should look as follows
    //?subject ?predicate ?objectVar 
    
    private void setUrisAndLiteralsOnForm(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
    	List<String> urisOnForm = new ArrayList<String>();
    	urisOnForm.add("objectVar");
    	editConfiguration.setUrisOnform(urisOnForm);
    	List<String> literalsOnForm = new ArrayList<String>();
    	if(EditConfigurationUtils.isDataProperty(EditConfigurationUtils.getPredicateUri(vreq), vreq)) {
    		//if data property set to data literal
    		literalsOnForm.add(dataLiteral);
    	}
    	editConfiguration.setLiteralsOnForm(literalsOnForm);
    }
    
    private String getDataLiteral(VitroRequest vreq) {
    	DataProperty prop = EditConfigurationUtils.getDataProperty(vreq);
    	return prop.getLocalName() + "Edited";
    }
    
    //This is for various items
    private void setSparqlQueries(EditConfigurationVTwo editConfiguration) {
    	//Sparql queries defining retrieval of literals etc.
    	editConfiguration.setSparqlForAdditionalLiteralsInScope(new HashMap<String, String>());
    	
    	Map<String, String> urisInScope = new HashMap<String, String>();
    	urisInScope.put("inverseProp", this.retrieveQueryForInverse());
    	editConfiguration.setSparqlForAdditionalUrisInScope(urisInScope);
    	
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
    	if(EditConfigurationUtils.isObjectProperty(EditConfigurationUtils.getPredicateUri(vreq), vreq)) {
    		fields = getObjectPropertyField(editConfiguration, vreq);
    	} else {
    		fields = getDataPropertyField(editConfiguration, vreq);
    	}
    	
    	editConfiguration.setFields(fields);
    }
    
    private Map<String, FieldVTwo> getDataPropertyField(
			EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
		Map<String, FieldVTwo> fields = new HashMap<String, FieldVTwo>();
		FieldVTwo field = new FieldVTwo();
    	field.setName(dataLiteral);
    	field.setNewResource(false);
    	//queryForExisting is not being used anywhere in Field
    	String rangeDatatypeUri = getRangeDatatypeUri(editConfiguration, vreq);
    	
    	List<String> validators = new ArrayList<String>();
    	validators.add("datatype:" + rangeDatatypeUri);
    	field.setValidators(validators);
    	
    	//subjectUri and subjectClassUri are not being used in Field
    	
    	field.setOptionsType("LITERALS");
    	//why isn't predicate uri set for data properties?
    	field.setPredicateUri(null);
    	field.setObjectClassUri(null);
    	field.setRangeDatatypeUri(rangeDatatypeUri);
    	
    	field.setRangeLang(getRangeLang(editConfiguration, vreq));
    	field.setLiteralOptions(getLiteralOptions(editConfiguration, vreq));
    	
    	//set assertions
    	List<String> assertions = new ArrayList<String>();
    	assertions.addAll(editConfiguration.getN3Required());
    	field.setAssertions(assertions);
    	fields.put(field.getName(), field);
    	
    	return fields;
	}

	private List<List<String>> getLiteralOptions(
			EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
		   DataPropertyStatement dps =EditConfigurationUtils.getDataPropertyStatement(vreq, vreq.getSession(), dataHash, predicateUri);
		   List<List<String>> literalOptions = new ArrayList<List<String>>();
		 if(dps == null) {
		        log.debug("No incoming dataproperty statement attribute for property ; adding a new statement");                
		        String rangeDatatypeUri = getRangeDatatypeUri(editConfiguration, vreq);
		        if(rangeDatatypeUri != null && rangeDatatypeUri.length() > 0) {                        
		            String defaultVal =  defaultsForXSDtypes.get(rangeDatatypeUri);
		            List<String> defaultArray = new ArrayList<String>();
		            defaultArray.add(defaultVal);
		            literalOptions.add(defaultArray);
		        }
		    }   
		return literalOptions;
	}

	private String getRangeLang(EditConfigurationVTwo editConfiguration,
			VitroRequest vreq) {
		String rangeLang = null;
		
	   DataPropertyStatement dps =EditConfigurationUtils.getDataPropertyStatement(vreq, vreq.getSession(), dataHash, predicateUri);
	   if(dps != null) {
		   rangeLang = dps.getLanguage();
		   if( rangeLang == null ) {
	           log.debug("no language attribute on data property statement in DefaultDataPropertyFormGenerator");
	       }else{
	           log.debug("language attribute of ["+rangeLang+"] on data property statement in DefaultDataPropertyFormGenerator");
	       }
	   }
	    return rangeLang;
	}

	private String getRangeDatatypeUri(EditConfigurationVTwo editConfiguration,
			VitroRequest vreq) {
		Individual subject = EditConfigurationUtils.getSubjectIndividual(vreq);
		DataProperty prop = EditConfigurationUtils.getDataProperty(vreq);
		
		//rangeDefaultJson goes into literalk options
		//validations include dataype:rangedatatypeurijson
		//rangeDatatypeUri is rangeDAttypeUriJson
		//rangeLang = rangeLanJson
	   DataPropertyStatement dps =EditConfigurationUtils.getDataPropertyStatement(vreq, vreq.getSession(), dataHash, predicateUri);
	   String rangeDatatypeUri = vreq.getWebappDaoFactory().getDataPropertyDao().getRequiredDatatypeURI(subject, prop);
  
	    if( dps != null ){
	      rangeDatatypeUri = dps.getDatatypeURI();
	        if( rangeDatatypeUri == null ){
	            log.debug("no range datatype uri set on data property statement when property's range datatype is "+prop.getRangeDatatypeURI()+" in DefaultDataPropertyFormGenerator");
	        } else {
	            log.debug("range datatype uri of ["+rangeDatatypeUri+"] on data property statement in DefaultDataPropertyFormGenerator");
	        }
	    } else {
	        log.debug("No incoming dataproperty statement attribute for property "+prop.getPublicName()+"; adding a new statement");                
	    }      

	    return rangeDatatypeUri;
	}

	private Map<String, FieldVTwo> getObjectPropertyField(
			EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
		Map<String, FieldVTwo> fields = new HashMap<String, FieldVTwo>();
		FieldVTwo field = new FieldVTwo();
    	field.setName("objectVar");
    	field.setNewResource(false);
    	//queryForExisting is not being used anywhere in Field
    	
    	List<String> validators = new ArrayList<String>();
    	validators.add("nonempty");
    	field.setValidators(validators);
    	
    	//subjectUri and subjectClassUri are not being used in Field
    	
    	field.setOptionsType("INDIVIDUALS_VIA_OBJECT_PROPERTY");
    	field.setPredicateUri(predicateUri);
    	
    	field.setObjectClassUri(null);
    	field.setRangeDatatypeUri(null);
    	
    	field.setRangeLang(null);
    	field.setLiteralOptions(new ArrayList<List<String>>());
    	
    	List<String> assertions = new ArrayList<String>();
    	assertions.addAll(editConfiguration.getN3Required());
    	assertions.addAll(editConfiguration.getN3Optional());
    	field.setAssertions(assertions);
    	fields.put(field.getName(), field);
    	
    	return fields;
    	
    	
	}

	private void prepareForUpdate(VitroRequest vreq, HttpSession session, EditConfigurationVTwo editConfiguration) {
    	//Here, retrieve model from 
    	Model model = (Model) session.getServletContext().getAttribute("jenaOntModel");
    	//if object property
    	if(EditConfigurationUtils.isObjectProperty(EditConfigurationUtils.getPredicateUri(vreq), vreq)){
	    	Individual objectIndividual = EditConfigurationUtils.getObjectIndividual(vreq);
	    	if(objectIndividual != null) {
	    		//update existing object
	    		editConfiguration.prepareForObjPropUpdate(model);
	    	}  else {
	    		//new object to be created
	            editConfiguration.prepareForNonUpdate( model );
	        }
    	} else {
    		if(datapropKeyStr != null && datapropKeyStr.trim().length() > 0 ) {
	    		DataPropertyStatement dps = EditConfigurationUtils.getDataPropertyStatement(vreq, 
	    				session, 
	    				dataHash, 
	    				EditConfigurationUtils.getPredicateUri(vreq));
	    		
	    		editConfiguration.prepareForDataPropUpdate(model, dps);
    		}
    	}
    }
    
    //Command processing
    private boolean isTypeOfNew(VitroRequest vreq) {
    	String typeOfNew = vreq.getParameter("typeOfNew");
    	return (typeOfNew != null && !typeOfNew.isEmpty());
    }
    //orignal code for process to forward new is below
    /*
	private ResponseValues processForwardToCreateNew(VitroRequest vreq) {
		String command = vreq.getParameter("cmd");
		ObjectProperty objectProp = (ObjectProperty) vreq.getAttribute("predicate");
	
		// TODO Auto-generated method stub
    	 // The default object proepty form offers the option to create a new item
        // instead of selecting from existing individuals in the system.
        // This is where the request to create a new indivdiual is handled.
        //
        // Forward to create new is part of the default object property form
        // it should be handled in that form's EditConfigurationVTwo, not here.
        // The code that sets up the EditConfigurationVTwo should decide on 
        // different configurations and templates to use based on isForwardToCreateNew. 
        //TODO: make sure that forward to create new works on the default object property form
    	
    	
        if( isFowardToCreateNew(vreq, objectProp, command)){
            return handleForwardToCreateNew(vreq, command, objectProp, isEditOfExistingStmt(vreq));
        }
        //what should this return otherwise and should this in fact redirect
        return null;
		
	}*/
    
    /*
    Forward to create new is part of the default object property form
    it should be handled in that form's EditConfigurationVTwo, not here.
    The code that sets up the EditConfigurationVTwo should decide on 
    different configurations and templates to use based on isForwardToCreateNew.
*//*
boolean isFowardToCreateNew(VitroRequest vreq, ObjectProperty objectProp, String command){       
   //Offer create new and select from existing are ignored if there is a custom form
   if( objectProp != null && objectProp.getCustomEntryForm() != null && !objectProp.getCustomEntryForm().isEmpty()){        
       return false;
   } else {

       boolean isForwardToCreateNew = 
           ( objectProp != null && objectProp.getOfferCreateNewOption() && objectProp.getSelectFromExisting() == false)
           || ( objectProp != null && objectProp.getOfferCreateNewOption() && "create".equals(command));

       return isForwardToCreateNew;
   }
}

ResponseValues handleForwardToCreateNew(VitroRequest vreq, String command, ObjectProperty objectProp, boolean isEditOfExistingStmt){                          
   vreq.setAttribute("isForwardToCreateNew", new Boolean(true));
   
   //If a objectProperty is both provideSelect and offerCreateNewOption
   // and a user goes to a defaultObjectProperty.jsp form then the user is
   // offered the option to create a new Individual and replace the 
   // object in the existing objectPropertyStatement with this new individual. 
   boolean isReplaceWithNew =
       isEditOfExistingStmt && "create".equals(command) 
       && objectProp != null && objectProp.getOfferCreateNewOption() == true;                

   // If an objectProperty is selectFromExisitng==false and offerCreateNewOption == true
   // the we want to forward to the create new form but edit the existing object
   // of the objPropStmt.
   boolean isForwardToCreateButEdit = 
       isEditOfExistingStmt && objectProp != null 
       && objectProp.getOfferCreateNewOption() == true 
       && objectProp.getSelectFromExisting() == false
       && ! "create".equals(command);

   //bdc34: maybe when doing a create new, the custom form should be on the class, not the property?
   String form;
   if( isReplaceWithNew ){
       vreq.setAttribute("isReplaceWithNew", new Boolean(true));
       form = DEFAULT_ADD_INDIVIDUAL;
   }else  if( isForwardToCreateButEdit ){
       vreq.setAttribute("isForwardToCreateButEdit", new Boolean(true));
       form = DEFAULT_ADD_INDIVIDUAL;
   }else {
       form = DEFAULT_ADD_INDIVIDUAL;
   }
   
   //forward to form?
   //There should be no error message here
   //TODO: Return redirect response values or find out to process this here
   HashMap<String,Object> map = new HashMap<String,Object>();
	 map.put("errorMessage", "forweard to create new is not yet implemented");
	 return new TemplateResponseValues("error-message.ftl", map);
}        
*/

}
