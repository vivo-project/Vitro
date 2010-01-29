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
<%@ page import="java.util.List" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>
<%! 
public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.forms.defaultLinkForm.jsp");
%>
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
      PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>
      SELECT ?urlExisting
      WHERE { ?link vitro:linkURL ?urlExisting }
</v:jsonset>
<%--  Pair the "existing" query with the skeleton of what will be asserted for a new statement involving this field.
      The actual assertion inserted in the model will be created via string substitution into the ? variables.
      NOTE the pattern of punctuation (a period after the prefix URI and after the ?field) --%> 
<v:jsonset var="urlAssertion" >
      @prefix vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> .
      ?link vitro:linkURL ?url .
</v:jsonset>

<v:jsonset var="anchorExisting" >
      PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>
      SELECT ?anchorExisting
      WHERE { ?link vitro:linkAnchor ?anchorExisting }
</v:jsonset>
<v:jsonset var="anchorAssertion" >
      @prefix vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> .
      ?link vitro:linkAnchor ?anchor .
</v:jsonset>

<%--  When not retrieving a literal via a datatype property, put the SPARQL statement into
      the SparqlForExistingUris --%>

<v:jsonset var="n3ForEdit">
      @prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
      @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
      @prefix vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> .

      ?subject vitro:additionalLink  ?link .

      ?link rdf:type vitro:Link .

      ?link
          vitro:linkURL         ?url ;
          vitro:linkAnchor      ?anchor .
          
</v:jsonset>

<v:jsonset var="n3Optional">
      @prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
    
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
    "newResources"      : { "link" : "http://vivo.library.cornell.edu/ns/0.1#individual" },
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

    String submitLabel=""; // don't put local variables into the request
    /* title is used by pre and post form fragments */
    if (objectUri != null) {
    	request.setAttribute("title", "Edit link for " + subject.getName());
        submitLabel = "Save changes";
    } else {
        request.setAttribute("title","Create a new link for " + subject.getName());
        submitLabel = "Create new link";
    }

%>

<jsp:include page="${preForm}"/>

<h2>${title}</h2>
<form action="<c:url value="/edit/processRdfForm2.jsp"/>" >
    <v:input type="text" label="URL" id="url" size="70"/>
    <v:input type="text" label="label" id="anchor" size="60"/>
    <v:input type="submit" id="submit" value="<%=submitLabel%>" cancel="${param.subjectUri}"/>
</form>

<jsp:include page="${postForm}"/>
