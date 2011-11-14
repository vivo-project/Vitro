<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Literal" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.ModelChangePreprocessor"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.DefaultAddMissingIndividualFormModelPreprocessor"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>
<%
    Individual subject = (Individual)request.getAttribute("subject");
    ObjectProperty prop = (ObjectProperty)request.getAttribute("predicate");
    if (prop == null) throw new Error("no object property specified via incoming predicate attribute in defaultAddMissingIndividualForm.jsp");
    String propDomainPublic = (prop.getDomainPublic() == null) ? "affiliation" : prop.getDomainPublic();

    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    if( prop.getRangeVClassURI() == null ) {
        // If property has no explicit range, we will use e.g. owl:Thing.
        // Typically an allValuesFrom restriction will come into play later.
        VClass top = wdf.getVClassDao().getTopConcept();
        prop.setRangeVClassURI(top.getURI());
    }
    
    String objectUri = (String)request.getAttribute("objectUri");
    String predicateUri = (String)request.getAttribute("predicateUri");
    
    boolean isForwardToCreateNew = request.getAttribute("isForwardToCreateNew") != null &&
    (Boolean)request.getAttribute("isForwardToCreateNew") == true;
    
    // If this request is the first forward to this form we need to create a new
    // edit key because the request will be coming from a defaultObjPropForm and
    // will have an edit key that will be reused by the EditConfiguration that 
    // is being created below.  This also preserves back button functionality to
    // the form this request is coming from.  If the edit key was not cleared
    // the new editConfig below would overwrite the previous form's editConfig
    // in the Session.
    if( isForwardToCreateNew )     	    	
    	request.setAttribute("editKey", EditConfiguration.newEditKey(session));    
    
    //If a objectProperty is both provideSelect and offerCreateNewOption
    // then when the user gos to a default ObjectProperty edit for an existing
    // object property statement then the user can create a new Individual,
    // and replace the object in the existing objectPropertyStatement with
    // this new individual. 
    boolean isReplaceWithNew = request.getAttribute("isReplaceWithNew") != null &&
        (Boolean)request.getAttribute("isReplaceWithNew") == true;
    
    // If an objectProperty is selectFromExisitng==false and offerCreateNewOption == true
    // the we want to forward to the create new form but edit the existing object
    // of the objPropStmt.
    boolean isForwardToCreateButEdit = request.getAttribute("isForwardToCreateButEdit") != null
    &&  (Boolean)request.getAttribute("isForwardToCreateButEdit") == true;
        
    vreq.setAttribute("defaultNamespace",wdf.getDefaultNamespace());
    
    StringBuffer n3AssertedTypesUnescapedBuffer = new StringBuffer(); 
    
    // TODO: redo to query restrictions directly.  getVClassesForProperty returns the subclasses, which we don't want.
    
    //for ( VClass vclass : assertionsWdf.getVClassDao().getVClassesForProperty(subject.getVClassURI(),prop.getURI()) ) {
    //	if (vclass.getURI()!=null) {
    //		n3AssertedTypesUnescapedBuffer.append("?newIndividual ").append(" rdf:type <")
    //		.append(vclass.getURI()).append("> .\n");
    //	}
    //}
    vreq.setAttribute("n3AssertedTypesUnescaped",n3AssertedTypesUnescapedBuffer.toString());
    
    String flagURI = wdf.getVClassDao().getTopConcept().getURI(); 
    vreq.setAttribute("flagURI",flagURI);

    VClass rangeClass = null;
    String typeOfNew = (String)vreq.getAttribute("typeOfNew");
    if(typeOfNew != null )
    	rangeClass = wdf.getVClassDao().getVClassByURI( typeOfNew );
    if( rangeClass == null ){
    	rangeClass = wdf.getVClassDao().getVClassByURI(prop.getRangeVClassURI());
    	if( rangeClass == null ) throw new Error ("Cannot find class for range for property.  Looking for " + prop.getRangeVClassURI() );    
    }
	//vreq.setAttribute("rangeClassLocalName",rangeClass.getLocalName());
	//vreq.setAttribute("rangeClassNamespace",rangeClass.getNamespace());
    vreq.setAttribute("rangeClassUri",rangeClass.getURI());

    //todo: set additional ranges using allValuesFrom.

    //vreq.setAttribute("curatorReviewUri","http://vivo.library.cornell.edu/ns/0.1#CuratorReview");
%>

<v:jsonset var="queryForInverse" >
    PREFIX owl:  <http://www.w3.org/2002/07/owl#>
    SELECT ?inverse_property
    WHERE {
        ?inverse_property owl:inverseOf ?predicate
    }
</v:jsonset>

<%-- Enter a SPARQL query for each field, by convention concatenating the field id with "Existing"
     to convey that the expression is used to retrieve any existing value for the field in an existing individual.
     Each of these must then be referenced in the sparqlForExistingLiterals section of the JSON block below
     and in the literalsOnForm --%>
<v:jsonset var="nameExisting" >
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?existingName
    WHERE { ?newIndividual rdfs:label ?existingName }
</v:jsonset>

<%-- Pair the "existing" query with the skeleton of what will be asserted for a new statement involving this field.
     the actual assertion inserted in the model will be created via string substitution into the ? variables.
     NOTE the pattern of punctuation (a period after the prefix URI and after the ?field) --%>
<v:jsonset var="n3ForName"  >
    @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
    ?newIndividual rdfs:label ?name .
</v:jsonset>

<v:jsonset var="n3ForRelation"  >    
    ?subject ?predicate ?newIndividual .
</v:jsonset>

<v:jsonset var="n3ForType"  >
    @prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
    ?newIndividual rdf:type <${rangeClassUri}> .
</v:jsonset>

<v:jsonset var="n3ForFlag"  >
    @prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
    ?newIndividual rdf:type <${flagURI}> .
</v:jsonset>

<%-- using v:jsonset here so everything goes through the same JSON escaping --%>
<v:jsonset var="n3AssertedTypes">
	@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
	${n3AssertedTypesUnescaped}
</v:jsonset>

<v:jsonset var="n3Inverse" >
    ?newIndividual ?inverseProp ?subject .
</v:jsonset>

<%-- note that it's safer to have multiple distinct optional blocks so that a failure in one
     will not prevent correct sections from being inserted --%>

<c:set var="editjson" scope="request">
  {
    "formUrl" : "${formUrl}",
    "editKey" : "${editKey}",
    "urlPatternToReturnTo" : "/entity",

    "subject"   : [ "subject", "${subjectUriJson}" ],
    "predicate" : [ "predicate", "${predicateUriJson}" ],
    "object"    : [ "newIndividual", "${objectUriJson}", "URI" ],

    "n3required"                : [ "${n3ForName}" , "${n3ForRelation}", "${n3ForType}"],
    "n3optional"                : [  "${n3Inverse}", "${n3AssertedTypes}", "${n3ForFlag}"  ],
    "newResources" : {
        "newIndividual"         : "${defaultNamespace}individual"
    },
    "urisInScope"               : { },
    "literalsInScope"           : { },
    "urisOnForm"                : [  ],
    "literalsOnForm"            : [ "name" ],
    "filesOnForm"               : [ ],
    "sparqlForLiterals"         : {  },
    "sparqlForUris"             : { "inverseProp" : "${queryForInverse}" },
    "sparqlForExistingLiterals" : {
        "name"                  : "${nameExisting}"       
    },
    "sparqlForExistingUris"     : { },
    "fields" : {
        "name" : {
            "newResource"       : "false",
            "validators"        : ["nonempty"],
            "optionsType"       : "UNDEFINED",
            "literalOptions"    : [ ],
            "predicateUri"      : "",
            "objectClassUri"    : "",
            "rangeDatatypeUri"  : "",
            "rangeLang"         : "",
            "assertions"        : [ "${n3ForName}" ]
        }
     }
  }
</c:set>
<%
	EditConfiguration editConfig = null;
	if( ! isForwardToCreateNew )
    	editConfig = EditConfiguration.getConfigFromSession(session,request);
	//else
	//  don't want to get the editConfig because it was for the defaultObjPropForm
	
    if( editConfig == null ){
        editConfig = new EditConfiguration((String)request.getAttribute("editjson"));
        EditConfiguration.putConfigInSession(editConfig, session);
    }

    Model model =  (Model)application.getAttribute("jenaOntModel");
    
    if( isReplaceWithNew){
        // this is very specialized for the defaultAddMissingIndividual.jsp 
        // to support objPropStmt edits where the user wants to 
        // create a new Individual.  Here we trick the processRdfForm2 into
        // creating a new Individual and use the preprocessor to do the 
        // removal of the old objPropStmt                
        editConfig.addModelChangePreprocessor( 
                new DefaultAddMissingIndividualFormModelPreprocessor(
                        subject.getURI(),predicateUri ,objectUri ) );
        editConfig.setObject(null);
        editConfig.prepareForNonUpdate(model);
    }else if ( isForwardToCreateButEdit) {
        editConfig.prepareForObjPropUpdate(model);                
    }else if(request.getAttribute("object") != null ){
        editConfig.prepareForObjPropUpdate(model);
    } else{
        editConfig.prepareForNonUpdate(model);
    }

    String submitButtonLabel="";
    /* title is used by pre and post form fragments */
    //set title to Edit to maintain functionality from 1.1.1 and avoid updates to Selenium tests
    request.setAttribute("title", "Edit");
    if (objectUri != null) {
        request.setAttribute("formTitle", "Edit \""+propDomainPublic+"\" entry for " + subject.getName());
        submitButtonLabel = "Save changes";
    } else {
        request.setAttribute("formTitle","Create \""+propDomainPublic+"\" entry for " + subject.getName());
        submitButtonLabel = "Create \""+propDomainPublic+"\" entry";
    }

%>

<jsp:include page="${preForm}">
    <jsp:param name="useTinyMCE" value="false"/>
    <jsp:param name="useAutoComplete" value="false"/>
</jsp:include>

<h2>${formTitle}</h2>
<h1>JSP form, must be removed for the 1.4!</h1>
<form action="<c:url value="/edit/processRdfForm2.jsp"/>" ><br/>
    <v:input type="text" label="name (required)" id="name" size="30"/><br/>    
    <v:input type="submit" id="submit" value="<%=submitButtonLabel%>" cancel="true"/>
</form>

<jsp:include page="${postForm}"/>
