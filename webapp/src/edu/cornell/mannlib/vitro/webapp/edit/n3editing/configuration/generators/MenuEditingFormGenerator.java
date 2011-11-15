/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.SelectListGeneratorVTwo;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils;

/**
 * Generates the edit configuration for a default property form.
 *
 */
public class MenuEditingFormGenerator implements EditConfigurationGenerator {
	
	private Log log = LogFactory.getLog(DefaultObjectPropertyFormGenerator.class);
	private String template = "testMenuEdit.ftl";
	//Set when processing
	//private String subjectUriJson = null;
	//private String predicateUriJson = null;
	//private String objectUriJson = null;
	
	//TODO: Check if above even needed or if we can process using regular uris
	private String subjectUri = null;
	private String predicateUri = null;
	private String objectUri = null;
	
	
	//whether or not this is an object or data prop
	private boolean isObjectPropForm = false;
	//from 'default data prop form'
	private static HashMap<String,String> defaultsForXSDtypes;

	static {
	defaultsForXSDtypes = new HashMap<String,String>();
	//defaultsForXSDtypes.put("http://www.w3.org/2001/XMLSchema#dateTime","2001-01-01T12:00:00");
	defaultsForXSDtypes.put("http://www.w3.org/2001/XMLSchema#dateTime","#Unparseable datetime defaults to now");
  }
	
	
	@Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
            HttpSession session) {
       
    	//The actual N3 created here needs to include multiple levels from hasElement all the way down to the
    	//actual pagej
    
    	EditConfigurationVTwo editConfiguration = new EditConfigurationVTwo();
    	//Setting a custom test template for now
    	//TODO: Where to get this custom template from?  Should it be a predicate in display model somewhere?
    	editConfiguration.setTemplate(this.template);
    	//process subject, predicate, object parameters
    	this.initProcessParameters(vreq, editConfiguration);
    	
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
    	this.setUrisAndLiteralsOnForm(editConfiguration);
    	
    	editConfiguration.setFilesOnForm(new ArrayList<String>());
    	
    	//Sparql queries
    	this.setSparqlQueries(editConfiguration);
    	
    	//Set up fields
    	this.setFields(editConfiguration, vreq);

    	//set submission url
    	editConfiguration.setSubmitToUrl("/edit/process");
    	editConfiguration.putConfigInSession(editConfiguration, session);

    	//Here, retrieve model from 
    	//Model model = (Model) session.getServletContext().getAttribute("jenaOntModel");
    	//Use special model instead
    	Individual subject = (Individual)vreq.getAttribute("subject");
        ObjectProperty prop = (ObjectProperty)vreq.getAttribute("predicate");

        WebappDaoFactory wdf = vreq.getWebappDaoFactory();  
    	Model model = (Model) vreq.getWriteModel();
    	
    	this.prepareForUpdate(vreq, editConfiguration, model, subject, prop, wdf);
    	this.associatePageData(vreq, editConfiguration, model);
    	//don't need this here exactly
    	//this.generateSelectForExisting(vreq, session, editConfiguration, subject, prop, wdf);
    	
    	return editConfiguration;
    }
    
    private void associatePageData(VitroRequest vreq,
			EditConfigurationVTwo editConfiguration, Model model) {
		vreq.setAttribute("formTitle", "Edit Menu Item");
		//
	}

	//Initialize setup: process parameters
    private void initProcessParameters(VitroRequest vreq, EditConfigurationVTwo editConfiguration) {
    	String formUrl = (String)vreq.getAttribute("formUrl");
    	String editKey = (String)vreq.getAttribute("editKey");
    	System.out.println("Form url is " + formUrl + " and editKey is " + editKey);
    	//this.subjectUriJson = (String)vreq.getAttribute("subjectUriJson");
    	//this.predicateUriJson = (String)vreq.getAttribute("predicateUriJson");
    	//this.objectUriJson = (String)vreq.getAttribute("objectUriJson");
    	//regular, non json version
    	this.subjectUri = (String)vreq.getAttribute("subjectUri");
    	this.predicateUri = (String)vreq.getAttribute("predicateUri");
    	this.objectUri = (String)vreq.getAttribute("objectUri");
    	//Get actual object uri as not concerned with json escaped version
    	//System.out.println("Object Uri is " + objectUri + " and json version is " + objectUriJson);
    	
    	//Set up EditConfigurationVTwo object
    	
    	editConfiguration.setFormUrl(formUrl);
    	editConfiguration.setEditKey(editKey);
    	editConfiguration.setUrlPatternToReturnTo("/individual");
    	
    	//subject, predicate, objectVar
    	editConfiguration.setVarNameForSubject("subject");
    	editConfiguration.setSubjectUri(subjectUri);

    	editConfiguration.setVarNameForPredicate("predicate");
    	editConfiguration.setPredicateUri(predicateUri);

    	
    	
    	//this needs to be set for the editing to be triggered properly, otherwise the 'prepare' method
    	//pretends this is a data property editing statement and throws an error
    	//"object"       : [ "objectVar" ,  "${objectUriJson}" , "URI"],
    	//if(objectUri != null) {
    		//not concerned about remainder, can move into default obj prop form if required
    		this.isObjectPropForm = true;
    		this.processObjectPropForm(vreq, editConfiguration);
    	//} else {
    	//	this.isObjectPropForm = false;
    	//   this.processDataPropForm(vreq, editConfiguration);
    	//}
    }
    
    private void processObjectPropForm(VitroRequest vreq, EditConfigurationVTwo editConfiguration) {
    	editConfiguration.setVarNameForObject("objectVar");    	
    	editConfiguration.setObject(objectUri);    	
    }
    
    private void processDataPropForm(VitroRequest vreq, EditConfigurationVTwo editConfiguration) {
        Integer dataHash = EditConfigurationUtils.getDataHash(vreq); 	    
    	DataPropertyStatement dps = (DataPropertyStatement)vreq.getAttribute("dataprop");

 		//ObjectUriJson is null, so should include data prop info here
 		//Use dataprop key info here instead
 		 DataProperty prop = (DataProperty)vreq.getAttribute("predicate");
 		 //if( prop == null ) return doHelp(vreq, "In DefaultDataPropertyFormGenerator, could not find predicate " + predicateUri);
 		 //Why are we setting attributes here again?
 		 vreq.setAttribute("propertyName",prop.getPublicName());
		    Individual subject = (Individual)vreq.getAttribute("subject");
		    //if( subject == null ) return doHelp(vreq,"In DefaultDataPropertyFormGenerator, could not find subject " + subjectUri);
		    vreq.setAttribute("subjectName",subject.getName());
		    
		    String rangeDatatypeUri = vreq.getWebappDaoFactory().getDataPropertyDao().getRequiredDatatypeURI(subject, prop);
		    //String rangeDatatypeUri = prop.getRangeDatatypeURI();
		    vreq.setAttribute("rangeDatatypeUriJson", MiscWebUtils.escape(rangeDatatypeUri));
		    
		    
		    if( dps != null ){		        		           
		        log.debug("dataHash is " + dataHash);            		        
		        
		        String rangeDatatype = dps.getDatatypeURI();
		        if( rangeDatatype == null ){
		            log.debug("no range datatype uri set on data property statement when property's range datatype is "+prop.getRangeDatatypeURI()+" in DefaultDataPropertyFormGenerator");
		            vreq.setAttribute("rangeDatatypeUriJson","");
		        } else {
		            log.debug("range datatype uri of ["+rangeDatatype+"] on data property statement in DefaultDataPropertyFormGenerator");
		            vreq.setAttribute("rangeDatatypeUriJson",rangeDatatype);
		        }
		        String rangeLang = dps.getLanguage();
		        if( rangeLang == null ) {
		            log.debug("no language attribute on data property statement in DefaultDataPropertyFormGenerator");
		            vreq.setAttribute("rangeLangJson","");
		        }else{
		            log.debug("language attribute of ["+rangeLang+"] on data property statement in DefaultDataPropertyFormGenerator");
		            vreq.setAttribute("rangeLangJson", rangeLang);
		        }
		    } else {
		        log.debug("No incoming dataproperty statement attribute for property "+prop.getPublicName()+"; adding a new statement");                
		        if(rangeDatatypeUri != null && rangeDatatypeUri.length() > 0) {                        
		            String defaultVal = defaultsForXSDtypes.get(rangeDatatypeUri);
		            if( defaultVal == null )            	
		            	vreq.setAttribute("rangeDefaultJson", "");
		            else
		            	vreq.setAttribute("rangeDefaultJson", '"' + MiscWebUtils.escape(defaultVal)  + '"' );
		        }
		    }   
	    	editConfiguration.setDatapropKey(dataHash);

    }
    
    
    //Get N3 required specifically for menu management
    //?Is it necessarily separate
    private List<String> generateN3Required(VitroRequest vreq) {
    	List<String> n3ForEdit = new ArrayList<String>();
    	String n3String = "?subject ?predicate ";
    	//n3ForEdit.add("?subject");
    	//n3ForEdit.add("?predicate");
    	
    	//leaving check out for now
    	//if(this.isObjectPropForm) {
    		n3String += "?objectVar .";
    		//n3ForEdit.add("?objectVar");
    		//In this case, defaultMenu hasElement ?objectVar
    		//Now add Strings
    		//?objectVar hasPage ?page
    		n3ForEdit.add(n3String);
    		n3ForEdit.add("\n ?objectVar <" + DisplayVocabulary.ITEM_TO_PAGE + "> ?page .");
    		n3ForEdit.add("\n ?page <" + DisplayVocabulary.DISPLAY_NS+ "title> ?title .");

    		
//    	} else { 
//    		 DataProperty prop = (DataProperty)vreq.getAttribute("predicate");
//    		 String localName = prop.getLocalName();
//    		 String dataLiteral = localName + "Edited";
//    		 n3String += "?"+dataLiteral;
//    		 n3ForEdit.add(n3String);
//    	}
    	return n3ForEdit;
    }
    //Below: use to replace default obj prop form
    //Handles both object and data property
    /*
    private List<String> generateN3Required(VitroRequest vreq) {
    	List<String> n3ForEdit = new ArrayList<String>();
    	n3ForEdit.add("?subject");
    	n3ForEdit.add("?predicate");
    	if(this.isObjectPropForm) {
    		n3ForEdit.add("?objectVar");
    	} else {
    		 DataProperty prop = (DataProperty)vreq.getAttribute("predicate");
    		 String localName = prop.getLocalName();
    		 String dataLiteral = localName + "Edited";
    		 n3ForEdit.add("?"+dataLiteral);
    	}
    	return n3ForEdit;
    }*/
    
    private List<String> generateN3Optional() {
    	List<String> n3Inverse = new ArrayList<String>();
    	n3Inverse.add("?objectVar ?inverseProp ?subject . \n");
   // 	n3Inverse.add("?page ?inversePage ?objectVar .");
    	//n3Inverse.add("?objectVar");
    	//n3Inverse.add("?inverseProp");
    	//n3Inverse.add("?subject"); 
    	return  n3Inverse;
    }
    
    //Set queries
    private String retrieveQueryForInverse () {
    	String queryForInverse = "PREFIX owl:  <http://www.w3.org/2002/07/owl#>"
			+ " SELECT ?inverse_property "
			+ "    WHERE { ?inverse_property owl:inverseOf ?predicate } ";
    	return queryForInverse;
    }
    
    private void setUrisAndLiteralsInScope(EditConfigurationVTwo editConfiguration) {
    	editConfiguration.setUrisInScope(new HashMap<String, List<String>>());
    	editConfiguration.setLiteralsInScope(new HashMap<String, List<Literal>>());
    }
    
    //n3 should look as follows
    //?subject ?predicate ?objectVar .
    //?objectVar display:linkText ?name .
    //?objectVar display:toPage ?page .
    //?page rdf:type ?type . //multiple types possible
    //?page display:title ?title .
    //?page display:urlMapping ?mapping .
    
    private void setUrisAndLiteralsOnForm(EditConfigurationVTwo editConfiguration) {
    	List<String> urisOnForm = new ArrayList<String>();
    	urisOnForm.add("objectVar");
    	//Also adding page
    	urisOnForm.add("page");
    	editConfiguration.setUrisOnform(urisOnForm);
    	//let's just get title
    	List<String> literalsOnForm = new ArrayList<String>();
    	literalsOnForm.add("title");
    	editConfiguration.setLiteralsOnForm(literalsOnForm);
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
    	map.put("page", "SELECT ?page where {?objectVar <" + DisplayVocabulary.TO_PAGE + "> ?page . } ");
    	return map;
    }
    
    private HashMap<String, String> generateSparqlForExistingLiterals() {
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("title", "SELECT ?title where {?page <" + DisplayVocabulary.DISPLAY_NS + "title> ?title . } ");
    	//If this works then add below
    	//Title, URL Mapping, type
    	//map.put("type", "SELECT ?type where {?page <" + RDF.type.getURI() + "> ?type . } ");
    	//And then being able to generate the Class groups required
    	//Could just pass that back in 
    	return map;
    }
    
    //Get all properties for a page?
    //Just get the properties?
    
    //Fields
    private void setFields(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
    	Map<String, FieldVTwo> fields = new HashMap<String, FieldVTwo>();
    	
    	
    	//Field should be for page title and other associations (assuming this is what actually goes on the form)
    	FieldVTwo field = new FieldVTwo();
    	field.setName("title");

    	List<String> validators = new ArrayList<String>();
    	validators.add("nonempty");
    	field.setValidators(validators);
    	field.setOptionsType("LITERALS");
    	field.setPredicateUri(DisplayVocabulary.DISPLAY_NS + "title");

    	fields.put("title", field);
    	//Object Var Field
    	//Won't need this in our case
    	/*
    	FieldVTwo field = new FieldVTwo();
    	field.setName("objectVar");
    	field.setNewResource(false);
    	//queryForExisting is not being used anywhere in Field
    	
    	//TODO: Check how validators will work
    	List<String> validators = new ArrayList<String>();
    	validators.add("nonempty");
    	field.setValidators(validators);
    	
    	//subjectUri and subjectClassUri are not being used in Field
    	field.setOptionsType("INDIVIDUALS_VIA_OBJECT_PROPERTY");
    	field.setPredicateUri(this.predicateUri);
    	
    	field.setObjectClassUri(null);
    	field.setRangeDatatypeUri(null);
    	
    	field.setRangeLang(null);
    	field.setLiteralOptions(new ArrayList<List<String>>());
    	
    	List<String> assertions = new ArrayList<String>();
    	assertions.add("?subject");
    	assertions.add("?predicate");
    	assertions.add("?objectVar");
    	assertions.add("?objectVar");
    	assertions.add("?inverseProp");
    	assertions.add("?subject");
    	field.setAssertions(assertions);
    	
    	fields.put("objectVar", field);
    	*/
    	
    	//Fields for us will actually be page specific
    	
    	//TODO: Check why/how this method signature has changed
    	editConfiguration.setFields(fields);
    }
    
    //Based on whether new or existing object prepare for update
    //TODO: Update for data property editing as well
    private void prepareForUpdate(VitroRequest vreq, EditConfigurationVTwo editConfiguration, Model model, Individual subject, ObjectProperty prop, WebappDaoFactory wdf) {
    	String formTitle = " ";
    	String submitLabel = " ";
    	//this block is for an edit of an existing object property statement
    	if(vreq.getAttribute("object") != null) {
    		editConfiguration.prepareForObjPropUpdate(model);
    		formTitle = "Change entry for: <em>" + prop.getDomainPublic() + " </em>";
    		submitLabel = "save change";
    	}  else {
            editConfiguration.prepareForNonUpdate( model );
            if ( prop.getOfferCreateNewOption() ) {
            	//Try to get the name of the class to select from
           	  	VClass classOfObjectFillers = null;
        
    		    if( prop.getRangeVClassURI() == null ) {    	
    		    	// If property has no explicit range, try to get classes 
    		    	List<VClass> classes = wdf.getVClassDao().getVClassesForProperty(subject.getVClassURI(), prop.getURI());
    		    	if( classes == null || classes.size() == 0 || classes.get(0) == null ){	    	
    			    	// If property has no explicit range, we will use e.g. owl:Thing.
    			    	// Typically an allValuesFrom restriction will come into play later.	    	
    			    	classOfObjectFillers = wdf.getVClassDao().getTopConcept();	    	
    		    	} else {
    		    		if( classes.size() > 1 )
    		    			log.debug("Found multiple classes when attempting to get range vclass.");
    		    		classOfObjectFillers = classes.get(0);
    		    	}
    		    }else{
    		    	classOfObjectFillers = wdf.getVClassDao().getVClassByURI(prop.getRangeVClassURI());
    		    	if( classOfObjectFillers == null )
    		    		classOfObjectFillers = wdf.getVClassDao().getTopConcept();
    		    }
            	
                log.debug("property set to offer \"create new\" option; custom form: ["+prop.getCustomEntryForm()+"]");
                formTitle   = "Select an existing "+classOfObjectFillers.getName()+" for "+subject.getName();
                submitLabel = "select existing";
            } else {
                formTitle   = "Add an entry to: <em>"+prop.getDomainPublic()+"</em>";
                submitLabel = "save entry";
            }
        }
    	vreq.setAttribute("formTitle", formTitle);
    	vreq.setAttribute("submitLabel", submitLabel);
    }
    
    //if existing object values, allow for selection from existing
    private void generateSelectForExisting(VitroRequest vreq, HttpSession session, EditConfigurationVTwo editConfiguration, Individual subject, ObjectProperty prop, WebappDaoFactory wdf) {
	      if( prop.getSelectFromExisting() ){
	    	// set ProhibitedFromSearch object so picklist doesn't show
	        // individuals from classes that should be hidden from list views
	        OntModel displayOntModel = 
	            (OntModel) session.getServletContext()
	                .getAttribute("displayOntModel");
	        if (displayOntModel != null) {
	            ProhibitedFromSearch pfs = new ProhibitedFromSearch(
	                DisplayVocabulary.SEARCH_INDEX_URI, displayOntModel);
	            if( editConfiguration != null )
	                editConfiguration.setProhibitedFromSearch(pfs);
	        }
	    	Map<String,String> rangeOptions = SelectListGeneratorVTwo.getOptions(editConfiguration, "objectVar" , wdf);    	
	    	if( rangeOptions != null && rangeOptions.size() > 0 ) {
	    		vreq.setAttribute("rangeOptionsExist", true);
	    	    vreq.setAttribute("rangeOptions.objectVar", rangeOptions);
	    	} else { 
	    		vreq.setAttribute("rangeOptionsExist",false);
	    	}
	    }
    }
    
    //Process additional data
    //In this case
    
}
