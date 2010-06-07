<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.ontology.OntModel" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="com.hp.hpl.jena.rdf.model.ModelFactory" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Resource" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Literal" %>
<%@ page import="com.hp.hpl.jena.rdf.model.ResourceFactory" %>
<%@ page import="com.hp.hpl.jena.shared.Lock" %>
<%@ page import="com.thoughtworks.xstream.XStream" %>
<%@ page import="com.thoughtworks.xstream.io.xml.DomDriver" %>
<%@ page import="edu.cornell.mannlib.vedit.beans.LoginFormBean" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditN3Generator" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.Field" %>
<%@ page import="java.io.StringReader" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Iterator" %>
<%@page import="org.apache.commons.logging.LogFactory"%>
<%@page import="org.apache.commons.logging.Log"%>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<%@page import="java.io.InputStream"%>
<%@page import="org.apache.commons.fileupload.FileItemIterator"%>
<%@page import="org.apache.commons.fileupload.FileItemStream"%>
<%@page import="org.apache.commons.fileupload.util.Streams"%>
<%@page import="com.hp.hpl.jena.rdf.model.Property"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary"%>
<%@page import="java.io.File"%>
<%@page import="org.apache.commons.fileupload.FileItem"%>
<%@page import="org.apache.commons.fileupload.FileItemFactory"%>
<%@page import="org.apache.commons.fileupload.disk.DiskFileItemFactory"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.ServletIdentifierBundleFactory"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.RoleIdentifier"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditN3Utils"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.ModelChangePreprocessor"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page import="java.net.URLDecoder" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<%-- 2nd prototype of processing.

This one takes two lists of n3, on required and one optional.  If
all of the variables in the required n3 are not bound or it cannot
be processed as n3 by Jena then it is an error in processing the form.
The optional n3 blocks will proccessed if their variables are bound and
are well formed.
--%>
<%    
    if( session == null)
        throw new Error("need to have session");
    boolean selfEditing = VitroRequestPrep.isSelfEditing(request);
    if (!selfEditing && !LoginFormBean.loggedIn(request, LoginFormBean.NON_EDITOR)) {
%>
        
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.jena.DependentResourceDeleteJena"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.beans.Individual"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.InsertException"%><c:redirect url="<%= Controllers.LOGIN %>" />      
<%
    }
    
    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    
    /* the post parameters seem to get consumed by the parsing so
     * we have to make a copy. */
    Map <String,String[]> queryParameters = null;        
    queryParameters = vreq.getParameterMap();        
 
    List<String>  errorMessages = new ArrayList<String>();                   
    
    //this version has been removed from the updated code
    //EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,vreq,queryParameters); 
    EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session, request);
    if( editConfig == null ){
        %><jsp:forward page="/edit/messages/noEditConfigFound.jsp"/><%
    }    
    EditN3Generator n3Subber = editConfig.getN3Generator();     
    EditSubmission submission = new EditSubmission(queryParameters,editConfig);
           
    /* entity to return to may be a variable */
    List<String> entToReturnTo = new ArrayList<String>(1);
    if( editConfig.getEntityToReturnTo() != null ){        
        entToReturnTo.add(" "+editConfig.getEntityToReturnTo()+" ");
    }    
    
    Map<String,String> errors = submission.getValidationErrors();
    EditSubmission.putEditSubmissionInSession(session,submission);

    if(  errors != null && ! errors.isEmpty() ){   
        String form = editConfig.getFormUrl();
        vreq.setAttribute("formUrl", form);
        vreq.setAttribute("view", vreq.getParameter("view"));
        %>
            <jsp:forward page="${formUrl}" />
        <%
        return;
    }

    OntModel queryModel = editConfig.getQueryModelSelector().getModel(request,application);
    OntModel resourcesModel = editConfig.getResourceModelSelector().getModel(request,application);    
    
    List<Model> requiredAssertions  = null;
    List<Model> requiredRetractions = null;
    List<Model> optionalAssertions  = null;
    
    boolean requestIsAnUpdate =  editConfig.getObject() != null && editConfig.getObject().trim().length() > 0; 
    if( requestIsAnUpdate ){
        //handle update of an existing object    
        if( log.isDebugEnabled()) log.debug("editing an existing resource: " + editConfig.getObject() );        

        Map<String,List<String>> fieldAssertions = fieldsToAssertionMap(editConfig.getFields());
        Map<String,List<String>> fieldRetractions= fieldsToRetractionMap(editConfig.getFields());        
        
        /* ********** URIs and Literals on Form/Parameters *********** */
        fieldAssertions = n3Subber.substituteIntoValues(  submission.getUrisFromForm(), submission.getLiteralsFromForm(), fieldAssertions);
        if(log.isDebugEnabled()) logAddRetract("substituted in literals from form",fieldAssertions,fieldRetractions);
        entToReturnTo = n3Subber.subInUris(submission.getUrisFromForm(),entToReturnTo);        
        //fieldRetractions does NOT get values from form.
        
        /* ****************** URIs and Literals in Scope ************** */
        fieldAssertions = n3Subber.substituteIntoValues(editConfig.getUrisInScope(), editConfig.getLiteralsInScope(), fieldAssertions );
        fieldRetractions = n3Subber.substituteIntoValues(editConfig.getUrisInScope(), editConfig.getLiteralsInScope(), fieldRetractions);
        if(log.isDebugEnabled()) logAddRetract("substituted in URIs and Literals from scope",fieldAssertions,fieldRetractions);
        entToReturnTo = n3Subber.subInUris(editConfig.getUrisInScope(),entToReturnTo);
        
        //do edits ever need new resources? (YES)
        Map<String,String> varToNewResource = newToUriMap(editConfig.getNewResources(),wdf);
        fieldAssertions = n3Subber.substituteIntoValues(varToNewResource, null, fieldAssertions);
        if(log.isDebugEnabled()) logAddRetract("substituted in URIs for new resources",fieldAssertions,fieldRetractions);
        entToReturnTo = n3Subber.subInUris(varToNewResource,entToReturnTo);
        //fieldRetractions does NOT get values from form.

        //editing an existing statement
        List<Model> requiredFieldAssertions  = new ArrayList<Model>();
        List<Model> requiredFieldRetractions = new ArrayList<Model>();
        for(String fieldName: fieldAssertions.keySet()){
            Field field = editConfig.getFields().get(fieldName);

            /* CHECK that field changed, then add assertions and retractions */
            
            // No longer checking if field has changed, because assertions and retractions
            // are mutually diffed before statements are added to or removed from the model.
            // The explicit change check can cause problems
            // in more complex setups, like the automatic form building in DataStaR.
            
            if (true) { // ( hasFieldChanged(fieldName, editConfig, submission) ){
				//log.debug("field "+fieldName+" has changed ...");
                /* if the field was a checkbox then we need to something special */

                List<String> assertions = fieldAssertions.get(fieldName);
                List<String> retractions = fieldRetractions.get(fieldName);
                for( String n3 : assertions){
                    try{
                        Model model = ModelFactory.createDefaultModel();
                        StringReader reader = new StringReader(n3);
                        model.read(reader, "", "N3");
                        requiredFieldAssertions.add(model);
                    }catch(Throwable t){
                    	String errMsg = "error processing N3 assertion string from field " + fieldName + "\n"+
                        t.getMessage() + '\n' + "n3: \n" + n3;
                        errorMessages.add(errMsg);
                        if ( log.isDebugEnabled() ) {
                        	log.debug( errMsg );
                        }
                    }
                }
                for( String n3 : retractions ){
                    try{
                        Model model = ModelFactory.createDefaultModel();
                        StringReader reader = new StringReader(n3);
                        model.read(reader, "", "N3");
                        requiredFieldRetractions.add(model);
                    }catch(Throwable t){
                    	String errMsg = "error processing N3 retraction string from field " + fieldName + "\n"+
                        t.getMessage() + '\n' +
                        "n3: \n" + n3;
                        errorMessages.add(errMsg);
                        if ( log.isDebugEnabled() ) {
                        	log.debug( errMsg );
                        }
                    }
                }
            }
        }
        requiredAssertions = requiredFieldAssertions;
        requiredRetractions = requiredFieldRetractions;
        optionalAssertions = Collections.EMPTY_LIST;
        
    } else {
        if( log.isDebugEnabled()) log.debug("creating a new relation " + editConfig.getPredicateUri() );
        //handle creation of a new object property and maybe a resource
        List<String> n3Required = editConfig.getN3Required();
        List<String> n3Optional = editConfig.getN3Optional();
        
        /* ********** URIs and Literals on Form/Parameters *********** */
        //sub in resource uris off form
        n3Required = n3Subber.subInUris(submission.getUrisFromForm(), n3Required);
        n3Optional = n3Subber.subInUris(submission.getUrisFromForm(), n3Optional);        
        if(log.isDebugEnabled()) logRequiredOpt("substituted in URIs  off from ",n3Required,n3Optional);
        entToReturnTo = n3Subber.subInUris(submission.getUrisFromForm(), entToReturnTo);

        //sub in literals from form
        n3Required = n3Subber.subInLiterals(submission.getLiteralsFromForm(), n3Required);
        n3Optional = n3Subber.subInLiterals(submission.getLiteralsFromForm(), n3Optional);
        if(log.isDebugEnabled()) logRequiredOpt("substituted in literals off from ",n3Required,n3Optional);

        /* ****************** URIs and Literals in Scope ************** */        
        n3Required = n3Subber.subInUris( editConfig.getUrisInScope(), n3Required);
        n3Optional = n3Subber.subInUris( editConfig.getUrisInScope(), n3Optional);
        if(log.isDebugEnabled()) logRequiredOpt("substituted in URIs from scope ",n3Required,n3Optional);
        entToReturnTo = n3Subber.subInUris(editConfig.getUrisInScope(), entToReturnTo);
        
        n3Required = n3Subber.subInLiterals( editConfig.getLiteralsInScope(), n3Required);
        n3Optional = n3Subber.subInLiterals( editConfig.getLiteralsInScope(), n3Optional);
        if(log.isDebugEnabled()) logRequiredOpt("substituted in Literals from scope ",n3Required,n3Optional);
        
        /* ****************** New Resources ********************** */
        Map<String,String> varToNewResource = newToUriMap(editConfig.getNewResources(),wdf);         

        //if we are editing an existing prop, no new resources will be substituted since the var will
        //have already been substituted in by urisInScope.
        n3Required = n3Subber.subInUris( varToNewResource, n3Required);
        n3Optional = n3Subber.subInUris( varToNewResource, n3Optional);
        if(log.isDebugEnabled()) logRequiredOpt("substituted in URIs for new resources  ",n3Required,n3Optional);               
        entToReturnTo = n3Subber.subInUris(varToNewResource, entToReturnTo);
        
        //deal with required N3
        List<Model> requiredNewModels = new ArrayList<Model>();
         for(String n3 : n3Required){
             try{
                 Model model = ModelFactory.createDefaultModel();
                 StringReader reader = new StringReader(n3);
                 model.read(reader, "", "N3");
                 requiredNewModels.add( model );
             }catch(Throwable t){
                 errorMessages.add("error processing required n3 string \n"+
                         t.getMessage() + '\n' +
                         "n3: \n" + n3 );
             }
         }
        if( !errorMessages.isEmpty() ){

            String error = "problems processing required n3: \n";
            for( String errorMsg : errorMessages){
                error += errorMsg + '\n';
            }
            throw new JspException("errors processing required N3,\n" +  error );
        }
        requiredAssertions = requiredNewModels;
        requiredRetractions = Collections.EMPTY_LIST;

        //deal with optional N3
        List<Model> optionalNewModels = new ArrayList<Model>();
        for(String n3 : n3Optional){
            try{
                Model model = ModelFactory.createDefaultModel();
                StringReader reader = new StringReader(n3);
                model.read(reader, "", "N3");
                optionalNewModels.add(model);
            }catch(Throwable t){
                errorMessages.add("error processing optional n3 string  \n"+
                        t.getMessage() + '\n' +
                        "n3: \n" + n3);
            }
        }
        optionalAssertions = optionalNewModels;        
    }

    
    //The requiredNewModels and the optionalNewModels could be handled differently
    //but for now we'll just do them the same
    requiredAssertions.addAll(optionalAssertions);

    //************************************************************
    //make a model with all the assertions and a model with all the 
    //retractions, do a diff on those and then only add those to the 
    //jenaOntModel
    //************************************************************
        
    Model allPossibleAssertions = ModelFactory.createDefaultModel();
    Model allPossibleRetractions = ModelFactory.createDefaultModel();
    
    for( Model model : requiredAssertions ) {
        allPossibleAssertions.add( model );
    }
    for( Model model : requiredRetractions ){
        allPossibleRetractions.add( model );
    }
    
    Model actualAssertions = allPossibleAssertions.difference( allPossibleRetractions );    
    Model actualRetractions = allPossibleRetractions.difference( allPossibleAssertions );
        
    if( editConfig.isUseDependentResourceDelete() ){
    	Model depResRetractions = 
    		DependentResourceDeleteJena
    		.getDependentResourceDeleteForChange(actualAssertions,actualRetractions,queryModel);
    	actualRetractions.add( depResRetractions );
    }
    
    List<ModelChangePreprocessor> modelChangePreprocessors = editConfig.getModelChangePreprocessors();
    if ( modelChangePreprocessors != null ) {
        for ( ModelChangePreprocessor pp : modelChangePreprocessors ) {
        	pp.preprocess( actualRetractions, actualAssertions, request );
        }
    }
    
    // get the model to write to here in case a preprocessor has switched the write layer
    OntModel writeModel = editConfig.getWriteModelSelector().getModel(request,application);  
   
    String editorUri = EditN3Utils.getEditorUri(vreq,session,application); 
    Lock lock = null;
    try{
        lock =  writeModel.getLock();
        lock.enterCriticalSection(Lock.WRITE);
        writeModel.getBaseModel().notifyEvent(new EditEvent(editorUri,true));   
        writeModel.add( actualAssertions );
        writeModel.remove( actualRetractions );
    }catch(Throwable t){
        errorMessages.add("error adding edit change n3required model to in memory model \n"+ t.getMessage() );
    }finally{
        writeModel.getBaseModel().notifyEvent(new EditEvent(editorUri,false));
        lock.leaveCriticalSection();
    }
    
    if( entToReturnTo.size() >= 1 && entToReturnTo.get(0) != null){
        request.setAttribute("entityToReturnTo",
                entToReturnTo.get(0).trim().replaceAll("<","").replaceAll(">",""));
    }
%>

<jsp:forward page="postEditCleanUp.jsp"/>

<%!

    /* ********************************************************* */
    /* ******************** utility functions ****************** */
    /* ********************************************************* */

    public Map<String,List<String>> fieldsToAssertionMap( Map<String,Field> fields){
        Map<String,List<String>> out = new HashMap<String,List<String>>();
        for( String fieldName : fields.keySet()){
            Field field = fields.get(fieldName);

            List<String> copyOfN3 = new ArrayList<String>();
            for( String str : field.getAssertions()){
                copyOfN3.add(str);
            }
            out.put( fieldName, copyOfN3 );
        }
        return out;
    }

     public Map<String,List<String>> fieldsToRetractionMap( Map<String,Field> fields){
        Map<String,List<String>> out = new HashMap<String,List<String>>();
        for( String fieldName : fields.keySet()){
            Field field = fields.get(fieldName);

            List<String> copyOfN3 = new ArrayList<String>();
            for( String str : field.getRetractions()){
                copyOfN3.add(str);
            }
            out.put( fieldName, copyOfN3 );
        }
        return out;
    }

     
    /* ******************** Utility methods ********************** */
    
    public Map<String,String> newToUriMap(Map<String,String> newResources, WebappDaoFactory wdf){
        HashMap<String,String> newVarsToUris = new HashMap<String,String>();
        HashSet<String> newUris = new HashSet<String>();
        for( String key : newResources.keySet()){        	
            String prefix = newResources.get(key);
        	String uri = makeNewUri(prefix, wdf);
        	while( newUris.contains(uri) ){
        		uri = makeNewUri(prefix,wdf);
        	}
        	newVarsToUris.put(key,uri);
        	newUris.add(uri);
        }
         return newVarsToUris;
    }

    
    public String makeNewUri(String prefix, WebappDaoFactory wdf){
        if( prefix == null || prefix.length() == 0 ){
        	String uri = null;       
        	try{
        		uri = wdf.getIndividualDao().getUnusedURI(null);
            }catch(InsertException ex){
            	log.error("could not create uri");
            }        
			return uri;
        }
        
        String goodURI = null;
        int attempts = 0;
        while( goodURI == null && attempts < 30 ){            
            Individual ind = new IndividualImpl();
            ind.setURI( prefix + random.nextInt() );
            try{
        		goodURI = wdf.getIndividualDao().getUnusedURI(ind);
            }catch(InsertException ex){
            	log.debug("could not create uri");
            }
            attempts++;
        }        
        if( goodURI == null )
        	log.error("could not create uri for prefix " + prefix);
        return goodURI;
    }

    static Random random = new Random();
    
    //we should get this from the application
    static String defaultUriPrefix = "http://vivo.library.cornell.edu/ns/0.1#individual";    
    public static final String baseDirectoryForFiles = "/usr/local/vitrofiles";
    
    private static Property RDF_TYPE = ResourceFactory.createProperty(VitroVocabulary.RDF_TYPE);
    private static Property RDFS_LABEL = ResourceFactory.createProperty(VitroVocabulary.RDFS+"label");
    
    Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.edit.processRdfForm2.jsp");
%>

<%!      
    /* What are the posibilities and what do they mean?
     field is a Uri:
      orgValue  formValue
      null      null       Optional object property, maybe a un-filled out checkbox or radio button.
      non-null  null       There was an object property and it was unset on the form
      null      non-null   There was an objProp that was not set and is now set.
      non-null  non-null    If they are the same then there was no edit, if the differ then form field was changed

      field is a Literal:
      orgValue  formValue
      null      null      Optional value that was not set.
      non-null  null      Optional value that was unset on form
      null      non-null  Optional value that was unset but was set on form
      non-null  non-null  If same, there was no edit, if different, there was a change to the form field.

      What about checkboxes?
    */
    private boolean hasFieldChanged(String fieldName, EditConfiguration editConfig, EditSubmission submission) {
        String orgValue = editConfig.getUrisInScope().get(fieldName);
        String newValue = submission.getUrisFromForm().get(fieldName);
               
        // see possibilities list in comments just above
        if (orgValue == null && newValue != null) {
            log.debug("Note: Setting previously null object property for field '"+fieldName+"' to new value ["+newValue+"]");
            return true;
        }

        if( orgValue != null && newValue != null){
            if( orgValue.equals(newValue))
              return false;
            else
              return true;
        }
       
        //This does NOT use the semantics of the literal's Datatype or the language.
        Literal orgLit = editConfig.getLiteralsInScope().get(fieldName);
        Literal newLit = submission.getLiteralsFromForm().get(fieldName);
        
        if( orgLit != null ) {
            orgValue = orgLit.getValue().toString();
        } 
        if( newLit != null ) {
            newValue = newLit.getValue().toString();
        }
        
        // added for links, where linkDisplayRank will frequently come in null
        if (orgValue == null && newValue != null) {
            return true;
        }
        
        if( orgValue != null && newValue != null ){
            if( orgValue.equals(newValue)) {
            	return false;
            }
                
            else {
                return true;
            }
        }
        //value wasn't set originally because the field is optional
        return false;
    }

    private boolean logAddRetract(String msg, Map<String,List<String>>add, Map<String,List<String>>retract){
        log.debug(msg);
        if( add != null ) log.debug( "assertions: " + add.toString() );
        if( retract != null ) log.debug( "retractions: " +  retract.toString() );
        return true;
    }
  
    private boolean logRequiredOpt(String msg, List<String>required, List<String>optional){
        log.debug(msg);
        if( required != null ) log.debug( "required: " + required.toString() );
        if( optional != null ) log.debug( "optional: " +  optional.toString() );
        return true;
    }
    
    private void dump(String name, Object fff){
        XStream xstream = new XStream(new DomDriver());
        System.out.println( "*******************************************************************" );
        System.out.println( name );
        System.out.println(xstream.toXML( fff ));
    }    
%>
