<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>

<%@ page import="com.hp.hpl.jena.rdf.model.Literal"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@ page import="com.hp.hpl.jena.vocabulary.XSD" %>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.JavaScript" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Css" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.elements.DateTimeWithPrecision"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.Field"%>

<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.DateTimeIntervalValidation"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>

<%!
    public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.forms.dateTimeValueForm");
%>
<%
    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    vreq.setAttribute("defaultNamespace", ""); //empty string triggers default new URI behavior
%>

<c:set var="vivoCore" value="http://vivoweb.org/ontology/core#" />
<c:set var="type" value="<%= VitroVocabulary.RDF_TYPE %>" />
<c:set var="rdfs" value="<%= VitroVocabulary.RDFS %>" />
<c:set var="label" value="${rdfs}label" />

<c:set var="toDateTimeValue" value="${vivoCore}dateTimeValue"/>
<c:set var="valueType" value="${vivoCore}DateTimeValue"/>

<c:set var="dateTimeValue" value="${vivoCore}dateTime"/>
<c:set var="dateTimePrecision" value="${vivoCore}dateTimePrecision"/>

<%-- Assertions for adding a new date time value --%>
<v:jsonset var="n3ForValue">
    ?subject      <${toDateTimeValue}> ?valueNode .
    ?valueNode  <${type}> <${valueType}> .
    ?valueNode  <${dateTimeValue}> ?dateTimeField-value .
    ?valueNode  <${dateTimePrecision}> ?dateTimeField-precision .
</v:jsonset>

<%-- Queries for editing an existing role --%>
<v:jsonset var="existingNodeQuery" >
    SELECT ?existingNode WHERE {
          ?subject <${toDateTimeValue}> ?existingNode .
          ?existingNode <${type}> <${valueType}> . }
</v:jsonset>

<v:jsonset var="existingDateTimeValueQuery" >
    SELECT ?existingDateTimeValue WHERE {
     ?subject <${toDateTimeValue}> ?existingValueNode .
     ?existingValueNode <${type}> <${valueType}> .
     ?existingValueNode <${dateTimeValue}> ?existingDateTimeValue . }
</v:jsonset>

<v:jsonset var="existingPrecisionQuery" >
    SELECT ?existingPrecision WHERE {
      ?subject <${toDateTimeValue}> ?existingValueNode .
      ?existingValueNode <${type}> <${valueType}> .
      ?existingValueNode <${dateTimePrecision}> ?existingPrecision . }
</v:jsonset>


<%-- Configure add vs. edit --%>
<%

    String objectUri = (String) request.getAttribute("objectUri");
    if (objectUri != null) { // editing existing entry
%>
        <c:set var="editMode" value="edit" />
        <c:set var="titleVerb" value="Edit" />
        <c:set var="submitButtonText" value="Edit Date/Time Value" />
        <c:set var="disabledVal" value="disabled" />
<% 
    } else { // adding new entry
%>
        <c:set var="editMode" value="add" />
        <c:set var="titleVerb" value="Create" />
        <c:set var="submitButtonText" value="Create Date/Time Value" />
        <c:set var="disabledVal" value="" />
<%  } %> 

<c:set var="editjson" scope="request">
  {
    "formUrl" : "${formUrl}",
    "editKey" : "${editKey}",
    "urlPatternToReturnTo" : "/individual",
    
    "subject"   : ["subject",    "${subjectUriJson}" ],
    "predicate" : ["toDateTimeValue", "${predicateUriJson}" ],
    "object"    : ["valueNode", "${objectUriJson}", "URI" ],
    
    "n3required"    : [  ],
    
    "n3optional"    : [ "${n3ForValue}" ],
    
    "newResources"  : { "valueNode" : "${defaultNamespace}" },
    
    "urisInScope"    : { },
    "literalsInScope": { },
    "urisOnForm"     : [ ],
    "literalsOnForm" :  [ ],
    "filesOnForm"    : [ ],
    "sparqlForLiterals" : { },
    "sparqlForUris" : {  },
    "sparqlForExistingLiterals" : {
        "dateTimeField-value"   : "${existingDateTimeValueQuery}",
    },
    "sparqlForExistingUris" : {
        "valueNode"      : "${existingNodeQuery}",
        "dateTimeField-precision": "${existingPrecisionQuery}"
    },
    "fields" : {     
      "dateTimeField" : {
         "newResource"      : "false",
         "validators"       : [  ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "",
         "rangeLang"        : "",
         "assertions"       : ["${n3ForValue}"]
      }
  }
}
</c:set>

<%
    log.debug(request.getAttribute("editjson"));

    EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,request);
    if (editConfig == null) {
        editConfig = new EditConfiguration((String) request.getAttribute("editjson"));
        EditConfiguration.putConfigInSession(editConfig,session);
        
        //setup date time edit elements
        Field dateTimeField = editConfig.getField("dateTimeField");
        // arguments for DateTimeWithPrecision are (fieldName, minimumPrecision, [requiredLevel])
        dateTimeField.setEditElement(new DateTimeWithPrecision(dateTimeField, VitroVocabulary.Precision.SECOND.uri(), VitroVocabulary.Precision.NONE.uri()));
    }     
            
    Model model = (Model) application.getAttribute("jenaOntModel");
    
    if (objectUri != null) { // editing existing
        editConfig.prepareForObjPropUpdate(model);
    } else { // adding new
        editConfig.prepareForNonUpdate(model);
    }
    
    List<String> customJs = new ArrayList<String>(Arrays.asList(JavaScript.JQUERY_UI.path(),
                                                                JavaScript.CUSTOM_FORM_UTILS.path()
                                                               ));            
    request.setAttribute("customJs", customJs);
    
    List<String> customCss = new ArrayList<String>(Arrays.asList(Css.JQUERY_UI.path(),
                                                                 Css.CUSTOM_FORM.path()
                                                                ));
    request.setAttribute("customCss", customCss);
    
    String subjectName = ((Individual) request.getAttribute("subject")).getName();
%>

<jsp:include page="${preForm}" />

<h2>${titleVerb} date time value for <%= subjectName %></h2>
<h1>JSP form, must be removed for the 1.4!</h1>
<form class="customForm" action="<c:url value="/edit/processRdfForm2.jsp"/>" >
    
    <v:input id="dateTimeField" />
       
    <p class="submit"><v:input type="submit" id="submit" value="${submitButtonText}" cancel="true"/></p>
</form>
    
<jsp:include page="${postForm}"/>
