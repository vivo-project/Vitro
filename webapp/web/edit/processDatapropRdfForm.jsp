<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.*" %>
<%@ page import="com.hp.hpl.jena.ontology.OntModel" %>
<%@ page import="com.hp.hpl.jena.shared.Lock" %>
<%@ page import="com.thoughtworks.xstream.XStream" %>
<%@ page import="com.thoughtworks.xstream.io.xml.DomDriver" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.EditLiteral" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditN3Generator" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.Field" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="java.io.StringReader" %>
<%@ page import="java.util.*" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.RdfLiteralHash"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.beans.DataProperty"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.RoleIdentifier"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditN3Utils"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<%-- 2nd prototype of processing, adapted for data property editing

This one takes one list of n3 for a single data property statement field,
and there will be no optional fields.  If the variables in the required n3
are not bound or it cannot be processed as n3 by Jena then it is an error
in processing the form.

Handling back button submissions:
As of 2008-08 this code can handle a single back button submission.  Deeper
back button submissions are handled by just creating the requested property
with the requested literal value and setting a request attribute to indicate
"back button confusion"  It is painful to the user to just give them an error
page since they might have just spent 5 min. typing in a value.

When an data property edit submission is POSTed it includes the subjectURI,
the predicateURI, the new Literal and a hash of the Literal to be replaced.
When a client POSTs a form that they received after several other POSTs 
involving the same data property and Literal, there is a chance that the 
data property statement to be replaced, with the Literal identified by the hash
in the POST, is no longer in the model.  

Current stop gap solution:
If we cannot find the data property statement to replace:
If the data property is functional then just delete the single existing statement
and replace with he new one.
Otherwise, just add the new data property statement, don't remove any statements
and set a flag in the request to indicate "back button confusion"
 
--%>
<%! 
    final Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.processDatapropRdfForm.jsp");
%>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousPages" %>
<% request.setAttribute("requestedActions", new UseMiscellaneousPages()); %>
<vitro:confirmAuthorization />

<%    
    log.debug("Starting processDatapropRdfForm.jsp");

    List<String> errorMessages = new ArrayList<String>();
    
    //Object sessionOntModel = request.getSession().getAttribute("jenaOntModel");
    //OntModel jenaOntModel = (sessionOntModel != null && sessionOntModel instanceof OntModel) ? (OntModel)sessionOntModel: 
    //  (OntModel)application.getAttribute("jenaOntModel");
        
    VitroRequest vreq = new VitroRequest(request);
    EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,vreq);
    if( editConfig == null ){
        %><jsp:forward page="/edit/messages/noEditConfigFound.jsp"/><%
        return;
    }
    
    EditSubmission submission = new EditSubmission(vreq.getParameterMap(), editConfig);  
    
    Map<String,String> errors = submission.getValidationErrors();
    EditSubmission.putEditSubmissionInSession(session,submission);   

    if( errors != null && ! errors.isEmpty() ){
        String form = editConfig.getFormUrl();
        vreq.setAttribute("formUrl", form);
        %><jsp:forward page="${formUrl}"/><%
        return;
    }

    OntModel queryModel = editConfig.getQueryModelSelector().getModel(request,application);
    OntModel resourcesModel = editConfig.getResourceModelSelector().getModel(request,application);
    
    EditN3Generator n3Subber = editConfig.getN3Generator();
    List<String> n3Required = editConfig.getN3Required();

    Map<String,List<String>> fieldAssertions = null;
    String subjectUri=null, predicateUri=null;
    Individual subject=null;
    if( editConfig.getDatapropKey() != null && editConfig.getDatapropKey().length() > 0){
        // we are editing an existing data property statement
        subjectUri   = editConfig.getSubjectUri();
        if (subjectUri == null || subjectUri.trim().length()==0) {
            log.error("No subjectUri parameter available via editConfig for datapropKey "+editConfig.getDatapropKey());
            throw new Error("No subjectUri parameter available via editConfig in processDatapropRdfForm.jsp");
        }
        predicateUri = editConfig.getPredicateUri();
        if (predicateUri == null || predicateUri.trim().length()==0) {
            log.error("No predicateUri parameter available via editConfig for datapropKey "+editConfig.getDatapropKey());
            throw new Error("No predicateUri parameter available via editConfig in processDatapropRdfForm.jsp");
        }
        
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();

        // need to get subject because have to iterate through all its data property statements to match datapropKey hashcode
        subject = wdf.getIndividualDao().getIndividualByURI(subjectUri);
        if( subject == null ) {
            log.error("Could not find subject Individual via editConfig's subjectUri while proceessing update to datapropKey "+editConfig.getDatapropKey());
            throw new Error("In processDatapropRdfForm.jsp, could not find subject Individual via uri " + subjectUri);
        }
        
        boolean backButtonProblems = checkForBackButtonConfusion(vreq, application, submission, editConfig, subject, wdf);
        if( backButtonProblems ){
            %><jsp:forward page="/edit/messages/datapropertyBackButtonProblems.jsp"/><%
            return;
        }
        
        fieldAssertions = fieldsToMap(editConfig.getFields());
    }
    
    /* ********** URIs and Literals on Form/Parameters *********** */
    //sub in resource uris off form
    n3Required = n3Subber.subInUris(submission.getUrisFromForm(), n3Required);

    //sub in literals from form
    n3Required = n3Subber.subInLiterals(submission.getLiteralsFromForm(), n3Required);

    fieldAssertions = n3Subber.substituteIntoValues(submission.getUrisFromForm(), submission.getLiteralsFromForm(), fieldAssertions );

    /* ****************** URIs and Literals in Scope ************** */
    n3Required = n3Subber.subInUris(  editConfig.getUrisInScope(), n3Required);

    n3Required = n3Subber.subInLiterals( editConfig.getLiteralsInScope(), n3Required);

    fieldAssertions = n3Subber.substituteIntoValues(editConfig.getUrisInScope(),editConfig.getLiteralsInScope(), fieldAssertions );

    /* ****************** New Resources ********************** */
    Map<String,String> varToNewResource = newToUriMap(editConfig.getNewResources(),resourcesModel);

    //if we are editing an existing prop, no new resources will be substituted since the var will
    //have already been substituted in by urisInScope.
    n3Required = n3Subber.subInUris( varToNewResource, n3Required);

    fieldAssertions = n3Subber.substituteIntoValues(varToNewResource, null, fieldAssertions );

    /* ***************** Build Models ******************* */
    /* bdc34: we should check if this is an edit of an existing
    or a new individual.  If this is a edit of an existing then
    we don't need to do the n3required or the n3optional; only the
    the assertions and retractions from the fields are needed.
     */
    List<Model> requiredAssertions  = null;
    List<Model> requiredRetractions = null;

    boolean submissionWasAnUpdate = false;
    if( editConfig.getDatapropKey() != null && editConfig.getDatapropKey().trim().length() > 0 ){
        //editing an existing statement
        submissionWasAnUpdate = true;
        
        List<Model> requiredFieldAssertions  = new ArrayList<Model>();
        List<Model> requiredFieldRetractions = new ArrayList<Model>();
        for(String fieldName: fieldAssertions.keySet()){
            Field field = editConfig.getFields().get(fieldName);
            /* CHECK that field changed, then add assertions and retractions */
            if( hasFieldChanged(fieldName, editConfig, submission) ){
                log.debug("Field "+fieldName+" has changed for datapropKey "+editConfig.getDatapropKey());                
                
                List<String> assertions = fieldAssertions.get(fieldName);
                for( String n3 : assertions){
                    try{
                        log.debug("Adding assertion '"+n3+"' to requiredFieldAssertions");
                        Model model = ModelFactory.createDefaultModel();
                        StringReader reader = new StringReader(n3);
                        model.read(reader, "", "N3");
                        requiredFieldAssertions.add(model);
                    }catch(Throwable t){
                        log.warn("processing N3 assertions string from field "+fieldName+"\n"+t.getMessage()+'\n'+"n3: \n"+n3);
                        errorMessages.add("error processing N3 assertion string from field " + fieldName + "\n"+
                                t.getMessage() + '\n' +
                                "n3: \n" + n3 );
                    }
                }
                if (field.getRetractions()!=null) {
                    for( String n3 : field.getRetractions()){
                        try{
                            log.debug("Adding retraction '"+n3+"' to requiredFieldRetractions");
                            Model model = ModelFactory.createDefaultModel();
                            StringReader reader = new StringReader(n3);
                            model.read(reader, "", "N3");
                            requiredFieldRetractions.add(model);
                        }catch(Throwable t){
                            log.warn("processing N3 retraction string from field "+fieldName+"\n"+t.getMessage()+'\n'+"n3: \n"+n3);
                            errorMessages.add("error in processDatapropRdfForm.jsp processing N3 retraction string from field "+fieldName+"\n"+t.getMessage()+'\n'+"n3: \n"+n3);
                        }
                    }
                }
                if(checkForEmptyString(submission, editConfig)){
                    //don't assert the empty string dataproperty
                    requiredFieldAssertions.clear();
                }
            }
        }
        requiredAssertions = requiredFieldAssertions;
        requiredRetractions = requiredFieldRetractions;

    } else { //deal with required N3
        submissionWasAnUpdate = false;
        log.debug("Not editing an existing statement since no datapropKey in editConfig");
                
        List<Model> requiredNewModels = new ArrayList<Model>();
        for(String n3 : n3Required){
            try{
                log.debug("Adding assertion '"+n3+"' to requiredNewModels");
                Model model = ModelFactory.createDefaultModel();
                StringReader reader = new StringReader(n3);
                model.read(reader, "", "N3");
                requiredNewModels.add( model );
            }catch(Throwable t){
                log.warn("error processing required n3 string \n"+t.getMessage()+'\n'+"n3: \n"+n3);
                errorMessages.add("error processing required n3 string \n"+t.getMessage()+'\n'+"n3: \n"+n3);
            }
        }
        
        requiredRetractions = Collections.EMPTY_LIST;
        if( !checkForEmptyString(submission, editConfig) ){
            requiredAssertions = requiredNewModels;
            
            if( !errorMessages.isEmpty() ){
                for( String error : errorMessages){
                    log.debug(error);
                }                
            }            
        }else{
            //doing an empty field delete, see Issue VITRO-432  
            requiredAssertions = Collections.EMPTY_LIST;
        }
    }    
    
    OntModel writeModel = editConfig.getWriteModelSelector().getModel(request,application);
    Lock lock = null;
    String editorUri = EditN3Utils.getEditorUri(request);    
    try{
        lock =  writeModel.getLock();
        lock.enterCriticalSection(Lock.WRITE);
        writeModel.getBaseModel().notifyEvent(new EditEvent(editorUri,true));
        for( Model model : requiredAssertions) {
            writeModel.add(model);
        }
        for(Model model : requiredRetractions ){
            writeModel.remove( model );
        }
    }catch(Throwable t){
        errorMessages.add("In processDatapropRdfForm.jsp, error adding edit change n3required model to in memory model \n"+ t.getMessage() );
    }finally{
        writeModel.getBaseModel().notifyEvent(new EditEvent(editorUri,false));
        lock.leaveCriticalSection();
    }
    
    
    //now setup an EditConfiguration so a single back button submissions can be handled
    EditConfiguration copy = editConfig.copy();
    
    //need a new DataPropHash and a new editConfig that uses that, and replace 
    //the editConfig used for this submission in the session.  The same thing
    //is done for an update or a new insert since it will convert the insert
    //EditConfig into an update EditConfig.
    log.debug("attempting to make an updated copy of the editConfig for browser back button support");
    Field dataField = copy.getField(copy.getVarNameForObject());
     
    DataPropertyStatement dps = new DataPropertyStatementImpl();
    Literal submitted = submission.getLiteralsFromForm().get(copy.getVarNameForObject());
    if( submitted != null ){
    	dps.setIndividualURI( copy.getSubjectUri() );
    	dps.setDatapropURI( copy.getPredicateUri() );
    	dps.setDatatypeURI( submitted.getDatatypeURI());
    	dps.setLanguage( submitted.getLanguage() );
    	dps.setData( submitted.getLexicalForm() );
       
    	copy.prepareForDataPropUpdate(writeModel, dps);
    	copy.setDatapropKey( Integer.toString(RdfLiteralHash.makeRdfLiteralHash(dps)) );
    	EditConfiguration.putConfigInSession(copy,session);
    }
%>

<jsp:forward page="postEditCleanUp.jsp"/>

<%!/* ********************************************************* */
    /* ******************** utility functions ****************** */
    /* ********************************************************* */

    public Map<String, List<String>> fieldsToMap(Map<String, Field> fields) {
        Map<String, List<String>> out = new HashMap<String, List<String>>();
        for (String fieldName : fields.keySet()) {
            Field field = fields.get(fieldName);

            List<String> copyOfN3 = new ArrayList<String>();
            for (String str : field.getAssertions()) {
                copyOfN3.add(str);
            }
            out.put(fieldName, copyOfN3);
        }
        return out;
    }

    public Map<String, String> newToUriMap(Map<String, String> newResources,
            Model model) {
        HashMap<String, String> newUris = new HashMap<String, String>();
        for (String key : newResources.keySet()) {
            newUris.put(key, makeNewUri(newResources.get(key), model));
        }
        return newUris;
    }

    public String makeNewUri(String prefix, Model model) {
        if (prefix == null || prefix.length() == 0)
            prefix = defaultUriPrefix;

        String uri = prefix + random.nextInt();
        Resource r = ResourceFactory.createResource(uri);
        while (model.containsResource(r)) {
            uri = prefix + random.nextInt();
            r = ResourceFactory.createResource(uri);
        }
        return uri;
    }

    static Random random = new Random();

    static String defaultUriPrefix = "http://vivo.library.cornell.edu/ns/0.1#individual";%>


<%!private boolean hasFieldChanged(String fieldName,
            EditConfiguration editConfig, EditSubmission submission) {
        String orgValue = editConfig.getUrisInScope().get(fieldName);
        String newValue = submission.getUrisFromForm().get(fieldName);
        if (orgValue != null && newValue != null) {
            if (orgValue.equals(newValue))
                return false;
            else
                return true;
        }

        Literal orgLit = editConfig.getLiteralsInScope().get(fieldName);
        Literal newLit = submission.getLiteralsFromForm().get(fieldName);
        boolean fieldChanged = !EditLiteral.equalLiterals(orgLit, newLit);
        log.debug("field " + fieldName + " "
                + (fieldChanged ? "did Change" : "did NOT change"));
        return fieldChanged;
    }

    private boolean checkForBackButtonConfusion(VitroRequest vreq, ServletContext application, EditSubmission submission,
            EditConfiguration editConfig, Individual subject,
            WebappDaoFactory wdf) {
        if (editConfig.getDatapropKey() == null
                || editConfig.getDatapropKey().length() == 0)
            return false;
        
        Model model = (Model)application.getAttribute("jenaOntModel");
        int dpropHash = Integer.parseInt(editConfig.getDatapropKey());
        DataPropertyStatement dps = RdfLiteralHash.getPropertyStmtByHash(subject, editConfig.getPredicateUri(), dpropHash, model);

        if (dps != null)
            return false;
        DataProperty dp = wdf.getDataPropertyDao().getDataPropertyByURI(
                editConfig.getPredicateUri());
        if (dp != null) {
            if (dp.getDisplayLimit() == 1 /* || dp.isFunctional() */)
                return false;
            else
                return true;
        }
        return false;

    }

    // Our editors have gotten into the habbit of clearing the text from the
    // textarea and saving it to invoke a delete.  see Issue VITRO-432   
    private boolean checkForEmptyString(EditSubmission submission,
            EditConfiguration editConfig) {
        if (editConfig.getFields().size() == 1) {
            String onlyField = editConfig.getFields().keySet().iterator()
                    .next();
            Literal value = submission.getLiteralsFromForm().get(onlyField);
            if( value == null ){
            	log.debug("No parameters found in submission for field \"" + onlyField +"\"");
            	return true;
            }else if( "".equals(value.getLexicalForm())) {
                log.debug("Submission was a single field named \"" + onlyField + "\" with an empty string");
                return true;
            }
        }
        return false;
    }
    

    private void dump(String name, Object fff) {
        XStream xstream = new XStream(new DomDriver());
        Log log = LogFactory
                .getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.forms.processDatapropRdfForm.jsp");
        log
                .debug("*******************************************************************");
        log.debug(name);
        log.debug(xstream.toXML(fff));
    }%>
