/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.DISPLAY_ONT_MODEL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.IndividualsViaObjectPropetyOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.AntiXssValidation;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;

/**
 * Generates the edit configuration for a default property form.
 *
 */
public class DefaultObjectPropertyFormGenerator implements EditConfigurationGenerator {

    //TODO: bdc34 why does the DefaultObjectPropertyForm have all this data property stuff?
	private Log log = LogFactory.getLog(DefaultObjectPropertyFormGenerator.class);
	private boolean isObjectPropForm = false;
	private String subjectUri = null;
	private String predicateUri = null;
	private String objectUri = null;
	private String datapropKeyStr= null;
	private int dataHash = 0;
	
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
            HttpSession session) throws Exception {
    	
    	//Check if create new and return specific edit configuration from that generator.
    	if(DefaultAddMissingIndividualFormGenerator.isCreateNewIndividual(vreq, session)) {
    		DefaultAddMissingIndividualFormGenerator generator = new DefaultAddMissingIndividualFormGenerator();
    		return generator.getEditConfiguration(vreq, session);
    	}
    	
    	//TODO: Add a generator for delete: based on command being delete - propDelete.jsp
        //Generate a edit configuration for the default object property form and return it.
    	//if(DefaultDeleteGenerator.isDelete( vreq,session)){
    	//  return (new DefaultDeleteGenerator()).getEditConfiguration(vreq,session);
    	
    	return getDefaultObjectEditConfiguration(vreq, session);
    }
    
    private EditConfigurationVTwo getDefaultObjectEditConfiguration(VitroRequest vreq, HttpSession session) throws Exception {
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
    //	editConfiguration.putConfigInSession(editConfiguration, session);

    	prepareForUpdate(vreq, session, editConfiguration);
    	
    	//After the main processing is done, check if select from existing process
    	processProhibitedFromSearch(vreq, session, editConfiguration);
    	
    	//Form title and submit label now moved to edit configuration template
    	//TODO: check if edit configuration template correct place to set those or whether
    	//additional methods here should be used and reference instead, e.g. edit configuration template could call
    	//default obj property form.populateTemplate or some such method
    	//Select from existing also set within template itself
    	setTemplate(editConfiguration, vreq);
    	
    	editConfiguration.addValidator(new AntiXssValidation());
    	
    	//Set edit key
    	setEditKey(editConfiguration, vreq);
    	//Adding additional data, specifically edit mode
        addFormSpecificData(editConfiguration, vreq);
    	return editConfiguration;
    }
    
    private void setEditKey(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
    	String editKey = EditConfigurationUtils.getEditKey(vreq);	
    	editConfiguration.setEditKey(editKey);
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
    	String formUrl = EditConfigurationUtils.getFormUrlWithoutContext(vreq);

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
    		log.debug("This is an object property: " + predicateUri);
    		//not concerned about remainder, can move into default obj prop form if required
    		this.isObjectPropForm = true;
    		this.initObjectParameters(vreq);
    		this.processObjectPropForm(vreq, editConfiguration);
    	} else {
    		log.debug("This is a data property: " + predicateUri);
    		this.isObjectPropForm = false;
    	   this.processDataPropForm(vreq, editConfiguration);
    	}
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
    }
    
    private void processDataPropForm(VitroRequest vreq, EditConfigurationVTwo editConfiguration) {
        //bdc34
        throw new Error("DefaultObjectPropertyForm should not be doing data property editing");    	
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
    	//Set only if object property form
    	if(this.isObjectPropForm) {
    		n3Inverse.add("?objectVar ?inverseProp ?subject .");
    	}
    	return n3Inverse;
    	
    }
    
    //Set queries
    private String retrieveQueryForInverse () {
    	String queryForInverse =  "PREFIX owl:  <http://www.w3.org/2002/07/owl#>"
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
    	List<String> literalsOnForm = new ArrayList<String>();
    	if(EditConfigurationUtils.isDataProperty(EditConfigurationUtils.getPredicateUri(vreq), vreq)) {
    		//if data property set to data literal
    		literalsOnForm.add(dataLiteral);
    	} else {
    		//uris on form should be empty if data property
    		urisOnForm.add("objectVar");
    	}
    	editConfiguration.setUrisOnform(urisOnForm);
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

    
    private void setFields(EditConfigurationVTwo editConfiguration, VitroRequest vreq, String predicateUri) throws Exception {
    	Map<String, FieldVTwo> fields = new HashMap<String, FieldVTwo>();
    	if(EditConfigurationUtils.isObjectProperty(EditConfigurationUtils.getPredicateUri(vreq), vreq)) {
    		fields = getObjectPropertyField(editConfiguration, vreq);
    	} else {
    	    throw new Exception("DefaultObjectPropertyFormGenerator does not handle data properties.");
    	}
    	
    	editConfiguration.setFields(fields);
    }       

	private Map<String, FieldVTwo> getObjectPropertyField(
			EditConfigurationVTwo editConfiguration, VitroRequest vreq) throws Exception {
		Map<String, FieldVTwo> fields = new HashMap<String, FieldVTwo>();
		FieldVTwo field = new FieldVTwo();
    	field.setName("objectVar");    	
    	//queryForExisting is not being used anywhere in Field
    	
    	List<String> validators = new ArrayList<String>();
    	validators.add("nonempty");
    	field.setValidators(validators);
    	    	
    	field.setOptions( new IndividualsViaObjectPropetyOptions(
    	        subjectUri, 
    	        predicateUri, 
    	        objectUri));    	    	
    	    	    
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
    	    throw new Error("DefaultObjectPropertyForm does not handle data properties.");
    	}
    }
      
    private boolean isSelectFromExisting(VitroRequest vreq) {
    	String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
    	if(EditConfigurationUtils.isDataProperty(predicateUri, vreq)) {
    		return false;
    	}
    	ObjectProperty objProp = EditConfigurationUtils.getObjectPropertyForPredicate(vreq, EditConfigurationUtils.getPredicateUri(vreq));
    	return objProp.getSelectFromExisting();
    }
    
    //Additional processing, eg. select from existing
    //This is really process prohibited from search
    private void processProhibitedFromSearch(VitroRequest vreq, HttpSession session, EditConfigurationVTwo editConfig) {
    	if(isSelectFromExisting(vreq)) {
    		// set ProhibitedFromSearch object so picklist doesn't show
            // individuals from classes that should be hidden from list views
    		//TODO: Check how model is retrieved
            OntModel displayOntModel = 
               (OntModel) session.getServletContext()
                    .getAttribute(DISPLAY_ONT_MODEL);
            if (displayOntModel != null) {
                ProhibitedFromSearch pfs = new ProhibitedFromSearch(
                    DisplayVocabulary.SEARCH_INDEX_URI, displayOntModel);
                if( editConfig != null )
                    editConfig.setProhibitedFromSearch(pfs);
            }
    	}
    }
    
  //Form specific data
	public void addFormSpecificData(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
		HashMap<String, Object> formSpecificData = new HashMap<String, Object>();
		//range options need to be stored for object property 
		//Store field names
		List<String> objectSelect = new ArrayList<String>();
		objectSelect.add(editConfiguration.getVarNameForObject());
		//TODO: Check if this is the proper way to do this?
		formSpecificData.put("objectSelect", objectSelect);
		editConfiguration.setFormSpecificData(formSpecificData);
	}
    
    

}
