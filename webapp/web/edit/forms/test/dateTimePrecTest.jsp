<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%-- 

This is a test file for the DateTimeWithPrecision EditElement.

 --%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>

<%
    org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("edu.cornell.mannlib.vitro.jsp.edit.forms.test.dateTimePrecTest.jsp");
    log.debug("Starting dateTimePrecTest.jsp");
%>
<%
    Individual subject = (Individual)request.getAttribute("subject");    
    VitroRequest vreq = new VitroRequest(request);    
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();    
%>

<v:jsonset var="queryForInverse" >
    PREFIX owl:  <http://www.w3.org/2002/07/owl#>
    SELECT ?inverse_property
    WHERE {
        ?inverse_property owl:inverseOf ?predicate
    }
</v:jsonset>

<v:jsonset var="sparqlForDxPrecision"  >
 PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
 PREFIX core: <http://vivoweb.org/ontology/core#>     
 SELECT ?dtX-precision WHERE {
    ?subject ?predicate ?object .
    ?object <core:hasDateTimeValue> ?dtX. 
    ?dtX <rdf:type>  <core:DateTimeValue> .           
    ?dtX <core:dateTimePrecision> ?dtX-precision .            
</v:jsonset>

<v:jsonset var="sparqlForDxValue"  >
 PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>    
 PREFIX core: <http://vivoweb.org/ontology/core#>     
 SELECT ?dtX-value WHERE {
    ?subject ?predicate ?object .
    ?object <core:hasDateTimeValue> ?dtX. 
    ?dtX <rdf:type>  <core:DateTimeValue> .    
    ?dtX <core:dateTime> ?dtX-value .                   
</v:jsonset>

<v:jsonset var="n3ForEdit"  >
    @prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
    @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
    @prefix vivo: <http://vivo.library.cornell.edu/ns/0.1#> .
    @prefix vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> .
    @prefix core: <http://vivoweb.org/ontology/core#> .
    
    ?subject ?predicate ?object .
    ?object <core:hasDateTimeValue> ?dtX. 
    ?dtX <rdf:type>  <core:DateTimeValue> .
    
    ?dtX <core:dateTime> ?dtX-value .    
    ?dtX <core:dateTimePrecision> ?dtX-precision .            
</v:jsonset>

<c:set var="editjson" scope="request">
  {
    "formUrl" : "${formUrl}",
    "editKey" : "${editKey}",
    "urlPatternToReturnTo" : "/individual",

    "subject"   : [ "subject", "${subjectUriJson}" ],
    "predicate" : [ "predicate", "${predicateUriJson}" ],
    "object"    : [ "object", "${objectUriJson}", "URI" ],
    
    "n3required"                : [ "${n3ForEdit}" ],
    "n3optional"                : [  ],
    "newResources"              : { "dtX" : "", "object" : "" },
    "urisInScope"               : { },
    "literalsInScope"           : { },
    "urisOnForm"                : [ ],
    "literalsOnForm"            : [ ],
    "filesOnForm"               : [ ],
    "sparqlForLiterals"         : { },
    "sparqlForUris"             : { },
    "sparqlForExistingLiterals" : { "dtX-value": "${sparqlForDxValue}" },
    "sparqlForExistingUris"     : { "dtX-precision": "${sparqlForDxPrecision}" },
    "fields" : {
        "dtX" : {
            "newResource"       : "true",
            "validators"        : [  ],
            "optionsType"       : "edu.cornell.mannlib.vitro.webapp.edit.elements.DateTimeWithPrecision",
            "editElement"       : "edu.cornell.mannlib.vitro.webapp.edit.elements.DateTimeWithPrecision",
            "literalOptions"    : [ ],
            "predicateUri"      : "",
            "objectClassUri"    : "",
            "rangeDatatypeUri"  : "",
            "rangeLang"         : "",
            "assertions"        : [ "${n3ForEdit}" ]
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
    
    /* title is used by pre and post form fragments */    
    request.setAttribute("title", "Edit dateTimePrec entry for " + subject.getName());        
%>

<jsp:include page="${preForm}">
    <jsp:param name="useTinyMCE" value="false"/>    
</jsp:include>

<h2>${title}</h2>

<form action="<c:url value="/edit/processRdfForm2.jsp"/>" >
    <v:input id="dtX" type="what" />        
    <v:input type="submit" id="submit" value="submit" cancel="true"/>
</form>

<jsp:include page="${postForm}"/>
