<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%-- Custom form for adding a new Individual from the siteAdmin page.

This form is not associated with an object property, it is reached because the 
html:form on dataInput.jsp has an input element like:
 <input type="hidden" name="editForm" value="newIndividualForm.jsp"/>
 
This form is intended to only do the addition of an individual.  It is not configured with sparql 
for existing so it cannot handle an update.   It will not have a subject, predicate or object 
parameter set up by editRequestDispatch.
  
--%>

<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>

<%@ page import="com.hp.hpl.jena.rdf.model.Literal"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@ page import="com.hp.hpl.jena.vocabulary.XSD" %>

<%@page import="edu.cornell.mannlib.vitro.webapp.beans.VClass"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils"%>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>


<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>

<%! 
    public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.forms.newIndividualForm");
%>
<%
    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();    
    vreq.setAttribute("defaultNamespace", ""); //empty string triggers default new URI behavior
    
    request.setAttribute("stringDatatypeUriJson", MiscWebUtils.escape(XSD.xstring.toString()));  
    
    VClass type = wdf.getVClassDao().getVClassByURI(vreq.getParameter("typeOfNew"));
    
    List<String> customCss = new ArrayList<String>(Arrays.asList("/edit/forms/css/customForm.css"
                                                                 ));
    request.setAttribute("customCss", customCss);
%>

<c:set var="typeName" ><%= type.getName() %></c:set>

<c:set var="rdfs" value="<%= VitroVocabulary.RDFS %>" />
<c:set var="rdf" value="<%= VitroVocabulary.RDF %>" />

<v:jsonset var="n3ForType">
  @prefix rdf: <${rdf}> .
  ?newInd rdf:type <${param.typeOfNew}> .
</v:jsonset>

<v:jsonset var="n3ForRdfsLabel">
    @prefix rdfs: <${rdfs}> .
    ?newInd rdfs:label  ?label .
</v:jsonset>

<c:set var="editjson" scope="request">
  {
    "formUrl" : "${formUrl}",
    "editKey" : "${editKey}",
    "urlPatternToReturnTo" : "/individual",

    "subject"   : ["subjectNotUsed",   "" ],
    "predicate" : ["predicateNotUsed", "" ],
    "object"    : ["objectNotUsed",    "", "URI" ],
    
    "n3required"    : [ "${n3ForType}"  ],
    
    "n3optional"    : [ "${n3ForRdfsLabel}" ],
                        
    "newResources"  : { "newInd" : "${defaultNamespace}" },

    "urisInScope"    : { },
    "literalsInScope": { },
    "urisOnForm"     : [ ],
    "literalsOnForm" : [ "label", "firstName", "lastName" ],
    "filesOnForm"    : [ ],
    "sparqlForLiterals" : { },
    "sparqlForUris" : {  },
    "sparqlForExistingLiterals" : { },
    "sparqlForExistingUris" : { },
    "fields" : {
      "label" : {
         "newResource"      : "false",
         "validators"       : [ "nonempty" ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "${stringDatatypeUriJson}",
         "rangeLang"        : "",
         "assertions"       : [  ]
      }
  }
}
</c:set>
<%
    EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,request);
    if (editConfig == null) {
        editConfig = new EditConfiguration((String) request.getAttribute("editjson"));     
        EditConfiguration.putConfigInSession(editConfig,session);
    }
	editConfig.setEntityToReturnTo("?newInd");
	
    //this form always is always doing a non-update:
    Model model = (Model) application.getAttribute("jenaOntModel");
	editConfig.prepareForNonUpdate(model);        
%>

<c:set var="requiredHint" value="<span class='requiredHint'> *</span>" />

<jsp:include page="${preForm}" />
<h1>JSP form, must be removed for the 1.4!</h1>
<h2>Create a new ${typeName}</h2>

<form class="customForm" action="<c:url value="/edit/processRdfForm2.jsp"/>" >
	<v:input type="text" label="Name ${requiredHint}" id="label" size="30" />
    
    <c:set var="submitLabel" value="Create ${typeName}" />       
    <p class="submit"><v:input type="submit" id="submit" value="${submitLabel}" cancel="true" cancelUrl="/siteAdmin" /></p>    
    <p id="requiredLegend" class="requiredHint">* required fields</p>
</form>

<jsp:include page="${postForm}"/>
