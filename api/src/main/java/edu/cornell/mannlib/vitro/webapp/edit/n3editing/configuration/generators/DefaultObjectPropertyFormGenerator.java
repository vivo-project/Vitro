/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.JspToGeneratorMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.vclassgroup.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.IndividualsViaObjectPropetyOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.AntiXssValidation;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils.EditMode;

/**
 * Generates the edit configuration for a default property form.
 * This handles the default object property auto complete.
 * 
 * If a default property form is request and the number of indivdiuals
 * found in the range is too large, the the auto complete setup and
 * template will be used instead.
 */
public class DefaultObjectPropertyFormGenerator implements EditConfigurationGenerator {

	private Log log = LogFactory.getLog(DefaultObjectPropertyFormGenerator.class);	
	private String subjectUri = null;
	private String predicateUri = null;
	private String objectUri = null;	
		
	private String objectPropertyTemplate = "defaultPropertyForm.ftl";
	private String acObjectPropertyTemplate = "autoCompleteObjectPropForm.ftl";		
	
	protected boolean doAutoComplete = false;
	protected boolean tooManyRangeIndividuals = false;
	
	protected long maxNonACRangeIndividualCount = 300;
	protected String customErrorMessages = null;
	
	private static HashMap<String,String> defaultsForXSDtypes ;
	  static {
		defaultsForXSDtypes = new HashMap<String,String>();
		//defaultsForXSDtypes.put("http://www.w3.org/2001/XMLSchema#dateTime","2001-01-01T12:00:00");
		defaultsForXSDtypes.put("http://www.w3.org/2001/XMLSchema#dateTime","#Unparseable datetime defaults to now");
	  }
	  
	  
    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
            HttpSession session) throws Exception {

    	if(!EditConfigurationUtils.isObjectProperty(EditConfigurationUtils.getPredicateUri(vreq), vreq)) {    	    	
    	    throw new Exception("DefaultObjectPropertyFormGenerator does not handle data properties.");
    	}
    	
    	//Custom error can also be represented as an exception above, but in this case
    	//we would like the page to enable the user to go back to the profile page
    	
    	customErrorMessages = getCustomErrorMessages(vreq);
    	if(customErrorMessages != null) {
    		return this.getCustomErrorEditConfiguration(vreq, session);
    	}
    	
     	if( tooManyRangeOptions( vreq, session ) ){
    		tooManyRangeIndividuals = true;
    		doAutoComplete = true;
    	}
     	
    	//Check if create new and return specific edit configuration from that generator.
    	if(DefaultAddMissingIndividualFormGenerator.isCreateNewIndividual(vreq, session)) {
			EditConfigurationGenerator generator = JspToGeneratorMapping.createFor("defaultAddMissingIndividualForm.jsp", DefaultAddMissingIndividualFormGenerator.class);
    		return generator.getEditConfiguration(vreq, session);
    	}
    	    
    	//TODO: Add a generator for delete: based on command being delete - propDelete.jsp
        //Generate a edit configuration for the default object property form and return it.
    	//if(DefaultDeleteGenerator.isDelete( vreq,session)){
    	//  return (new DefaultDeleteGenerator()).getEditConfiguration(vreq,session);
    	
    	return getDefaultObjectEditConfiguration(vreq, session);
    }
	
    private String getCustomErrorMessages(VitroRequest vreq) {
		String errorMessages = null;
    	String rangeUri = vreq.getParameter("rangeUri");
		VClass rangeVClass = null;
		if(rangeUri != null && !rangeUri.isEmpty()) {
	        WebappDaoFactory ctxDaoFact = vreq.getLanguageNeutralWebappDaoFactory();
   		    rangeVClass = ctxDaoFact.getVClassDao().getVClassByURI(rangeUri);
   		    if(rangeVClass == null) {
   		    	errorMessages = I18n.text(vreq,"the_range_class_does_not_exist");
   		    }
		}
		
		return errorMessages;
	}

	protected List<VClass> getRangeTypes(VitroRequest vreq) {
        // This first part needs a WebappDaoFactory with no filtering/RDFService
        // funny business because it needs to be able to retrieve anonymous union
        // classes by their "pseudo-bnode URIs".
        // Someday we'll need to figure out a different way of doing this.
        //WebappDaoFactory ctxDaoFact = ModelAccess.on(
        //        vreq.getSession().getServletContext()).getWebappDaoFactory();
        WebappDaoFactory ctxDaoFact = vreq.getLanguageNeutralWebappDaoFactory();

        List<VClass> types = new ArrayList<VClass>();
    	Individual subject = EditConfigurationUtils.getSubjectIndividual(vreq);
   		String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
   		String rangeUri = EditConfigurationUtils.getRangeUri(vreq);
   		if (rangeUri != null && !rangeUri.isEmpty()) {
   		    VClass rangeVClass = ctxDaoFact.getVClassDao().getVClassByURI(rangeUri);
   		    if(rangeVClass != null) {
	   		    if (!rangeVClass.isUnion()) {
	   		        types.add(rangeVClass);    
	   		    } else {
	   		        for (VClass unionComponent : rangeVClass.getUnionComponents()) {
	   		            types.add(unionComponent);
	   		        }
	   		    }
	   		    return types;
   		    } else {
   		    	log.error("Range VClass does not exist for " + rangeUri);
   		    }
   		}
   		WebappDaoFactory wDaoFact = vreq.getWebappDaoFactory();
		//Get all vclasses applicable to subject
   		if(subject != null) {
			List<VClass> vClasses = subject.getVClasses();
			HashMap<String, VClass> typesHash = new HashMap<String, VClass>();
			for(VClass vclass: vClasses) {
				 List<VClass> rangeVclasses = wDaoFact.getVClassDao().getVClassesForProperty(vclass.getURI(),predicateUri);
				 if(rangeVclasses !=  null) {
					 for(VClass range: rangeVclasses) {
						 //a hash will keep a unique list of types and so prevent duplicates
						 typesHash.put(range.getURI(), range);
					 }
				 }
			}
			types.addAll(typesHash.values());
   		} else {
   			log.error("Subject individual was null for");
   		}
        return types;
	}	
	
    private boolean tooManyRangeOptions(VitroRequest vreq, HttpSession session ) throws SearchEngineException {
    	List<VClass> rangeTypes = getRangeTypes(vreq);
		SearchEngine searchEngine = ApplicationUtils.instance().getSearchEngine();
    	
    	List<String> types = new ArrayList<String>();
    	for (VClass vclass : rangeTypes) {
    	    if (vclass.getURI() != null) {
    	        types.add(vclass.getURI());
    	    }
    	}
    	
    	//empty list means the range is not set to anything, force Thing
    	if(types.size() == 0 ){
    		types.add(VitroVocabulary.OWL_THING);
    	} 
    	
    	long count = 0;    		   
    	for( String type:types){
    		//search query for type count.    		
    		SearchQuery query = searchEngine.createQuery();
    		if( VitroVocabulary.OWL_THING.equals( type )){
    			query.setQuery( "*:*" );    			
    		}else{
    			query.setQuery( VitroSearchTermNames.RDFTYPE + ":" + type);
    		}
    		query.setRows(0);	
    		SearchResponse rsp = searchEngine.query(query);
    		SearchResultDocumentList docs = rsp.getResults();
    		long found = docs.getNumFound();
    		count = count + found;
    		if( count > maxNonACRangeIndividualCount )
    			break;
    	}
    	    	
    	return  count > maxNonACRangeIndividualCount ;    	    	   
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
    	setFields(editConfiguration, vreq, EditConfigurationUtils.getPredicateUri(vreq), getRangeTypes(vreq));
    	
    //	No need to put in session here b/c put in session within edit request dispatch controller instead
    	//placing in session depends on having edit key which is handled in edit request dispatch controller
    //	editConfiguration.putConfigInSession(editConfiguration, session);

    	prepareForUpdate(vreq, session, editConfiguration);
    	
    	//After the main processing is done, check if select from existing process
    	processProhibitedFromSearch(vreq, session, editConfiguration);
    	
    	//Form title and submit label moved to template
    	setTemplate(editConfiguration, vreq);
    	
    	editConfiguration.addValidator(new AntiXssValidation());
    	
    	//Set edit key
    	setEditKey(editConfiguration, vreq);
    	    	       	    	
    	//Adding additional data, specifically edit mode
    	if( doAutoComplete ){
    		addFormSpecificDataForAC(editConfiguration, vreq, session);
    	}else{	    
	        addFormSpecificData(editConfiguration, vreq);
    	}      
    	
    	return editConfiguration;
    }
	
	//We only need enough for the error message to show up
	private EditConfigurationVTwo getCustomErrorEditConfiguration(VitroRequest vreq, HttpSession session) {
		EditConfigurationVTwo editConfiguration = new EditConfigurationVTwo();    	
    	
    	//process subject, predicate, object parameters
    	this.initProcessParameters(vreq, session, editConfiguration);
    	
    	this.setUrisAndLiteralsInScope(editConfiguration);
    	
    	//Sparql queries
    	this.setSparqlQueries(editConfiguration);
    	
    
    	prepareForUpdate(vreq, session, editConfiguration);
    	
    	editConfiguration.setTemplate("customErrorMessages.ftl");
    	
    	//Set edit key
    	setEditKey(editConfiguration, vreq);
    	
    	//if custom error messages is not null, then add to form specific data
    	if(customErrorMessages != null) {
    		//at this point, it shouldn't be null
    		HashMap<String, Object> formSpecificData = new HashMap<String, Object>();
    		formSpecificData.put("customErrorMessages", customErrorMessages);
    		editConfiguration.setFormSpecificData(formSpecificData);
    	}
    	return editConfiguration;
	}
    
    private void setEditKey(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
    	String editKey = EditConfigurationUtils.getEditKey(vreq);	
    	editConfiguration.setEditKey(editKey);
    }
    
	private void setTemplate(EditConfigurationVTwo editConfiguration,
			VitroRequest vreq) {
		if( doAutoComplete )
			editConfiguration.setTemplate(acObjectPropertyTemplate);
		else
			editConfiguration.setTemplate(objectPropertyTemplate);
		
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
    		this.initObjectParameters(vreq);
    		this.processObjectPropForm(vreq, editConfiguration);
    	} else {
    		log.debug("This is a data property: " + predicateUri);
    		return;
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
       
    //Get N3 required 
    //Handles both object and data property    
    private List<String> generateN3Required(VitroRequest vreq) {
    	List<String> n3ForEdit = new ArrayList<String>();
    	String editString = "?subject ?predicate ";    	
    	editString += "?objectVar";    	
    	editString += " .";
    	n3ForEdit.add(editString);
    	return n3ForEdit;
    }
    
    private List<String> generateN3Optional() {
    	List<String> n3Inverse = new ArrayList<String>();    	
    	n3Inverse.add("?objectVar ?inverseProp ?subject .");    	
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
    	
    	//uris on form should be empty if data property
    	urisOnForm.add("objectVar");
    	
    	editConfiguration.setUrisOnform(urisOnForm);
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
    	return map;
    }
    
    private HashMap<String, String> generateSparqlForExistingLiterals() {
    	HashMap<String, String> map = new HashMap<String, String>();
    	return map;
    }
    
    protected void setFields(EditConfigurationVTwo editConfiguration, VitroRequest vreq, String predicateUri) throws Exception {
        setFields(editConfiguration, vreq, predicateUri, null);
    }
    
    protected void setFields(EditConfigurationVTwo editConfiguration, VitroRequest vreq, String predicateUri, List<VClass> rangeTypes) throws Exception {
		FieldVTwo field = new FieldVTwo();
    	field.setName("objectVar");    	
    	
    	List<String> validators = new ArrayList<String>();
    	validators.add("nonempty");
    	field.setValidators(validators);
    	    	
    	if( ! doAutoComplete ){
    		field.setOptions( new IndividualsViaObjectPropetyOptions(
    	        subjectUri, 
    	        predicateUri,
    	        rangeTypes,
    	        objectUri,
				vreq ));
    	}else{
    		field.setOptions(null);
    	}
    	
    	Map<String, FieldVTwo> fields = new HashMap<String, FieldVTwo>();
    	fields.put(field.getName(), field);    	
    	    	    	
    	editConfiguration.setFields(fields);
    }       

	private void prepareForUpdate(VitroRequest vreq, HttpSession session, EditConfigurationVTwo editConfiguration) {
    	//Here, retrieve model from 
		OntModel model = ModelAccess.on(session.getServletContext()).getOntModel();
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
            OntModel displayOntModel = ModelAccess.on(session.getServletContext()).getOntModel(DISPLAY);
            ProhibitedFromSearch pfs = new ProhibitedFromSearch(
                DisplayVocabulary.SEARCH_INDEX_URI, displayOntModel);
            if( editConfig != null )
                editConfig.setProhibitedFromSearch(pfs);
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
		if(customErrorMessages != null && !customErrorMessages.isEmpty()) {
			formSpecificData.put("customErrorMessages", customErrorMessages);
		}
		editConfiguration.setFormSpecificData(formSpecificData);
	}
        			
	public void addFormSpecificDataForAC(EditConfigurationVTwo editConfiguration, VitroRequest vreq, HttpSession session) throws SearchEngineException {
		HashMap<String, Object> formSpecificData = new HashMap<String, Object>();
		//Get the edit mode
		formSpecificData.put("editMode", getEditMode(vreq).toString().toLowerCase());
		
		//We also need the type of the object itself
		List<VClass> types = getRangeTypes(vreq);
        //if types array contains only owl:Thing, the search will not return any results
        //In this case, set an empty array
        if(types.size() == 1 && types.get(0).getURI().equals(VitroVocabulary.OWL_THING) ){
        	types = new ArrayList<VClass>();
        }
		
        StringBuffer typesBuff = new StringBuffer();
        for (VClass type : types) {
            if (type.getURI() != null) {
                typesBuff.append(type.getURI()).append(",");
            }
        }
        
		formSpecificData.put("objectTypes", typesBuff.toString());
		log.debug("autocomplete object types : "  + formSpecificData.get("objectTypes"));
		
		//Get label for individual if it exists
		if(EditConfigurationUtils.getObjectIndividual(vreq) != null) {
			String objectLabel = EditConfigurationUtils.getObjectIndividual(vreq).getName();
			formSpecificData.put("objectLabel", objectLabel);
		}
		
		//TODO: find out if there are any individuals in the classes of objectTypes
		formSpecificData.put("rangeIndividualsExist", rangeIndividualsExist(types) );
		
		formSpecificData.put("sparqlForAcFilter", getSparqlForAcFilter(vreq));
		if(customErrorMessages != null && !customErrorMessages.isEmpty()) {
			formSpecificData.put("customErrorMessages", customErrorMessages);
		}
		editConfiguration.setTemplate(acObjectPropertyTemplate);
		editConfiguration.setFormSpecificData(formSpecificData);
	}
	
	private Object rangeIndividualsExist(List<VClass> types) throws SearchEngineException {		
		SearchEngine searchEngine = ApplicationUtils.instance().getSearchEngine();
    	
    	boolean rangeIndividualsFound = false;
    	for( VClass type:types){
    		//search  for type count.
    		SearchQuery query = searchEngine.createQuery();   
    		query.setQuery( VitroSearchTermNames.RDFTYPE + ":" + type.getURI());
    		query.setRows(0);
    		
    		SearchResponse rsp = searchEngine.query(query);
    		SearchResultDocumentList docs = rsp.getResults();
    		if( docs.getNumFound() > 0 ){
    			rangeIndividualsFound = true;
    			break;
    		}    		
    	}
    	
    	return  rangeIndividualsFound;		
	}

	public String getSubjectUri() {
		return subjectUri;
	}
	
	public String getPredicateUri() {
		return predicateUri;
	}
	
	public String getObjectUri() {
		return objectUri;
	}
	

	/** get the auto complete edit mode */
	public EditMode getEditMode(VitroRequest vreq) {
		//In this case, the original jsp didn't rely on FrontEndEditingUtils
		//but instead relied on whether or not the object Uri existed
		String objectUri = EditConfigurationUtils.getObjectUri(vreq);
		EditMode editMode = FrontEndEditingUtils.EditMode.ADD;
		if(objectUri != null && !objectUri.isEmpty()) {
			editMode = FrontEndEditingUtils.EditMode.EDIT;
			
		}
		return editMode;
	}
    
	public String getSparqlForAcFilter(VitroRequest vreq) {
		String subject = EditConfigurationUtils.getSubjectUri(vreq);			
		String predicate = EditConfigurationUtils.getPredicateUri(vreq);
		//Get all objects for existing predicate, filters out results from addition and edit
		String query =  "SELECT ?objectVar WHERE { " + 
			"<" + subject + "> <" + predicate + "> ?objectVar .} ";
		return query;
	}
		

}
