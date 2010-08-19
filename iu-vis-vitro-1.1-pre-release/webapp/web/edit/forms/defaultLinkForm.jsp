<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.Literal" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="com.hp.hpl.jena.vocabulary.XSD" %>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils"%>

<%@ page import="java.util.List" %>

<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>
<%! 
    public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.forms.defaultLinkForm.jsp");
%>
<%
    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    vreq.setAttribute("defaultNamespace", wdf.getDefaultNamespace());
    
    String propertyUri = (String) request.getAttribute("predicateUri");
    String objectUri = (String) request.getAttribute("objectUri");  
    
    String stringDatatypeUriJson = MiscWebUtils.escape(XSD.xstring.toString());
    String uriDatatypeUriJson = MiscWebUtils.escape(XSD.anyURI.toString());
%>

<c:set var="stringDatatypeUriJson" value="<%= stringDatatypeUriJson %>" />
<c:set var="uriDatatypeUriJson" value="<%= uriDatatypeUriJson %>" />

<c:set var="rdfUri" value="<%= VitroVocabulary.RDF %>" />
<c:set var="vitroUri" value="<%= VitroVocabulary.vitroURI %>" />
<c:set var="linkUrl" value="<%= VitroVocabulary.LINK_URL %>" />
<c:set var="linkAnchor" value="<%= VitroVocabulary.LINK_ANCHOR %>" />
<c:set var="linkDisplayRank" value="<%= VitroVocabulary.LINK_DISPLAYRANK_URL %>" />

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
      WHERE { ?link <${linkUrl}>  ?urlExisting }
</v:jsonset>
<%--  Pair the "existing" query with the skeleton of what will be asserted for a new statement involving this field.
      The actual assertion inserted in the model will be created via string substitution into the ? variables.
      NOTE the pattern of punctuation (a period after the prefix URI and after the ?field) --%> 
<v:jsonset var="urlAssertion" >
      ?link <${linkUrl}>  ?url .
</v:jsonset>

<v:jsonset var="anchorExisting" >
      SELECT ?anchorExisting
      WHERE { ?link <${linkAnchor}> ?anchorExisting } 
</v:jsonset>
<v:jsonset var="anchorAssertion" >
      ?link <${linkAnchor}> ?anchor .
</v:jsonset>

<%-- RY Currently display rank is always hard-coded to -1, but later we may want to enable sorting. --%>
<v:jsonset var="displayRankExisting" >
      SELECT ?displayRankExisting
      WHERE { ?link <${linkDisplayRank}> ?displayRankExisting } 
</v:jsonset>
<v:jsonset var="displayRankAssertion" >
      ?link <${linkDisplayRank}> ?displayRank .
</v:jsonset>

<%--  When not retrieving a literal via a datatype property, put the SPARQL statement into
      the SparqlForExistingUris --%>

<v:jsonset var="n3ForEdit">
      @prefix rdf:  <${rdfUri}> .
      @prefix vitro: <${vitroUri}> .

      ?subject ?predicate  ?link .

      ?link rdf:type vitro:Link .

      ?link
          <${linkUrl}>  ?url ;
          <${linkAnchor}> ?anchor ;
          <${linkDisplayRank}> ?displayRank .
          
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
    "n3optional"        : [ ],
    "newResources"      : { "link" : "${defaultNamespace}" },
    "urisInScope"       : { },
    "literalsInScope"   : { },
    "urisOnForm"        : [ ],
    "literalsOnForm"    : [ "url", "anchor", "displayRank" ],
    "filesOnForm"       : [ ],
    "sparqlForLiterals" : { },
    "sparqlForUris"     : { },
    "sparqlForExistingLiterals" : {
        "url"         : "${urlExisting}",
        "anchor"      : "${anchorExisting}",
        "displayRank" : "${displayRankExisting}"
    },
    "sparqlForExistingUris" : { },
    "fields" : {
      "url" : {
         "newResource"      : "false",
         "validators"       : [ "nonempty", "datatype:${uriDatatypeUriJson}" ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "${uriDatatypeUriJson}",
         "rangeLang"        : "",
         "assertions"       : [ "${urlAssertion}" ]
      },
      "anchor" : {
         "newResource"      : "false",
         "validators"       : [ "nonempty", "datatype:${stringDatatypeUriJson}" ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "${stringDatatypeUriJson}",
         "rangeLang"        : "",
         "assertions"       : [ "${anchorAssertion}" ]
      },
      "displayRank" : {
         "newResource"      : "false",
         "validators"       : [ ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "${stringDatatypeUriJson}",
         "rangeLang"        : "",
         "assertions"       : [ "${displayRankAssertion}" ]      
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
    if( objectUri != null ){        
        editConfig.prepareForObjPropUpdate(model);            
    }else{
        editConfig.prepareForNonUpdate(model);
    }

    /* get some data to make the form more useful */
    String subjectName = ((Individual)request.getAttribute("subject")).getName();

    String submitLabel=""; 
    String title="";
    String linkCategory = propertyUri.equals(VitroVocabulary.PRIMARY_LINK) ? "primary" : "additional";
    if (objectUri != null) {
    	title = "Edit <em>" + linkCategory + " link</em> for " + subjectName;
        submitLabel = "Save changes";
    } else {
        title = "Create a new <em>" + linkCategory + " link</em> for " + subjectName;
        submitLabel = "Create new link";
    }

%>

<jsp:include page="${preForm}"/>

<h2><%= title %></h2>
<form action="<c:url value="/edit/processRdfForm2.jsp"/>" >
    <v:input type="text" label="URL" id="url" size="70"/>
    <v:input type="text" label="Link anchor text" id="anchor" size="70"/>
    <input type="hidden" name="displayRank" value="-1" />
    <p class="submit"><v:input type="submit" id="submit" value="<%=submitLabel%>" cancel="true"/></p>
</form>

<jsp:include page="${postForm}"/>
