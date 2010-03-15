<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.Literal"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>

<%
    org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("edu.cornell.mannlib.vitro.jsp.edit.forms.defaultVitroNsPropForm.jsp");
    log.debug("Starting defaultVitroNsPropForm.jsp");
    
    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    vreq.setAttribute("defaultNamespace", wdf.getDefaultNamespace());
    
    String subjectUri   = vreq.getParameter("subjectUri");
    String predicateUri = vreq.getParameter("predicateUri");
 
    DataPropertyStatement dps = (DataPropertyStatement)vreq.getAttribute("dataprop");
     
    String datapropKeyStr = vreq.getParameter("datapropKey");
    
    Individual subject = (Individual)vreq.getAttribute("subject");
    if( subject == null ) {
        throw new Error("In vitroNsEditLabelForm.jsp, could not find subject " + subjectUri);
    }

%>

<%-- RY Once this is working, change to just one vitroNsEditForm for all vitro ns props, by parameterizing the predicate.
The title and submit button text will need to be customized.
Not sure sparqlForExistingLiterals is needed: see defaultDatapropForm - doesn't use it.
 --%>
<%-- RY Change labelExisting, label, and labelAssertion to variables once this is working,
so it can be more easily copied to another form. 
Also change hard-coded predicate to ?predicate, so it will be picked up from the editConfig predicate --%>

<c:set var="predicate" value="<%=predicateUri%>" />
<c:set var="propertyName" value="${fn:substringAfter(predicate, '#')}" />

<%--  Then enter a SPARQL query for the field, by convention concatenating the field id with "Existing"
      to convey that the expression is used to retrieve any existing value for the field in an existing individual.
      This must then be referenced in the sparqlForExistingLiterals section of the JSON block below
      and in the literalsOnForm --%>
<v:jsonset var="dataExisting">
    SELECT ?dataExisting WHERE {
        ?subject <${predicate}> ?dataExisting }
</v:jsonset>

<%--  Pair the "existing" query with the skeleton of what will be asserted for a new statement involving this field.
      The actual assertion inserted in the model will be created via string substitution into the ? variables.
      NOTE the pattern of punctuation (a period after the prefix URI and after the ?field) --%>
<v:jsonset var="dataAssertion"  >
    ?subject <${predicate}> ?label .
</v:jsonset>

<%-- RY This will be the default, but base it on propertyName --%>
<c:set var="rangeDatatypeUri" value="http://www.w3.org/2001/XMLSchema#string" />

<c:set var="editjson" scope="request">
  {
    "formUrl"              : "${formUrl}",
    "editKey"              : "${editKey}",
    "datapropKey"          : "<%= datapropKeyStr == null ? "" : datapropKeyStr %>",    
    "urlPatternToReturnTo" : "/entity",

    "subject"   : ["subject",   "${subjectUriJson}" ],
    "predicate" : ["predicate", "${predicateUriJson}" ],
    "object"    : ["${propertyName}", "", "DATAPROPHASH" ],
    
    "n3required"    : [ "${dataAssertion}" ],
    "n3optional"    : [ ],
    "newResources"  : { },
    "urisInScope"    : { },
    "literalsInScope": { },
    "urisOnForm"     : [ ],
    "literalsOnForm" :  [ "${propertyName}" ],
    "filesOnForm"    : [ ],
    "sparqlForLiterals" : { },
    "sparqlForUris" : {  },
    "sparqlForExistingLiterals" : { "${propertyName}" : "${dataExisting}" },
    "sparqlForExistingUris" : { },
    "fields" : {
      "label" : {
         "newResource"      : "false",
         "validators"       : [ "nonempty" ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "${rangeDatatypeUri}",
         "rangeLang"        : "",
         "assertions"       : [ "${dataAssertion}" ]
      }
  }
}
</c:set>

<%
    EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,request);
    if (editConfig == null) {
        editConfig = new EditConfiguration((String)vreq.getAttribute("editjson"));
        EditConfiguration.putConfigInSession(editConfig, session);
    }
    
    if ( datapropKeyStr != null && datapropKeyStr.trim().length() > 0  ) {
        Model model =  (Model)application.getAttribute("jenaOntModel");
        editConfig.prepareForDataPropUpdate(model,dps);
    }    

%>

<c:set var="propertyLabel" value="${propertyName == 'label' ? 'name' : propertyName}" />
<c:set var="submitLabel" value="Edit ${propertyLabel}" />
<c:set var="title" scope="request" value="Edit the ${propertyLabel} of ${subject.name}:" />

<jsp:include page="${preForm}"/>

<h2>${title}</h2>
<form action="<c:url value="/edit/processDatapropRdfForm.jsp"/>" >
    <v:input type="text" id="label" size="30" />
    <p class="submit"><v:input type="submit" id="submit" value="${submitLabel}" cancel="${param.subjectUri}"/></p>
</form>

<jsp:include page="${postForm}"/>

