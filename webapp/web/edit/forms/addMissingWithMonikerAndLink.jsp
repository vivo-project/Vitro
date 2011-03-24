<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Literal" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>
<%
    Individual subject = (Individual)request.getAttribute("subject");
    ObjectProperty prop = (ObjectProperty)request.getAttribute("predicate");
    if (prop == null) throw new Error("no object property specified via incoming predicate attribute in defaultAddMissingIndividualForm.jsp");
    String propDomainPublic = (prop.getDomainPublic() == null) ? "affiliation" : prop.getDomainPublic();

    VitroRequest vreq = new VitroRequest(request);
    //String contextPath = vreq.getContextPath();
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    if( prop.getRangeVClassURI() == null )throw new Error("Property has null for its range class URI");

    VClass rangeClass = wdf.getVClassDao().getVClassByURI(prop.getRangeVClassURI());
    if( rangeClass == null ) throw new Error ("Cannot find class for range for property.  Looking for " + prop.getRangeVClassURI() );
    //vreq.setAttribute("rangeClassLocalName",rangeClass.getLocalName());
    //vreq.setAttribute("rangeClassNamespace",rangeClass.getNamespace());
    vreq.setAttribute("rangeClassUri",prop.getRangeVClassURI());
    vreq.setAttribute("curatorReviewUri","http://vivo.library.cornell.edu/ns/0.1#CuratorReview");
    
    //get the current portal and make this new individual a member of that portal
    vreq.setAttribute("portalUri", vreq.getPortal().getTypeUri());
%>

<v:jsonset var="queryForInverse" >
    PREFIX owl:  <http://www.w3.org/2002/07/owl#>
    SELECT ?inverse_property
    WHERE { ?inverse_property owl:inverseOf ?predicate }
</v:jsonset>

<%-- Enter here the class names to be used for constructing MONIKERS_VIA_VCLASS pick lists
     These are then referenced in the field's ObjectClassUri but not elsewhere.
     Note that you can't reference a jsonset variable inside another jsonset expression
     or you get double escaping problems --%>
<v:jsonset var="newIndividualVClassUri">${rangeClassUri}</v:jsonset>

<%-- Then enter a SPARQL query for each field, by convention concatenating the field id with "Existing"
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
<v:jsonset var="nameAssertion" >
    @prefix vivo: <http://vivo.library.cornell.edu/ns/0.1#> .
    ?newIndividual rdfs:label ?name .
</v:jsonset>

<v:jsonset var="monikerExisting" >
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?existingMoniker
    WHERE { ?newIndividual vitro:moniker ?existingMoniker }
</v:jsonset>
<v:jsonset var="monikerAssertion" >
    @prefix vivo: <http://vivo.library.cornell.edu/ns/0.1#> .
    ?newIndividual vitro:moniker ?moniker .
</v:jsonset>

<v:jsonset var="linkUrlExisting" >
    PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>
    SELECT ?existingLinkUrl
    WHERE { ?newIndividual vitro:primaryLink ?newLink ;
            ?newLink vitro:linkURL ?existingLinkUrl .            
    }
</v:jsonset>
<v:jsonset var="linkUrlAssertion" >
    @prefix vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> .
    ?newLink vitro:linkURL ?linkUrl .
</v:jsonset>

<v:jsonset var="n3ForEdit"  >
    @prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
    @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
    @prefix vivo: <http://vivo.library.cornell.edu/ns/0.1#> .
    @prefix vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> .

    ?newIndividual rdf:type <${curatorReviewUri}> .
    ?newIndividual rdf:type <${rangeClassUri}> .
    ?subject ?predicate ?newIndividual .
    
    ?newIndividual rdfs:label ?name .
</v:jsonset>

<v:jsonset var="n3Inverse" >
    ?newIndividual ?inverseProp ?subject .
</v:jsonset>

<%-- make sure you have all the @prefix entries to cover the statements in each block --%>
<v:jsonset var="n3optional" >
    @prefix vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> .
    ?newIndividual vitro:moniker ?moniker .
</v:jsonset>

<%-- set the portal of the new individual to the current portal. --%>
<v:jsonset  var="n3portal">
   @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
   ?newIndividual rdf:type <${portalUri}> .
</v:jsonset>

<%-- note that it's safer to have multiple distinct optional blocks so that a failure in one
     will not prevent correct sections from being inserted --%>
<v:jsonset var="n3link" >
    @prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
    @prefix vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> .
    ?newLink
        rdf:type vitro:Link ;
        vitro:linkURL ?linkUrl ;
        vitro:linkAnchor ?name ;
        vitro:linkDisplayRank "1" .
    
    ?newIndividual vitro:primaryLink ?newLink .
</v:jsonset>


<c:set var="editjson" scope="request">
  {
    "formUrl" : "${formUrl}",
    "editKey" : "${editKey}",
    "urlPatternToReturnTo" : "/entity",

    "subject"   : [ "subject", "${subjectUriJson}" ],
    "predicate" : [ "predicate", "${predicateUriJson}" ],
    "object"    : [ "newIndividual", "${objectUriJson}", "URI" ],
    
    "n3required"                : [ "${n3ForEdit}" ],
    "n3optional"                : [ "${n3optional}", "${n3Inverse}", "${n3link}", "${n3portal}" ],
    "newResources" : {
        "newIndividual"         : "http://vivo.library.cornell.edu/ns/0.1#individual",
        "newLink"               : "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Link"
    },
    "urisInScope"               : { },
    "literalsInScope"           : { },
    "urisOnForm"                : [ ],
    "literalsOnForm"            : [ "name","moniker","linkUrl" ],
    "filesOnForm"               : [ ],
    "sparqlForLiterals"         : { },
    "sparqlForUris"             : { "inverseProp" : "${queryForInverse}" },
    "sparqlForExistingLiterals" : {
        "name"                  : "${nameExisting}",
        "moniker"               : "${monikerExisting}",
        "linkUrl"               : "${linkUrlExisting}"
    },
    "sparqlForExistingUris"     : { },
    "fields" : {
        "name" : {
            "newResource"       : "false",
            "validators"        : [ "nonempty" ],
            "optionsType"       : "UNDEFINED",
            "literalOptions"    : [ ],
            "predicateUri"      : "",
            "objectClassUri"    : "",
            "rangeDatatypeUri"  : "",
            "rangeLang"         : "",
            "assertions"        : [ "${nameAssertion}" ]
        },
        "moniker" : {
            "newResource"       : "false",
            "validators"        : [ ],
            "optionsType"       : "MONIKERS_VIA_VCLASS",
            "literalOptions"    : [ ],
            "predicateUri"      : "",
            "objectClassUri"    : "${newIndividualVClassUri}",
            "rangeDatatypeUri"  : "",
            "rangeLang"         : "",
            "assertions"        : [ "${monikerAssertion}" ]
        },
        "linkUrl" : {
            "newResource"       : "false",
            "validators"        : [],
            "optionsType"       : "UNDEFINED",
            "literalOptions"    : [],
            "predicateUri"      : "",
            "objectClassUri"    : "",
            "rangeDatatypeUri"  : "",
            "rangeLang"         : "",
            "assertions"        : [ "${linkUrlAssertion}" ]
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

    String submitButtonLabel=""; // don't put local variables into the request
    /* title is used by pre and post form fragments */
    if (objectUri != null) {
    	request.setAttribute("title", "Edit \""+propDomainPublic+"\" entry for " + subject.getName());
        submitButtonLabel = "Save changes";
    } else {
        request.setAttribute("title","Create a new \""+propDomainPublic+"\" entry for " + subject.getName());
        submitButtonLabel = "Create new \""+propDomainPublic+"\" entry";
    }

%>

<jsp:include page="${preForm}">
    <jsp:param name="useTinyMCE" value="false"/>
    <jsp:param name="useAutoComplete" value="true"/>
</jsp:include>

<script type="text/javascript" language="javascript">
$(this).load($(this).parent().children('a').attr('src')+" .editForm");

$(document).ready(function() {
    var key = $("input[name='editKey']").attr("value");
    $.getJSON("<c:url value="/dataservice"/>", {getN3EditOptionList:"1", field: "moniker", editKey: key}, function(json){

    $("select#moniker").replaceWith("<input type='text' id='moniker' name='moniker' />");

    $("#moniker").autocomplete(json, {
            minChars: 0,
            width: 320,
            matchContains: true,
            mustMatch: 0,
            autoFill: false,
            formatItem: function(row, i, max) {
                return row[0];
            },
            formatMatch: function(row, i, max) {
                return row[0];
            },
            formatResult: function(row) {
                return row[0];
            }
           
        }).result(function(event, data, formatted) {
             $("input#moniker").attr("value", data[1]);
           });
}
);
})
</script>

<h2>${title}</h2>
<form action="<c:url value="/edit/processRdfForm2.jsp"/>" >
    <v:input type="text" label="name (required)" id="name" size="30"/>
    <hr/>
    <v:input type="select" label="label (optional)" id="moniker"/> <em>start typing to see existing choices, or add a new label</em>
    <v:input type="text" label="associated web page (optional)" id="linkUrl" size="50"/>
    <v:input type="submit" id="submit" value="<%=submitButtonLabel%>" cancel="${param.subjectUri}"/>
</form>

<jsp:include page="${postForm}"/>
