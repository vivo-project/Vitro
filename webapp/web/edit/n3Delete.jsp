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
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page import="java.io.StringReader" %>
<%@ page import="java.util.*" %>
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
<%@page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.jena.DependentResourceDeleteJena"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<%-- N3 based deletion.
 
 Build up the n3 using the fields from an edit configuration and then remove
 all of those statements from the systems model.  In general this should
 do the same thing as an update with processRdfForm2.jsp but it should just
 build the assertions graph and remove that from the system model.
 
--%>
<%    
    if( session == null)
        throw new Error("need to have session");
    boolean selfEditing = VitroRequestPrep.isSelfEditing(request);
    if (!selfEditing && !LoginFormBean.loggedIn(request, LoginFormBean.NON_EDITOR)) {
        %><c:redirect url="<%= Controllers.LOGIN %>" /><%
    }
    
    /* the post parameters seem to get consumed by the parsing so
     * we have to make a copy. */
    Map <String,List<String>> queryParameters = null;        
    queryParameters = request.getParameterMap();        
    
    List<String>  errorMessages = new ArrayList<String>();    

    EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,request,queryParameters);
    if( editConfig == null ){
        %><jsp:forward page="/edit/messages/noEditConfigFound.jsp"/><%
    }
    EditN3Generator n3Subber = editConfig.getN3Generator();
    EditSubmission submission = new EditSubmission(queryParameters, editConfig);     

    Map<String,String> errors =  submission.getValidationErrors();
    EditSubmission.putEditSubmissionInSession(session,submission);

    if(  errors != null && ! errors.isEmpty() ){
        String form = editConfig.getFormUrl();
        request.setAttribute("formUrl", form);
        %><jsp:forward page="${formUrl}"/><%
        return;
    }

    List<Model> requiredAssertionsToDelete  = new ArrayList<Model>();    
    List<Model> optionalAssertionsToDelete  = new ArrayList<Model>();
    
    boolean requestIsAnValidDelete =  editConfig.getObject() != null && editConfig.getObject().trim().length() > 0; 
    if( requestIsAnValidDelete ){
        if( log.isDebugEnabled()) log.debug("deleting using n3Delete.jsp; resource: " + editConfig.getObject() );
                
        List<String> n3Required = editConfig.getN3Required();
        List<String> n3Optional = editConfig.getN3Optional();
        
        /* ****************** URIs and Literals in Scope ************** */        
        n3Required = subInUris( editConfig.getUrisInScope(), n3Required);
        n3Optional = subInUris( editConfig.getUrisInScope(), n3Optional);
        if(log.isDebugEnabled()) logRequuiredOpt("subsititued in URIs from scope ",n3Required,n3Optional);
        
        n3Required = n3Subber.subInLiterals( editConfig.getLiteralsInScope(), n3Required);
        n3Optional = n3Subber.subInLiterals( editConfig.getLiteralsInScope(), n3Optional);
        if(log.isDebugEnabled()) logRequuiredOpt("subsititued in Literals from scope ",n3Required,n3Optional);
        
        //no new resources
        
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
        requiredAssertionsToDelete.addAll(requiredNewModels);        

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
        optionalAssertionsToDelete.addAll(optionalNewModels);                
        
        //////////////// from update of processRdfForm2.jsp /////////////

        Map<String,List<String>> fieldAssertions = null;
        if( editConfig.getObject() != null && editConfig.getObject().length() > 0){
            fieldAssertions = fieldsToAssertionMap(editConfig.getFields());
        }else{
            fieldAssertions = new HashMap<String,List<String>>();
        }

        Map<String,List<String>> fieldRetractions = null;
        if( editConfig.getObject() != null && editConfig.getObject().length() > 0){
            fieldRetractions = fieldsToRetractionMap(editConfig.getFields());
        }else{
            fieldRetractions = new HashMap<String,List<String>>();
        }
        
        /* ********** URIs and Literals on Form/Parameters *********** */
        fieldAssertions = n3Subber.substituteIntoValues(  submission.getUrisFromForm(), submission.getLiteralsFromForm(), fieldAssertions);
        if(log.isDebugEnabled()) logAddRetract("subsititued in literals from form",fieldAssertions,fieldRetractions);
        //fieldRetractions does NOT get values from form.
        
        /* ****************** URIs and Literals in Scope ************** */
        fieldAssertions = n3Subber.substituteIntoValues(editConfig.getUrisInScope(), editConfig.getLiteralsInScope(), fieldAssertions );
        fieldRetractions = n3Subber.substituteIntoValues(editConfig.getUrisInScope(), editConfig.getLiteralsInScope(), fieldRetractions);
        if(log.isDebugEnabled()) logAddRetract("subsititued in URIs and Literals from scope",fieldAssertions,fieldRetractions);
        
        //editing an existing statement
        List<Model> requiredFieldAssertions  = new ArrayList<Model>();
        List<Model> requiredFieldRetractions = new ArrayList<Model>();
        for(String fieldName: fieldAssertions.keySet()){
            Field field = editConfig.getFields().get(fieldName);

            /* For the delete don't check that field changed, just build assertions */            
            log.debug("making delete graph for field " + fieldName );
            /* if the field was a checkbox then we need to something special, don't know what that is yet */

            List<String> assertions = fieldAssertions.get(fieldName);
            for( String n3 : assertions){
                 try{
                     Model model = ModelFactory.createDefaultModel();
                     StringReader reader = new StringReader(n3);
                     model.read(reader, "", "N3");
                     requiredFieldAssertions.add(model);
                 }catch(Throwable t){
                    errorMessages.add("error processing N3 assertion string from field " + fieldName + "\n"+
                             t.getMessage() + '\n' +
                             "n3: \n" + n3 );
                }
            }                            
        }
        requiredAssertionsToDelete.addAll( requiredFieldAssertions );                       
    } else {    
        throw new Error("No object specified, cannot do delete");
    }
    
    //The requiredNewModels and the optionalNewModels could be handled differently
    //but for now we'll just do them the same
    requiredAssertionsToDelete.addAll(optionalAssertionsToDelete);   
    Model allPossibleToDelete = ModelFactory.createDefaultModel();    
    for( Model model : requiredAssertionsToDelete ) {
        allPossibleToDelete.add( model );
    }    
    
    OntModel queryModel = editConfig.getQueryModelSelector().getModel(request,application);
    if( editConfig.isUseDependentResourceDelete() ){
    	Model depResRetractions = 
    		DependentResourceDeleteJena
    		.getDependentResourceDeleteForChange(null,allPossibleToDelete,queryModel);
    	allPossibleToDelete.add( depResRetractions );
    }    
    
    OntModel writeModel = editConfig.getWriteModelSelector().getModel(request,application);
    String editorUri = EditN3Utils.getEditorUri(request,session,application);    
    Lock lock = null;
    try{
        lock =  writeModel.getLock();
        lock.enterCriticalSection(Lock.WRITE);
        writeModel.getBaseModel().notifyEvent(new EditEvent(editorUri,true));        
        writeModel.remove(allPossibleToDelete);        
    }catch(Throwable t){
        errorMessages.add("error adding edit change n3required model to in memory model \n"+ t.getMessage() );
    }finally{
        writeModel.getBaseModel().notifyEvent(new EditEvent(editorUri,false));
        lock.leaveCriticalSection();
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
     
     
     //******************************************
     //Use N3Subber instead of these utility methods
     

    public Map<String,List<String>> substituteIntoValues(Map<String,String> varsToUris,
                                                      Map<String,String> varsToLiterals,
                                                      Map<String,List<String>> namesToN3 ){
        Map<String,List<String>> outHash = new HashMap<String,List<String>>();
        for(String fieldName : namesToN3.keySet()){
            List<String> n3strings = namesToN3.get(fieldName);
            List<String> newList  = new ArrayList<String>();
            if( varsToUris != null)
                newList = subInUris(varsToUris, n3strings);
            if( varsToLiterals != null)
                newList = subInLiterals(varsToLiterals, newList);
            outHash.put(fieldName, newList);
        }
        return outHash;
    }

    
    public List<String> subInUris(Map<String,String> varsToVals, List<String> targets){
        if( varsToVals == null || varsToVals.isEmpty() ) return targets;
        ArrayList<String> outv = new ArrayList<String>();
        for( String target : targets){
            String temp = target;
            for( String key : varsToVals.keySet()) {
                temp = subInUris( key, varsToVals.get(key), temp)  ;
            }
            outv.add(temp);
        }
        return outv;
    }


    public String subInUris(String var, String value, String target){
        if( var == null || var.length() == 0 || value==null )
            return target;
        String varRegex = "\\?" + var;
        String out = target.replaceAll(varRegex,"<"+value+">");
        if( out != null && out.length() > 0 )
            return out;
        else
            return target;
    }

    
    public List<String>subInUris(String var, String value, List<String> targets){
        ArrayList<String> outv =new ArrayList<String>();
        for( String target : targets){
            outv.add( subInUris( var,value, target) ) ;
        }
        return outv;
    }

    
    public List<String> subInLiterals(Map<String,String> varsToVals, List<String> targets){
        if( varsToVals == null || varsToVals.isEmpty()) return targets;

        ArrayList<String> outv =new ArrayList<String>();
        for( String target : targets){
            String temp = target;
            for( String key : varsToVals.keySet()) {
                temp = subInLiterals( key, varsToVals.get(key), temp);
            }
            outv.add(temp);
        }
        return outv;
    }

    
    public List<String>subInLiterals(String var, String value, List<String> targets){
        ArrayList<String> outv =new ArrayList<String>();
        for( String target : targets){
            outv.add( subInLiterals( var,value, target) ) ;
        }
        return outv;
    }

    
    public String subInLiterals(String var, String value, String target){
        String varRegex = "\\?" + var;
        String out = target.replaceAll(varRegex,'"'+value+'"');  //*** THIS  NEEDS TO BE ESCAPED for N3 (thats python escaping)
        if( out != null && out.length() > 0 )
            return out    ;
        else
            return target;
    }

    
    public Map<String,String> newToUriMap(Map<String,String> newResources, Model model){
        HashMap<String,String> newUris = new HashMap<String,String>();
        for( String key : newResources.keySet()){
            newUris.put(key,makeNewUri(newResources.get(key), model));
        }
         return newUris;
    }

    
    public String makeNewUri(String prefix, Model model){
        if( prefix == null || prefix.length() == 0 )
            prefix = defaultUriPrefix;

        String uri = prefix + Math.abs( random.nextInt() );
        Resource r = ResourceFactory.createResource(uri);
        while( model.containsResource(r) ){
            uri = prefix + random.nextInt();
            r = ResourceFactory.createResource(uri);
        }
        return uri;
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
            if( orgValue.equals(newValue))
                return false;
            else
                return true;
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
  
    private boolean logRequuiredOpt(String msg, List<String>required, List<String>optional){
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
