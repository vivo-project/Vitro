<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Arrays"%>

<%@ page import="com.hp.hpl.jena.rdf.model.Literal"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@ page import="com.hp.hpl.jena.vocabulary.XSD"%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils"%>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>

<%
    org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("edu.cornell.mannlib.vitro.jsp.edit.forms.rdfLabelForm.jsp");

    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    vreq.setAttribute("defaultNamespace", wdf.getDefaultNamespace());
    
    String subjectUri   = vreq.getParameter("subjectUri");
    String predicateUri = vreq.getParameter("predicateUri");

    log.debug("Starting rdfsLabelForm.jsp");
    
    DataPropertyStatement dps = (DataPropertyStatement)vreq.getAttribute("dataprop");
     
    String datapropKeyStr = vreq.getParameter("datapropKey");
    
    Individual subject = (Individual)vreq.getAttribute("subject");
    if( subject == null ) {
        throw new Error("In rdfsLabelForm.jsp, could not find subject " + subjectUri);
    }
    
    Model model =  (Model)application.getAttribute("jenaOntModel");
    
    // Get datatype and language for the data property statement
    String rangeDatatypeUri = null,
           rangeLang = null;
    
    if (dps != null) {
        
        rangeDatatypeUri = dps.getDatatypeURI();        
        if (rangeDatatypeUri == null) {
            log.debug("no range datatype uri set on rdfs:label statement for property " + predicateUri + "in rdfsLabelForm.jsp");
        } else {
            log.debug("range datatype uri of [" + rangeDatatypeUri + "] on rdfs:label statement for property " + predicateUri + "in rdfsLabelForm.jsp");
        }        
        
        rangeLang = dps.getLanguage();
        if( rangeLang == null ) {            
            log.debug("no language attribute on rdfs:label statement for property " + predicateUri + "in rdfsLabelForm.jsp");
            rangeLang = "";
        } else {
            log.debug("language attribute of ["+rangeLang+"] on rdfs:label statement for property " + predicateUri + "in rdfsLabelForm.jsp");
        }
        
    } else {
        log.debug("No incoming rdfs:label statement for property "+predicateUri+"; adding a new statement");  
        rangeDatatypeUri = XSD.xstring.getURI();
    }
    
    String rangeDatatypeUriJson = rangeDatatypeUri == null ? "" : MiscWebUtils.escape(rangeDatatypeUri);
  
    vreq.setAttribute("rangeDatatypeUriJson", rangeDatatypeUriJson);   
    vreq.setAttribute("rangeLangJson", rangeLang);    

    // Create list of validators
    ArrayList<String> validatorList = new ArrayList<String>();
    if (predicateUri.equals(VitroVocabulary.LABEL) || predicateUri.equals(VitroVocabulary.RDF_TYPE)) {
        validatorList.add("\"nonempty\"");       
    }
    if (!StringUtils.isEmpty(rangeDatatypeUriJson)) {
        validatorList.add("\"datatype:" + rangeDatatypeUriJson + "\"");
    }
    vreq.setAttribute("validators", StringUtils.join(validatorList, ","));

%>

<c:set var="predicate" value="<%=predicateUri%>" />

<%--  Pair the "existing" query with the skeleton of what will be asserted for a new statement involving this field.
      The actual assertion inserted in the model will be created via string substitution into the ? variables.
      NOTE the pattern of punctuation (a period after the prefix URI and after the ?field) --%>
<v:jsonset var="dataAssertion"  >
    ?subject <${predicate}> ?label .
</v:jsonset>

<c:set var="editjson" scope="request">
  {
    "formUrl"              : "${formUrl}",
    "editKey"              : "${editKey}",
    "datapropKey"          : "<%= datapropKeyStr == null ? "" : datapropKeyStr %>",    
    "urlPatternToReturnTo" : "/entity",
    
    "subject"   : ["subject",   "${subjectUriJson}" ],
    "predicate" : ["predicate", "${predicateUriJson}" ],
    "object"    : ["label", "", "DATAPROPHASH" ],
    
    "n3required"    : [ "${dataAssertion}" ],
    "n3optional"    : [ ],
    "newResources"  : { },
    "urisInScope"    : { },
    "literalsInScope": { },
    "urisOnForm"     : [ ],
    "literalsOnForm" :  [ "label" ],
    "filesOnForm"    : [ ],
    "sparqlForLiterals" : { },
    "sparqlForUris" : {  },
    "sparqlForExistingLiterals" : { },
    "sparqlForExistingUris" : { },
    "fields" : {
      "label" : {
         "newResource"      : "false",
         "validators"       : [ ${validators} ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "${rangeDatatypeUriJson}",
         "rangeLang"        : "${rangeLangJson}",
         "assertions"       : [ "${dataAssertion}" ]
      }
  }
}
</c:set>

<%
    if( log.isDebugEnabled()) log.debug(request.getAttribute("editjson"));
    
    EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,request);
    if (editConfig == null) {
        log.debug("No editConfig in session. Making new editConfig.");
        log.debug(vreq.getAttribute("editjson"));
        editConfig = new EditConfiguration((String)vreq.getAttribute("editjson"));
        EditConfiguration.putConfigInSession(editConfig, session);

    }
    
    if ( datapropKeyStr != null && datapropKeyStr.trim().length() > 0  ) {
        editConfig.prepareForDataPropUpdate(model,dps);
    }    

    // Configure form
    String actionText = dps == null ? "Add new " : "Edit ";
    String submitLabel = actionText + "label";
    String title = actionText + "<em>label</em> for " + subject.getName();
  
%>

<jsp:include page="${preForm}">
    <jsp:param name="useTinyMCE" value="false"/>
</jsp:include>

<h1>JSP form, must be removed for the 1.4!</h1>

<h2><%= title %></h2>
<form action="<c:url value="/edit/processDatapropRdfForm.jsp"/>" >
    <v:input type="text" id="label" size="30" />
    <input type="hidden" name="vitroNsProp" value="true" />
    <p class="submit"><v:input type="submit" id="submit" value="<%= submitLabel %>" cancel="true"/></p>
</form>

<c:if test="${ (!empty param.datapropKey) && (empty param.deleteProhibited) }">
     <form class="deleteForm" action="editDatapropStmtRequestDispatch.jsp" method="post">                               
            <label for="delete"><h3>Delete this entry?</h3></label>
            <input type="hidden" name="subjectUri"   value="${param.subjectUri}"/>
            <input type="hidden" name="predicateUri" value="${param.predicateUri}"/>
            <input type="hidden" name="datapropKey"  value="${param.datapropKey}"/>                
            <input type="hidden" name="cmd"          value="delete"/>
            <input type="hidden" name="vitroNsProp" value="true" />
            <v:input type="submit" id="delete" value="Delete" cancel="" />
     </form>
</c:if>

<jsp:include page="${postForm}"/>


