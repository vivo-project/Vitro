/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.ModelSelector;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.StandardModelSelector;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.StandardWDFSelector;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.WDFSelector;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DefaultDataPropertyFormGenerator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.ModelChangePreprocessor;
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
    List<String> n3Required = new ArrayList<String>();
    //String representing optional N3 for RDF
    List<String> n3Optional = new ArrayList<String>();
    //Names of variables of 'objects' i.e. URIs on form
    List<String> urisOnform = new ArrayList<String>();
    //Names of variables corresponding to data values i.e. literals on form
    List<String> literalsOnForm = new ArrayList<String>();
    //Names of variables corresponding to Files on form
    List<String> filesOnForm = new ArrayList<String>();
    
    //Multi values now supported for uris and literals, so second parameter needs to be List<String>
    //Mapping of variable name for object to values for object, i.e. URIs, e.g. "hasElement" = "<a.com>, <b.com>"
    Map<String,List<String>> urisInScope = new HashMap<String,List<String>>();
    //Mapping from variable name to values for literals
    Map<String, List<Literal>> literalsInScope = new HashMap<String,List<Literal>>();
    
    //Map name of variable to sparql query which should return a one-column result set of URIs corresponding to variable
    //E.g. sparql for inverse of object property
    Map<String,String> sparqlForAdditionalUrisInScope = new HashMap<String,String>();
    //Mapping variable to sparql query returning literals 
    Map<String,String> sparqlForAdditionalLiteralsInScope = new HashMap<String,String>();
    
    //Variable names to URI prefixes for variables that are allowed to have new instances created
    Map<String,String> newResources = new HashMap<String,String>();
    
    //Variable names to fields, Field = additional configuration for variable
    Map<String,FieldVTwo> fields = new HashMap<String,FieldVTwo>();
    
    //Mapping variable name to Sparql query to find existing literals corresponding to variable, result set should be one-column multi-row of literals
    Map<String,String>sparqlForExistingLiterals = new HashMap<String,String>();
    //Mapping variable name to Sparql query to find existing URIs corresponding to variable, result set should be one-column multi-row of URIs/URI resources
    Map<String,String>sparqlForExistingUris = new HashMap<String,String>();

    String subjectUri;
    String varNameForSubject;

    String predicateUri;
    String varNameForPredicate;

    /** When this is a DataPropertyStmt edit, the object is not used, the
     * DataPropertyStatement is retrieved using the subject, predicate and the
     * datapropKey.  When this edit is for a ObjectPropertyStmt,
     * object is the URI, it has no quoting or &lt; or &gt;.
     */
    String object;
    
    /**
     * This can be the variable name for the object of a statement in
     * a object property or data property form.
     */
    String varNameForObject;   

    Integer datapropKey=null;
    
    /** urlPatternToReturnTo is the URL to use as the servlet to return to.
     * Usually it is "/individual" and entityToReturnTo will be added as a
     * "uri" parameter.   */
    String urlPatternToReturnTo = INDIVIDUAL_CONTROLLER ;
    
    /** If this is non-null it should be the URI of an Individual to return to after
     * the edit. */     
    String entityToReturnTo;
    
    /**
     * If this value is not null, it will force the edit to return to the specified 
     * URL from the PostEditCleanupController after an edit or a cancel.  This string does 
     * NOT get values substituted in and it does NOT get the context appended. 
     */
    String urlToReturnTo = null;
    
    /**
     * formUrl saves the URL that was used to request the form so that it can be 
     * reissued if a form validation fails and the client can be redirected back
     * to the original form. */
    String formUrl;
    
    /**
     * skipToUrl is a URL that should be forwarded to instead of displaying
     * a form.  This will need the context if it is relative.  It may be a
     * full off site URL.
     */
    String skipToUrl;
    
    String editKey;

    List<N3ValidatorVTwo> validators;

	EditN3GeneratorVTwo n3generator;   

    private List<ModelChangePreprocessor> modelChangePreprocessors =new LinkedList<ModelChangePreprocessor>();
    
    private List<EditSubmissionVTwoPreprocessor> editSubmissionPreprocessors;
    
    private ProhibitedFromSearch prohibitedFromSearch;

    //TODO: can we rename this to match the name "pageData" that is used on the templates for this?
    private HashMap<String, Object> formSpecificData;
    
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
    
    private boolean hasBeenPreparedForUpdate;
    
    public EditConfigurationVTwo(){ 
        writeModelSelector = StandardModelSelector.selector;
        queryModelSelector = StandardModelSelector.selector;   
        resourceModelSelector = StandardModelSelector.selector;
        wdfSelectorForOptons = StandardWDFSelector.selector;        
        n3generator = new EditN3GeneratorVTwo();
    }

    //Make copy of edit configuration object
    public EditConfigurationVTwo copy() {
    	EditConfigurationVTwo editConfig = new EditConfigurationVTwo();
    	//Copy n3generator - make copy of n3generator or get something else?
    	editConfig.setN3Generator( this.getN3Generator() );
    	//For remaining ensure we make copies of all the objects and we don't use refererences
    	//Set form url
    	editConfig.setFormUrl(this.getFormUrl());
    	//Set edit key
    	editConfig.setEditKey(this.getEditKey());
    	//subject, predicate
    	editConfig.setUrlPatternToReturnTo(this.getUrlPatternToReturnTo());
    	editConfig.setVarNameForSubject(this.getVarNameForSubject());
    	editConfig.setSubjectUri(this.getSubjectUri());
    	editConfig.setEntityToReturnTo(this.getEntityToReturnTo());
    	editConfig.setVarNameForPredicate(this.getVarNameForPredicate());
    	editConfig.setPredicateUri(this.getPredicateUri());
    	//object or data parameters based on whether object or data property
    	if(this.getObject() != null) {
    		editConfig.setObject(this.getObject());
    	}
    	editConfig.setVarNameForObject(this.getVarNameForObject());

    	editConfig.setDatapropKey(this.getDatapropKey());
    	    	
    	editConfig.setUrlPatternToReturnTo(this.getUrlPatternToReturnTo());
   
    	//n3 required
    	editConfig.setN3Required(EditConfigurationUtils.copy(this.getN3Required()));
    	//n3 optional
    	editConfig.setN3Optional(EditConfigurationUtils.copy(this.getN3Optional()));
    	//uris on form
    	editConfig.setUrisOnform(EditConfigurationUtils.copy(this.getUrisOnform()));
    	//literals on form
    	editConfig.setLiteralsOnForm(EditConfigurationUtils.copy(this.getLiteralsOnForm()));
    	//files on form
    	editConfig.setFilesOnForm(EditConfigurationUtils.copy(this.getFilesOnForm()));
    	//new resources    	
    	editConfig.setNewResources(EditConfigurationUtils.copyMap(this.getNewResources()));
    	//uris in scope
    	editConfig.setUrisInScope(EditConfigurationUtils.copyListMap(this.getUrisInScope()));

    	//TODO: ensure this is not a shallow copy of literals but makes entirely new literal objects
    	editConfig.setLiteralsInScope(this.getLiteralsInScope());    	
    	//editConfig.setLiteralsInScope(EditConfigurationUtils.copy(this.getLiteralsInScope()));
    	
    	//sparql for additional uris in scope
    	editConfig.setSparqlForAdditionalUrisInScope(
    			EditConfigurationUtils.copyMap(this.getSparqlForAdditionalUrisInScope()));     					
    	//sparql for additional literals in scope
    	editConfig.setSparqlForAdditionalLiteralsInScope(
    			EditConfigurationUtils.copyMap(this.getSparqlForAdditionalLiteralsInScope()));     					
    	//sparql for existing literals
    	editConfig.setSparqlForExistingLiterals(
    			EditConfigurationUtils.copyMap(this.getSparqlForExistingLiterals()));     					
    	//sparql for existing uris
    	editConfig.setSparqlForExistingUris(
    			EditConfigurationUtils.copyMap(this.getSparqlForExistingUris()));     					
    	
    	//TODO: Ensure this is true copy of field and not just shallow copy with same references
    	Map<String, FieldVTwo> fields = this.getFields();
    	editConfig.setFields(fields);
    	
    	return editConfig;
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
    		literalsInScope.put("currentTime", dateLiterals);
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
            urisInScope.put("editingUser", userUriList);
        }   
    }
    
    protected void basicPrepare( ){
        if( subjectUri != null && ! subjectUri.trim().isEmpty() && 
            varNameForSubject != null && ! varNameForSubject.trim().isEmpty() ){            
            urisInScope.put( varNameForSubject, Arrays.asList( subjectUri ));
            log.debug("Putting uris in scope - var name for subject "
                    + varNameForSubject + " and subject is " + subjectUri);
        }
        
        if( predicateUri != null && ! predicateUri.trim().isEmpty() &&
            varNameForPredicate != null && ! varNameForPredicate.trim().isEmpty() ){
            urisInScope.put( varNameForPredicate, Arrays.asList( predicateUri ));
            log.debug("Putting uris in scope - var name for predicate "
                    + varNameForPredicate + " and predicate is " + predicateUri);
        }
        
        if( isObjectResource() && 
            varNameForObject != null && ! varNameForObject.trim().isEmpty() ){
            urisInScope.put( varNameForObject, Arrays.asList(getObject()));
            log.debug("Putting uris in scope - var name for object " 
                    + varNameForObject + " and object is " + object);
        }        
    }
     
    /**
     * Prepare an EditConfiguration for a DataProperty update.
     * This should only be used when editing a existing data property statement. 
     */
    public void prepareForDataPropUpdate( Model model, DataPropertyDao dataPropertyDao ){
        if( model == null ) throw new Error("EditConfiguration.prepareForDataPropUpdate() needs a Model");        
        if( isObjectResource() ){
           throw new Error("This request does not seems to be a DataPropStmt update");
        } else  if (datapropKey == null) {
            throw new Error("This request does not appear to be for an update since it lacks a dataProp hash key ");                           
        }                     
        
        basicPrepare();
        
        DefaultDataPropertyFormGenerator.prepareForDataPropUpdate(model, this,dataPropertyDao );        

        // run SPARQL, sub in values
        SparqlEvaluateVTwo sparqlEval = new SparqlEvaluateVTwo(model);
        runSparqlForAdditional( sparqlEval );
        runSparqlForExisting( sparqlEval );
       
        hasBeenPreparedForUpdate = true;
    }

    /**
     * Prepare for a ObjectProperty update. Run SPARQL for existing values.
     *  This can be used for an object property or a direct form.
     */
    public void prepareForObjPropUpdate( Model model ){
        if( model == null ) {
        	log.debug("Model is null and will be throwing an error");
        	throw new Error("EditConfiguration.prepareForObjPropUpdate() needs a non-null Model");}
        if( !isObjectResource() ) {
        	log.debug("This does not seem to be an object property update. Lacks object.");
            throw new Error("This request does not appear to be for a object property update.");              
        }        
        
        basicPrepare();        
        
        // run SPARQL, sub in values
        SparqlEvaluateVTwo sparqlEval = new SparqlEvaluateVTwo( model );
        runSparqlForAdditional( sparqlEval );
        try {
        	runSparqlForExisting( sparqlEval );
        } catch (Exception e) {
        	e.printStackTrace();
        }

        hasBeenPreparedForUpdate = true;
    }

    /**
     * Run SPARQL for Additional values.  This can be used for
     * a data property, an object property or a direct form.
     */
    public void prepareForNonUpdate( Model model ){
        if( model == null ) throw new Error("prepareForNonUpdate() needs a non-null Model");

        basicPrepare();
        
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

    /** Return a copy of the value so that the configuration is not modified by external code.
     * @return
     */
    public List<String> getN3Required() {
        return EditConfigurationUtils.copy(n3Required);
    }

    //TODO: can we use varargs here?
    public void setN3Required(List<String> n3Required) {
        this.n3Required = n3Required;
    }

     /** return a copy of the value so that the configuration is not modified by external code.
     * @return
     */
    public List<String> getN3Optional() {
         return EditConfigurationUtils.copy( n3Optional );
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

    public EditConfigurationVTwo addUrisInScope(String key, List<String> list) {
        if( urisInScope  == null ){
            urisInScope = new HashMap<String, List<String>>();
        }
        urisInScope.put(key, list);
        return this;        
    }
    
    public Map<String, List<Literal>> getLiteralsInScope() {
        return literalsInScope;
    }

    public void setLiteralsInScope(Map<String, List<Literal>> literalsInScope) {
        this.literalsInScope = literalsInScope;
    }

    /** Return a copy of the value so that the configuration is not modified by external code.
     * @return
     */
    public Map<String, String> getSparqlForAdditionalUrisInScope() {        
        return copyMap(sparqlForAdditionalUrisInScope);        
    }

    public void setSparqlForAdditionalUrisInScope(Map<String, String> sparqlForAdditionalUrisInScope) {
        this.sparqlForAdditionalUrisInScope = sparqlForAdditionalUrisInScope;
    }

     /** Return a copy of the value so that the configuration is not modified by external code.
     * @return
     */
    public Map<String, String> getSparqlForAdditionalLiteralsInScope() {        
        return copyMap(sparqlForAdditionalLiteralsInScope);        
    }        
    
    private Map<String,String> copyMap(Map<String,String> source){
        if( source == null ) return null;
        Map<String, String> dest = new HashMap<String, String>();        
        for( String key : source.keySet()){
            if( source.get(key) != null )
                dest.put(new String(key), source.get(key));
            else 
                dest.put(new String(key), null);
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
        return copyMap(sparqlForExistingLiterals);        
    }

    public void setSparqlForExistingLiterals(Map<String, String> sparqlForExistingLiterals) {
        this.sparqlForExistingLiterals = sparqlForExistingLiterals;
    }

     /** return a copy of the value so that the configuration is not modified by external code.
     * @return
     */
    public Map<String, String> getSparqlForExistingUris() {         
        return copyMap(sparqlForExistingUris);        
    }

    public void setSparqlForExistingUris(Map<String, String> sparqlForExistingUris) {
        this.sparqlForExistingUris = sparqlForExistingUris;
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
        String key = getEditKeyFromRequest(request);
        
        if( key == null )
            return null;
        return getConfigFromSession(sess, key);
    }

    /**
     * The editKey can be a HTTP query parameter or it can be a request attribute.
     */
    public static String getEditKeyFromRequest( ServletRequest request){
        String key = null;
        if( request instanceof VitroRequest ){
            return request.getParameter("editKey");
        }else if( request instanceof HttpServletRequest ){
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

    @SuppressWarnings("unchecked")
    public static String newEditKey(HttpSession sess){
        int mills = new DateTime().getMillisOfDay();

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
        boolean objectFound = false;
        boolean dataKeyFound = false;
		if( object != null && ! object.trim().isEmpty() )
		    objectFound = true;
		if( getDatapropKey() != null )
		    dataKeyFound = true;
		if( dataKeyFound && objectFound )
		    throw new Error("Bad configuration: both datapropKey and object are defined.");
		return objectFound;
	}	

	public Integer getDatapropKey() {
        return datapropKey;
    }

    public void setDatapropKey(Integer datapropKey) {
        this.datapropKey = datapropKey;
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
        
    public List<N3ValidatorVTwo> getValidators() {
		return validators;
	}

    public void addValidator( N3ValidatorVTwo validator){
    	if( this.validators == null )
    		this.validators = new ArrayList<N3ValidatorVTwo>();
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
        return isObjectPropertyUpdate() || isDataPropertyUpdate(); 
    }
    
    public boolean isObjectPropertyUpdate(){
        return this.getObject() != null && this.getObject().trim().length() > 0;  
    }
    
    public boolean isDataPropertyUpdate() {
    	return this.getDatapropKey() != null ;
    }
    
    //TODO: can we rename this to match the name "pageData" that is used on the templates for this? 
    //This is for specific data for a form that will be set by the generator    
	public  void setFormSpecificData(HashMap<String, Object> formSpecificData) {
		this.formSpecificData = formSpecificData;
	}

    //TODO: can we rename this to match the name "pageData" that is used on the templates for this?
	public void addFormSpecificData( String key, Object value){
	    if( this.formSpecificData == null){
	        this.formSpecificData = new HashMap<String,Object>();
	    }
	    this.formSpecificData.put(key,value);	    
	}
	
    //TODO: can we rename this to match the name "pageData" that is used on the templates for this?
	public HashMap<String, Object> getFormSpecificData() {
		// TODO Auto-generated method stub
		return this.formSpecificData;
	}

	public boolean hasBeenPreparedForUpdate() {
	    return hasBeenPreparedForUpdate;
	}
	
    public void addNewResource(String key, String namespace){        
        if( key == null || key.isEmpty() ) 
            throw new IllegalArgumentException("key of new resource must not be null");
        if( newResources == null ) {
            newResources = new HashMap<String,String>();
            newResources.put(key, namespace);            
        }else{
            newResources.put(key, namespace);            
        }        
    }
    
    public void addSparqlForExistingLiteral(String key, String sparql){
        if( key == null || key.isEmpty() ) 
            throw new IllegalArgumentException("key must not be null");
        if( sparql == null || sparql .isEmpty() ) 
            throw new IllegalArgumentException("sparql must not be null");
        
        Map<String,String> map = sparqlForExistingLiterals;
        if( map == null ) {
            map = new HashMap<String,String>();
            map.put(key, sparql);   
            setSparqlForExistingLiterals(map);
        }else{
            map.put(key, sparql);            
        }        
    }
    
    public void addSparqlForExistingUris(String key, String sparql){
        if( key == null || key.isEmpty() ) 
            throw new IllegalArgumentException("key must not be null");
        if( sparql == null || sparql .isEmpty() )
            throw new IllegalArgumentException("sparql must not be null");
        
        Map<String,String> map = sparqlForExistingUris;
        if( map == null ) {
            map = new HashMap<String,String>();
            map.put(key, sparql);
            setSparqlForExistingUris(map);
        }else{
            map.put(key, sparql);            
        }        
    }
    
    public void addSparqlForAdditionalLiteralsInScope(String key, String sparql){
        if( key == null || key.isEmpty() ) 
            throw new IllegalArgumentException("key must not be null");
        if( sparql == null || sparql .isEmpty() ) 
            throw new IllegalArgumentException("sparql must not be null");
        
        Map<String,String> map = sparqlForAdditionalLiteralsInScope;
        if( map == null ) {
            map = new HashMap<String,String>();
            map.put(key, sparql);      
            setSparqlForAdditionalLiteralsInScope(map);
        }else{
            map.put(key, sparql);            
        }        
    }
    
    public void addSparqlForAdditionalUrisInScope(String key, String sparql){
        if( key == null || key.isEmpty() ) 
            throw new IllegalArgumentException("key must not be null");
        if( sparql == null || sparql .isEmpty() )
            throw new IllegalArgumentException("sparql must not be null");
        
        Map<String,String> map = sparqlForAdditionalUrisInScope;
        if( map == null ) {
            map = new HashMap<String,String>();
            map.put(key, sparql);
            setSparqlForAdditionalUrisInScope(map);
        }else{
            map.put(key, sparql);            
        }        
    }

    public void addField( FieldVTwo field){
        if( field == null ) 
            throw new IllegalArgumentException("field must not be null");
        if( field.getName() == null || field.getName().isEmpty() ) 
            throw new IllegalArgumentException("field must not have null or empty name");        
        if( fields == null )
            fields = new HashMap<String, FieldVTwo>();
                
        if( fields.containsKey(field.getName() ))
            throw new IllegalArgumentException("adding filed that is already in the field list");
        
        fields.put( field.getName(), field);                
    }

    @Override
    public String toString(){        
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);        
    }
    
    private static final String INDIVIDUAL_CONTROLLER = "/individual";

    public EditConfigurationVTwo addLiteralInScope(String key, Literal ... values) {
        if( literalsInScope  == null ){
            literalsInScope = new HashMap<String, List<Literal>>();
        }        
        literalsInScope.put(key, Arrays.asList(values));
        return this;                
    }

    public void setUrlToReturnTo(String url){
        this.urlToReturnTo = url;
    }

    public String getUrlToReturnTo() {
        return this.urlToReturnTo;
    }

    public void setSkipToUrl(String url){
        skipToUrl=url;
    }
    
    public String getSkipToUrl() {
        return skipToUrl;
    }
    
}
