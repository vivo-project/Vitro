<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.Literal" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary" %>

<%@ page import="java.util.List" %>

<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>
<%!  
    public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.forms.defaultLinkForm.jsp");
%>

<c:set var="vitroUri" value="<%= VitroVocabulary.vitroURI %>" />
<c:set var="rdfUri" value="<%= VitroVocabulary.RDF %>" />
<c:set var="rdfsUri" value="<%= VitroVocabulary.RDFS %>" />

<%--  Enter here any class names to be used for constructing INDIVIDUALS_VIA_VCLASS pick lists
      These are then referenced in the field's ObjectClassUri but not elsewhere.
      NOTE that this class may not exist in the model, in which the only choice of type
      that will show up is "web page", which will insert no new statements and just create
      links of type vitro:Link --%>

<%--  Then enter a SPARQL query for each field, by convention concatenating the field id with "Existing"
      to convey that the expression is used to retrieve any existing value for the field in an existing individual.
      Each of these must then be referenced in the sparqlForExistingLiterals section of the JSON block below
      and in the literalsOnForm --%>
<v:jsonset var="urlExisting" >
      SELECT ?urlExisting
      WHERE { ?link <${predicateUri}> ?urlExisting }
</v:jsonset>
<%--  Pair the "existing" query with the skeleton of what will be asserted for a new statement involving this field.
      The actual assertion inserted in the model will be created via string substitution into the ? variables.
      NOTE the pattern of punctuation (a period after the prefix URI and after the ?field) --%> 
<v:jsonset var="urlAssertion" >
      ?link <${predicateUri}> ?url .
</v:jsonset>

<v:jsonset var="anchorExisting" >
      PREFIX vitro: <${vitroUri}>
      SELECT ?anchorExisting
      WHERE { ?link vitro:linkAnchor ?anchorExisting }
</v:jsonset>
<v:jsonset var="anchorAssertion" >
      @prefix vitro: <${vitroUri}> .
      ?link vitro:linkAnchor ?anchor .
</v:jsonset>

<%--  When not retrieving a literal via a datatype property, put the SPARQL statement into
      the SparqlForExistingUris --%>

<v:jsonset var="n3ForEdit">
      @prefix rdf:  <${rdfUri}> .
      @prefix vitro: <${vitroUri}> .

      ?subject <${predicateUri}> ?link .

      ?link rdf:type vitro:Link .

      ?link
          vitro:linkURL         ?url ;
          vitro:linkAnchor      ?anchor .
          
</v:jsonset>

<v:jsonset var="n3Optional">
      @prefix rdf:  <${rdfUri}> .
    
      ?link rdf:type ?type .
</v:jsonset>

<c:set var="editjson" scope="request">
  {
    "formUrl" : "${formUrl}",
    "editKey" : "${editKey}",
    "urlPatternToReturnTo" : "/entity",
    
    "subject"   : ["subject",    "${subjectUriJson}" ],
    "predicate" : ["predicate", "${predicateUriJson}" ],
    "object"    : ["link", "${objectUriJson}", "URI" ],
    
    "n3required"        : [ "${n3ForEdit}" ],
    "n3optional"        : [ "${n3Optional}" ],
    "newResources"      : { },
    "urisInScope"       : { },
    "literalsInScope"   : { },
    "urisOnForm"        : [ ],
    "literalsOnForm"    : [ "url", "anchor" ],
    "filesOnForm"       : [ ],
    "sparqlForLiterals" : { },
    "sparqlForUris"     : { },
    "sparqlForExistingLiterals" : {
        "url"         : "${urlExisting}",
        "anchor"      : "${anchorExisting}"
    },
    "sparqlForExistingUris" : { },
    "fields" : {
      "url" : {
         "newResource"      : "false",
         "validators"       : [ "nonempty" ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "",
         "rangeLang"        : "",
         "assertions"       : [ "${urlAssertion}" ]
      },
      "anchor" : {
         "newResource"      : "false",
         "validators"       : [ "nonempty" ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "",
         "rangeLang"        : "",
         "assertions"       : [ "${anchorAssertion}" ]
      }
    }
  }
</c:set>
<%
    log.debug(request.getAttribute("editjson"));
    
    EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,request);
    if( editConfig == null ){
        editConfig = new EditConfiguration((String)request.getAttribute("editjson"));
        EditConfiguration.putConfigInSession(editConfig, session);
    }
    

    Model model =  (Model)application.getAttribute("jenaOntModel");
    String objectUri = (String)request.getAttribute("objectUri");    
    if( objectUri != null ){        
        editConfig.prepareForObjPropUpdate(model);            
    }else{
        editConfig.prepareForNonUpdate(model);
    }

    /* get some data to make the form more useful */
    Individual subject = (Individual)request.getAttribute("subject");

    String submitLabel=""; 
    String title = "";
    if (objectUri != null) {
    	title = "Edit <em>url</em> for " + subject.getName();
        submitLabel = "Save changes";
    } else {
        title = "Create a new <em>url</em> for " + subject.getName();
        submitLabel = "Create new url";
    }

%>

<jsp:include page="${preForm}"/>

<h2><%= title %></h2>
<form action="<c:url value="/edit/processRdfForm2.jsp"/>" >
    <v:input type="text" label="URL" id="url" size="70"/>
    <v:input type="text" label="Anchor text" id="anchor" size="60"/><br />
    <v:input type="submit" id="submit" value="<%=submitLabel%>" cancel="${param.subjectUri}"/>
</form>

<jsp:include page="${postForm}"/>
