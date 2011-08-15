/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.ModelSelector;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.StandardModelSelector;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.StandardWDFSelector;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.WDFSelector;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditSubmissionVTwoPreprocessor;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.edit.EditLiteral;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.ModelChangePreprocessor;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.N3Validator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditN3Utils;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;


/**
 * Represents a set of fields on a form and how parameters from a from
 * submission should be manipulated to create N3.
 *
 * Uris in urisInScope and urisOnForm should not have any quoting or escaping.
 *
 * Literals in literalsOnForm and literalsInScope should be escaped and quoted
 * in preparation for N3. They may also be appended with a datatype or lang.
 */
public class EditConfigurationVTwo {
    
	//Strings representing required n3 for RDF
    List<String> n3Required = Collections.emptyList();
    //String representing optional N3 for RDF
    List<String> n3Optional = Collections.emptyList();
    //Names of variables of 'objects' i.e. URIs on form
    List<String> urisOnform = Collections.emptyList();
    //Names of variables corresponding to data values i.e. literals on form
    List<String> literalsOnForm = Collections.emptyList();
    //Names of variables corresponding to Files on form
    List<String> filesOnForm = Collections.emptyList();
    
    //Multi values now supported for uris and literals, so second parameter needs to be List<String>
    //Mapping of variable name for object to values for object, i.e. URIs, e.g. "hasElement" = "<a.com>, <b.com>"
    Map<String,List<String>> urisInScope = Collections.emptyMap();
    //Mapping from variable name to values for literals
    Map<String, List<Literal>> literalsInScope = Collections.emptyMap();
    
    //Map name of variable to sparql query which should return a one-column result set of URIs corresponding to variable
    //E.g. sparql for inverse of object property
    Map<String,String> sparqlForAdditionalUrisInScope = Collections.emptyMap();
    //Mapping variable to sparql query returning literals 
    Map<String,String> sparqlForAdditionalLiteralsInScope = Collections.emptyMap();
    
    //Variable names to URI prefixes for variables that are allowed to have new instances created
    Map<String,String> newResources = Collections.emptyMap();
    
    //Variable names to fields, Field = additional configuration for variable
    Map<String,FieldVTwo> fields = Collections.emptyMap();
    
    //Mapping variable name to Sparql query to find existing literals corresponding to variable, result set should be one-column multi-row of literals
    Map<String,String>sparqlForExistingLiterals = Collections.emptyMap();
    //Mapping variable name to Sparql query to find existing URIs corresponding to variable, result set should be one-column multi-row of URIs/URI resources
    Map<String,String>sparqlForExistingUris = Collections.emptyMap();

    String subjectUri;
    String varNameForSubject;

    String predicateUri;
    String varNameForPredicate;

    /** When this is a DataPropertyStmt edit, the object is not used, the
     * DataPropertyStatement is retrieved using the subject, predicate and the
     * datapropKey.  When this edit is for a ObjectPropertyStmt,
     * object is the uri without the quoting &lt; or &gt;.
     */
    String object;
    String varNameForObject;
    boolean isObjectResource;

    String datapropKey;
    String datapropValue;

    String urlPatternToReturnTo;
    String entityToReturnTo;
    String formUrl;
    String editKey;

    List<N3Validator> validators = Collections.emptyList();

	EditN3GeneratorVTwo n3generator;   

    private List<ModelChangePreprocessor> modelChangePreprocessors = Collections.emptyList();
    
    private List<EditSubmissionVTwoPreprocessor> editSubmissionPreprocessors  = Collections.emptyList();
    
    private ProhibitedFromSearch prohibitedFromSearch;

    /** Name of freemarker template to generate form. */
    String template;
    
    /** URL to submit form to. */
    String submitToUrl;
    
    /** 
     * If true, then any dependent resources that are unlinked should be
     * removed using DependentResourceDelete. 
     */
    private boolean useDependentResourceDelete = true;   

	/** Model to write changes of a completed edit to. Usually this is null
     * and the edit will be written to the main graph of the system.     */
    private ModelSelector writeModelSelector;

    /** Model to query for existing and things like that. Usually this is null
     * and the main Model of the system will be used as the default.     */
    private ModelSelector queryModelSelector;
    
    /** Model to check when making new URIs to check that there is not already
     * a resource with a given URI. */
    private ModelSelector resourceModelSelector;
    
    /** WebappDaoFactory to build option, check box, and radio button lists. 
     * Usually this is set to null and the main model will be used. */
    private WDFSelector wdfSelectorForOptons;
    
    public EditConfigurationVTwo(){ 
        writeModelSelector = StandardModelSelector.selector;
        queryModelSelector = StandardModelSelector.selector;   
        resourceModelSelector = StandardModelSelector.selector;
        wdfSelectorForOptons = StandardWDFSelector.selector;
    }


    /**
     * Add symbols for things like currentTime and editingUser to 
     * editConfig.urisInScope and editConfig.literalsInScope.
     */
    public void addSystemValues( Model model, HttpServletRequest request, ServletContext context){
        if( model == null ) throw new Error("EditConfiguration.addSystemValues() needs a Model");
        if( request == null ) throw new Error("EditConfiguration.addSystemValues() needs a request");        
     
        /* current time */
        if( getSparqlForAdditionalLiteralsInScope() != null && 
            getSparqlForAdditionalLiteralsInScope().containsKey("currentTime") &&
            USE_SYSTEM_VALUE.equals(getSparqlForAdditionalLiteralsInScope().get("currentTime"))){
        	//Updating so that this is represented as an XSD Date Time literal - to allow for comparison later
        	//Currently it appears that this is only used for file upload 
            //getLiteralsInScope().put("currentTime", ResourceFactory.createTypedLiteral(new Date()));
        	SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    		String formattedDate = dateTime.format(Calendar.getInstance().getTime());
    		List<Literal> dateLiterals = new ArrayList<Literal>();
    		dateLiterals.add(ResourceFactory.createTypedLiteral(formattedDate, XSDDatatype.XSDdateTime));
    		getLiteralsInScope().put("currentTime", dateLiterals);
        }            
        
        /* editing user */
        if( getSparqlForAdditionalUrisInScope() != null && 
            getSparqlForAdditionalUrisInScope().containsKey("editingUser") &&
            USE_SYSTEM_VALUE.equals(getSparqlForAdditionalUrisInScope().get("editingUser"))) {
            
            if( request.getSession() == null )
                throw new Error("EditConfiguration.addSystemValues() needs a session");

            /* ********** Get URI of a logged in user ************** */
            String userUri = EditN3Utils.getEditorUri(request);
           	log.debug("EditConfiguration.java - checking system value for User URI " + userUri);
           	List<String> userUriList = new ArrayList<String>();
           	userUriList.add(userUri);
            getUrisInScope().put("editingUser", userUriList);
        }   
    }
    
    //TODO: Check if require 
    /**
     * Make a copy of this EditConfiguration, prepare for a DataProperty update
     * and return it.  
     */
    public void prepareForDataPropUpdate( Model model, DataPropertyStatement dpStmt){
        if( model == null ) throw new Error("EditConfiguration.prepareForDataPropUpdate() needs a Model");        
        if( isObjectResource ){
           throw new Error("This request seems to be an objectPropertyStmt update, not a DataPropStmt update");
        } else  if (datapropKey == null) {
            throw new Error("This request does not appear to be for an update since it lacks a dataprop object or a dataProp hash key ");                           
        }                     
        
        //TODO: Check if multiple statements might affect this implementation?
        List<Literal> dataPropLiterals = new ArrayList<Literal>();
        dataPropLiterals.add(new EditLiteral(dpStmt.getData(),dpStmt.getDatatypeURI(), dpStmt.getLanguage()));
        getLiteralsInScope().put(varNameForObject, dataPropLiterals);

        // run SPARQL, sub in values
        SparqlEvaluateVTwo sparqlEval = new SparqlEvaluateVTwo(model);
        runSparqlForAdditional( sparqlEval );
        runSparqlForExisting( sparqlEval );
        //Saving previous N3 state before edit
        //build retraction N3 for each Field
        for(String var : getFields().keySet() ){
            FieldVTwo field = getField(var);
            List<String> retractions = null;
            retractions = n3generator.subInMultiLiterals(getLiteralsInScope(),field.getAssertions());
            retractions = n3generator.subInMultiUris(getUrisInScope(), retractions);
            field.setRetractions(retractions);
        }   
    }

    /**
     * Make a copy of this EditConfiguration, prepare for a ObjectProperty update
     * and return it.
     */
    public void prepareForObjPropUpdate( Model model ){
        if( model == null ) {
        	//Added parens and output
        	log.debug("Model is null and will be throwing an error");
        	throw new Error("EditConfiguration.prepareForObjPropUpdate() needs a Model");}
        if( !isObjectResource )
        {
        	//Added parens and output
        	log.debug("This is not an object resource? lacks dataprop ");
            throw new Error("This request does not appear to be for an update since it lacks a dataprop object or a dataProp hash key ");              
        }
            //find the variable for object, this anchors the paths to the existing values
        if( object == null || object.trim().length() == 0)
        {
        	//Added parens and output
        	log.debug("Object is null or object length is null");
            throw new Error("This request does not appear to be for an update since it lacks an object");   
        }
        
        List<String> objectUris = new ArrayList<String>();
        objectUris.add(object);
        getUrisInScope().put( varNameForObject, objectUris);
        log.debug("Putting uris in scope - var name for object " + varNameForObject + " and object is " + object);
        // run SPARQL, sub in values
        SparqlEvaluateVTwo sparqlEval = new SparqlEvaluateVTwo( model );
        runSparqlForAdditional( sparqlEval );
        try {
        	runSparqlForExisting( sparqlEval );
        } catch (Exception e) {
        	e.printStackTrace();
        }

        //build retraction N3 for each Field
        for(String var : getFields().keySet() ){
            FieldVTwo field = getField(var);
            List<String> retractions = null;
            retractions = n3generator.subInMultiLiterals(getLiteralsInScope(),field.getAssertions());
            retractions = n3generator.subInMultiUris(getUrisInScope(), retractions);
            field.setRetractions(retractions);
        }
    }


    public void prepareForNonUpdate( Model model ){
        if( model == null ) throw new Error("EditConfiguration.prepareForNonUpdate() needs a Model");
        
        SparqlEvaluateVTwo sparqlEval = new SparqlEvaluateVTwo( model );
        runSparqlForAdditional( sparqlEval );
        //runSparqlForExisting( sparqlEval );             
    }
    
    public void setFields(Map<String,FieldVTwo> fields) {
    	this.fields = fields;
    }

    public void prepareForResubmit(MultiValueEditSubmission editSub){
        //get any values from editSub and add to scope
    }


    /**
     * Runs the queries for additional uris and literals then add those back into
     * the urisInScope and literalsInScope.
     */
    public void runSparqlForAdditional(SparqlEvaluateVTwo sparqlEval){
        sparqlEval.evaluateForAdditionalUris( this );
        sparqlEval.evalulateForAdditionalLiterals( this );
    }

    public void runSparqlForExisting(SparqlEvaluateVTwo sparqlEval){
        sparqlEval.evaluateForExistingUris( this );
        sparqlEval.evaluateForExistingLiterals( this );
    }

    public FieldVTwo getField(String key){
        if( fields == null) {
            throw new Error("hashmap of field objects must be set before you can get a value from the EditConfiguration");
        }
        return fields.get(key);
    }

    /** return a copy of the value so that the configuration is not modified by external code.
     * @return
     */
    public List<String> getN3Required() {
        List<String> copyForPassByValue = new ArrayList<String> (n3Required.size());
        for( String str : n3Required){
             copyForPassByValue.add(str);
         }
        return copyForPassByValue;
    }

    public void setN3Required(List<String> n3Required) {
        this.n3Required = n3Required;
    }

     /** return a copy of the value so that the configuration is not modified by external code.
     * @return
     */
    public List<String> getN3Optional() {
         List<String> copyForPassByValue = new ArrayList<String> (n3Optional.size());
         for( String str : n3Optional){
             copyForPassByValue.add(str);
         }
        return copyForPassByValue;
    }

    public void setN3Optional(List<String> n3Optional) {
        this.n3Optional = n3Optional;
    }

    public Map<String,String> getNewResources() {
        return newResources;
    }

    public void setNewResources(Map<String,String> newResources) {
        this.newResources = newResources;
    }

    public List<String> getUrisOnform() {
        return urisOnform;
    }

    public void setUrisOnform(List<String> urisOnform) {
        this.urisOnform = urisOnform;
    }

    public void setFilesOnForm(List<String> filesOnForm){
        this.filesOnForm = filesOnForm;
    }

    public List<String> getFilesOnForm(){
        return filesOnForm;
    }

    public List<String> getLiteralsOnForm() {
        return literalsOnForm;
    }

    public void setLiteralsOnForm(List<String> literalsOnForm) {
        this.literalsOnForm = literalsOnForm;
    }

    public Map<String, List<String>> getUrisInScope() {
        return urisInScope;
    }

    public void setUrisInScope(Map<String, List<String>> urisInScope) {
        this.urisInScope = urisInScope;
    }

    public Map<String, List<Literal>> getLiteralsInScope() {
        return literalsInScope;
    }

    public void setLiteralsInScope(Map<String, List<Literal>> literalsInScope) {
        this.literalsInScope = literalsInScope;
    }

    /** return a copy of the value so that the configuration is not modified by external code.
     * @return
     */
    public Map<String, String> getSparqlForAdditionalUrisInScope() {
         Map<String, String> copyForPassByValue = new HashMap<String, String>();
        copy(sparqlForAdditionalUrisInScope, copyForPassByValue);
        return copyForPassByValue;
    }

    public void setSparqlForAdditionalUrisInScope(Map<String, String> sparqlForAdditionalUrisInScope) {
        this.sparqlForAdditionalUrisInScope = sparqlForAdditionalUrisInScope;
    }

     /** return a copy of the value so that the configuration is not modified by external code.
     * @return
     */
    public Map<String, String> getSparqlForAdditionalLiteralsInScope() {
        Map<String, String> copyForPassByValue = new HashMap<String, String>();
        copy(sparqlForAdditionalLiteralsInScope, copyForPassByValue);
        return copyForPassByValue;
    }

    private Map<String,String> copy(Map<String,String> source, Map<String,String> dest){
        if( source == null ) return null;
        dest.clear();
        for( String key : source.keySet()){
            dest.put(key, source.get(key));
        }
        return dest;
    }

    public void setSparqlForAdditionalLiteralsInScope(Map<String, String> sparqlForAdditionalLiteralsInScope) {
        this.sparqlForAdditionalLiteralsInScope = sparqlForAdditionalLiteralsInScope;
    }

    public String getEntityToReturnTo() {
        return entityToReturnTo;
    }

    public void setEntityToReturnTo(String entityToReturnTo) {
        this.entityToReturnTo = entityToReturnTo;
    }

    public String getUrlPatternToReturnTo() {
        return urlPatternToReturnTo;
    }

    public void setUrlPatternToReturnTo(String s) {
        urlPatternToReturnTo = s;
    }

     /** return a copy of the value so that the configuration is not modified by external code.
     * @return
     */
    public Map<String, String> getSparqlForExistingLiterals() {
        Map<String, String> copyForPassByValue = new HashMap<String, String>();
        copy(sparqlForExistingLiterals, copyForPassByValue);
        return copyForPassByValue;
    }

    public void setSparqlForExistingLiterals(Map<String, String> sparqlForExistingLiterals) {
        this.sparqlForExistingLiterals = sparqlForExistingLiterals;
    }

     /** return a copy of the value so that the configuration is not modified by external code.
     * @return
     */
    public Map<String, String> getSparqlForExistingUris() {
         Map<String, String> copyForPassByValue = new HashMap<String, String>();
        copy(sparqlForExistingUris, copyForPassByValue);
        return copyForPassByValue;
    }

    public void setSparqlForExistingUris(Map<String, String> sparqlForExistingUris) {
        this.sparqlForExistingUris = sparqlForExistingUris;
    }


   public Map<String, List<String>> getN3ForFields(){
       return fieldsToMap( getFields() );
   }

    private Map<String,List<String>> fieldsToMap( Map<String,FieldVTwo> fields){
        Map<String,List<String>> out = new HashMap<String,List<String>>();
        for( String fieldName : fields.keySet()){
            FieldVTwo field = fields.get(fieldName);

            List<String> copyOfN3 = new ArrayList<String>();
            for( String str : field.getAssertions()){
                copyOfN3.add(str);
            }
            out.put( fieldName, copyOfN3 );
        }
        return out;
    }

    /* ********************** static methods to get EditConfigs from Session ******************************** */

     public static void clearAllConfigsInSession( HttpSession sess ){
        if(sess == null ) return;
        sess.removeAttribute("editConfiguration");
    }


    public static void clearEditConfigurationInSession(HttpSession session, EditConfigurationVTwo editConfig) {
        if( session == null || editConfig == null )
            return;
        Map<String,EditConfigurationVTwo> configs = (Map<String,EditConfigurationVTwo>)session.getAttribute("EditConfigurations");
        if( configs == null )
            return ;
        if( configs.containsKey( editConfig.editKey ) )
            configs.remove( editConfig.editKey );
    }

    public static void putConfigInSession(EditConfigurationVTwo ec, HttpSession sess){
        if( sess == null )
            throw new Error("EditConfig: could not put config in session because session was null");
        if( ec.editKey == null )
            throw new Error("EditConfig: could not put into session because editKey was null.");

        Map<String,EditConfigurationVTwo> configs = (Map<String,EditConfigurationVTwo>)sess.getAttribute("EditConfigurations");
        if( configs == null ){
            configs = new HashMap<String,EditConfigurationVTwo>();
            sess.setAttribute("EditConfigurations",configs);
        }
        configs.put(ec.editKey , ec);
    }

    public static EditConfigurationVTwo getConfigFromSession(HttpSession sess, String editKey){
        Map<String,EditConfigurationVTwo> configs = (Map<String,EditConfigurationVTwo>)sess.getAttribute("EditConfigurations");
        if( configs == null )
          return null;

        EditConfigurationVTwo config = configs.get( editKey );
        if( config == null )
            return null;
        else
            return config;
    }

    /**
     * This may return null, which indicates that there is no editKey or EditConfiguration in the
     * request or session.  If the queryParams are supplied, look for the editKey
     * there first since multipart parsing might have cleared them from the request.
     */
    public static EditConfigurationVTwo getConfigFromSession( HttpSession sess, HttpServletRequest request ){
        String key = getEditKey(request);
        
        if( key == null )
            return null;
        return getConfigFromSession(sess, key);
    }

    /**
     * The editKey can be a HTTP query parameter or it can be a request attribute.
     */
    public static String getEditKey( ServletRequest request){
        String key = null;
        if( request instanceof HttpServletRequest ){
            HttpServletRequest hsreq = (HttpServletRequest)request;
            boolean isMultipart = ServletFileUpload.isMultipartContent(hsreq);
            if( isMultipart ) {
                //multipart parsing will consume all request parameters so
                //the editKey needs to be stashed in the request attributes.
                key = (String)request.getAttribute("editKey");    
                if( key == null ) {
                    // handle the cancel button where nothing is really uploaded
                    key = request.getParameter("editKey");
                }                               
            }else{            	
            	key = (String)request.getAttribute("editKey");                
                if( key  != null  ){
                    return key;
                } else {
                	key = request.getParameter("editKey");
                }
            }
        }

        if( key != null && key.trim().length() > 0 ){
            return key;
        }else{
            log.debug("cannnot find editKey in request query parameters or from request");
            return null;
        }
    }

    public static String newEditKey(HttpSession sess){
        DateTime time = new DateTime();
        int mills = time.getMillisOfDay();

        Map<String,EditConfigurationVTwo> configs = (Map<String,EditConfigurationVTwo>)sess.getAttribute("EditConfigurations");
        if( configs == null ){
            return Integer.toString(mills);
        }else{
            while( configs.containsKey( Integer.toString(mills) )){
                mills ++;
            }
            return Integer.toString(mills);
        }
    }
    
    /*
     * getters and setters
     */

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public boolean isObjectResource() {
		return isObjectResource;
	}

	public void setObjectResource(boolean isObjectResource) {
		this.isObjectResource = isObjectResource;
	}

	public String getDatapropKey() {
        return datapropKey;
    }

    public void setDatapropKey(String datapropKey) {
        this.datapropKey = datapropKey;
    }
    
    //to allow for external setting
    public void setDatapropValue(String datapropValue) {
    	this.datapropValue = datapropValue;
    }

    public String getSubjectUri() {
        return subjectUri;
    }

    public void setSubjectUri(String subjectUri) {
        this.subjectUri = subjectUri;
    }

    public String getPredicateUri() {
        return predicateUri;
    }

    public void setPredicateUri(String predicateUri) {
        this.predicateUri = predicateUri;
    }

    public Map<String,FieldVTwo> getFields() {
        return fields;
    }

    public String getEditKey() {
        return editKey;
    }
    
    public void setEditKey(String editKey) {
    	this.editKey = editKey;
    }
    
    public String getFormUrl() {
        return formUrl;
    }
    
    public void setFormUrl(String formUrl) {
    	this.formUrl = formUrl;
    }

    public EditN3GeneratorVTwo getN3Generator(){
        return n3generator;
    }
    
    public void setN3Generator(EditN3GeneratorVTwo gen) {
    	this.n3generator = gen;
    }
    
    public String getVarNameForSubject() {
    	return this.varNameForSubject;
    }
    
    public void setVarNameForSubject(String varName) {
    	this.varNameForSubject = varName;
    }
    
    public String getVarNameForPredicate() {
    	return this.varNameForPredicate;
    }
    
    public void setVarNameForPredicate(String varName) {
    	this.varNameForPredicate = varName;
    }

    public String getVarNameForObject(){
        return this.varNameForObject;
    }
    
    public void setVarNameForObject(String varName) {
    	this.varNameForObject = varName;
    }
    
    /**If this is set to true, then dependent resources should be deleted on edits that
     * remove the parent resource.
     */
    public boolean isUseDependentResourceDelete() {
		return useDependentResourceDelete;
	}

    /**If this is set to true, then dependent resources should be deleted on edits that
     * remove the parent resource.
     */
	public void setUseDependentResourceDelete(boolean useDependentResourceDelete) {
		this.useDependentResourceDelete = useDependentResourceDelete;
	}
	
    public List<ModelChangePreprocessor> getModelChangePreprocessors() {
    	return this.modelChangePreprocessors;
    }
    
    public List<ModelChangePreprocessor> setModelChangePreprocessors() {
    	return this.modelChangePreprocessors;
    }
    
    public void addModelChangePreprocessor( ModelChangePreprocessor modelChangePreprocessor) {
    	this.modelChangePreprocessors.add( modelChangePreprocessor );
    }
    
    public void setProhibitedFromSearch( ProhibitedFromSearch prohibitedFromSearch) {
    	this.prohibitedFromSearch = prohibitedFromSearch;
    }
    
    public ProhibitedFromSearch getProhibitedFromSearch() {
    	return this.prohibitedFromSearch;
    }
    
    private void debugScope(String msg){
        if( log.isDebugEnabled()){
            log.debug(msg);
            log.debug("literalsInScope:");
            for( String key: literalsInScope.keySet() ){
                String val = literalsInScope.get(key).toString();
                log.debug( key + " " + val );
            }
            log.debug("uris in scope: " );
            for( String key: urisInScope.keySet() ){
                String val = urisInScope.get(key).toString();
                log.debug( key + " " + val );
            }
        }
    }
    
    public final static String USE_SYSTEM_VALUE = "USE_SYSTEM_VALUE";
    
    private static final Log log = LogFactory.getLog(EditConfigurationVTwo.class.getName());

    public ModelSelector getWriteModelSelector() {
        return writeModelSelector;
    }

    public void setWriteModelSelector(ModelSelector writeModel) {
        if( writeModel != null )
            this.writeModelSelector = writeModel;        
    }

    public ModelSelector getQueryModelSelector() {
        return queryModelSelector;
    }

    public void setQueryModelSelector(ModelSelector queryModel) {
        if( queryModel != null )
            this.queryModelSelector = queryModel;
    }

    public WDFSelector getWdfSelectorForOptons() {
        return wdfSelectorForOptons;
    }

    public void setWdfSelectorForOptons(WDFSelector wdfForOptons) {
        this.wdfSelectorForOptons = wdfForOptons;
    }

    public ModelSelector getResourceModelSelector() {
        return resourceModelSelector;
    }

    public void setResourceModelSelector(ModelSelector resourceModelSelector) {
        if( resourceModelSelector != null )
            this.resourceModelSelector = resourceModelSelector;
    }
        
    public List<N3Validator> getValidators() {
		return validators;
	}

    public void addValidator( N3Validator validator){
    	if( this.validators == null )
    		this.validators = new ArrayList<N3Validator>();
    	this.validators.add(validator);    		
    }    

    public void addEditSubmissionPreprocessor( EditSubmissionVTwoPreprocessor preprocessor){
        if( editSubmissionPreprocessors == null )
            editSubmissionPreprocessors = new ArrayList<EditSubmissionVTwoPreprocessor>();
        editSubmissionPreprocessors.add(preprocessor);         
    }  
    
    public List<EditSubmissionVTwoPreprocessor> getEditSubmissionPreprocessors() {
        return editSubmissionPreprocessors;
    }

    public void setTemplate(String template){
        this.template = template;
    }
    
    public String getTemplate() {
        return this.template;
    }

    public String getSubmitToUrl() {
        return submitToUrl;
    }

    public void setSubmitToUrl(String submitToUrl) {
        this.submitToUrl = submitToUrl;
    }
    
    public boolean isUpdate(){
        return this.getObject() != null && this.getObject().trim().length() > 0;  
    }
}
