<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>
<%!
    public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.forms.autoCompleteObjPropForm.jsp");
%>
<%
	log.warn("Starting autoCompleteObjPropForm.jsp");
    Individual subject = (Individual)request.getAttribute("subject");
    ObjectProperty prop = (ObjectProperty)request.getAttribute("predicate");

    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    if( prop.getRangeVClassURI() == null ) {
    	log.debug("Property has null for its range class URI");
    	// If property has no explicit range, we will use e.g. owl:Thing.
    	// Typically an allValuesFrom restriction will come into play later.
    	VClass top = wdf.getVClassDao().getTopConcept();
    	prop.setRangeVClassURI(top.getURI());
    	log.debug("Using "+prop.getRangeVClassURI());
    }

    VClass rangeClass = wdf.getVClassDao().getVClassByURI( prop.getRangeVClassURI());
    if( rangeClass == null ) log.debug("Cannot find class for range for property.  Looking for " + prop.getRangeVClassURI() );
%>

<v:jsonset var="queryForInverse" >
    PREFIX owl:  <http://www.w3.org/2002/07/owl#>
    SELECT ?inverse_property
    WHERE {
        ?inverse_property owl:inverseOf ?predicate
    }
</v:jsonset>

<c:set var="objectVar" value="<%=prop.getLocalName()%>"/>

<v:jsonset var="n3ForEdit"  >
    ?subject ?predicate ?${objectVar}.
</v:jsonset>

<v:jsonset var="n3Inverse" >
    ?${objectVar} ?inverseProp ?subject.
</v:jsonset>

<c:set var="editjson" scope="request">
  {
    "formUrl"                   : "${formUrl}",
    "editKey"                   : "${editKey}",
    "urlPatternToReturnTo"      : "/entity",

    "subject"      : [ "subject", "${subjectUriJson}" ] ,
    "predicate"    : [ "predicate", "${predicateUriJson}" ],
    "object"       : [ "${objectVar}" ,  "${objectUriJson}" , "URI"],

    "n3required"                : [ "${n3ForEdit}" ],
    "n3optional"                : [ "${n3Inverse}" ],
    "newResources"              : { },

    "urisInScope"               : { },
    "literalsInScope"           : { },

    "urisOnForm"                : ["${objectVar}"],
    "literalsOnForm"            : [ ],
    "filesOnForm"               : [ ],

    "sparqlForLiterals"         : { },
    "sparqlForUris"             : {"inverseProp" : "${queryForInverse}" },

    "sparqlForExistingLiterals" : { },
    "sparqlForExistingUris"     : { },
    "fields"                    : { "${objectVar}" : {
                                       "newResource"      : "false",
                                       "queryForExisting" : { },
                                       "validators"       : [ ],
                                       "optionsType"      : "INDIVIDUALS_VIA_OBJECT_PROPERTY",
                                       "subjectUri"       : "${subjectUriJson}",
                                       "subjectClassUri"  : "",
                                       "predicateUri"     : "${predicateUriJson}",
                                       "objectClassUri"   : "",
                                       "rangeDatatypeUri" : "",
                                       "rangeLang"        : "",
                                       "literalOptions"   : [ ] ,
                                       "assertions"       : ["${n3ForEdit}"]
                                     }
                                  }
  }
</c:set>

<%  /* now put edit configuration Json object into session */
    EditConfiguration editConfig = new EditConfiguration((String)request.getAttribute("editjson"));
    EditConfiguration.putConfigInSession(editConfig, session);
    String formTitle   ="";
    String submitLabel ="";
    Model model = (Model)application.getAttribute("jenaOntModel");
    if( request.getAttribute("object") != null ){//this block is for an edit of an existing object property statement
        editConfig.prepareForObjPropUpdate( model );
        formTitle   = "Change entry for: <em>"+prop.getDomainPublic()+"</em>";
        submitLabel = "save change";
    } else {
        editConfig.prepareForNonUpdate( model );
        if ( prop.getOfferCreateNewOption() ) {
            log.debug("property set to offer \"create new\" option; custom form: ["+prop.getCustomEntryForm()+"]");
            formTitle   = "Select an existing "+rangeClass.getName()+" for "+subject.getName();
            submitLabel = "Select existing";
        } else {
            formTitle   = "Add an entry to: <em>"+prop.getDomainPublic()+"</em>";
            submitLabel = "Save entry";
        }
    }
%>
<jsp:include page="${preForm}">
    <jsp:param name="useTinyMCE" value="false"/>
    <jsp:param name="useAutoComplete" value="true"/>
</jsp:include>
<h1>JSP form, must be removed for the 1.4!</h1>

<script type="text/javascript" language="javascript">
$(this).load($(this).parent().children('a').attr('src')+" .editForm");

$(document).ready(function() {
    var key = $("input[name='editKey']").attr("value");
    $.getJSON("<c:url value="/dataservice"/>", {getN3EditOptionList:"1", field: "${objectVar}", editKey: key}, function(json){

    $("select#${objectVar}").replaceWith("<input type='hidden' id='${objectVar}' name='${objectVar}' /><input type='text' id='${objectVar}-entry' name='${objectVar}-entry' />");

    $("#${objectVar}-entry").autocomplete(json, {
            minChars: 1,
            width: 320,
            matchContains: true,
            mustMatch: 1,
            autoFill: true,
            // formatItem: function(row, i, max) {
            //     return row[0];
            // },
            // formatMatch: function(row, i, max) {
            //     return row[0];
            // },
            // formatResult: function(row) {
            //     return row[0];
            // }
           
        }).result(function(event, data, formatted) {
             $("input#${objectVar}-entry").attr("value", data[0]); // dump the string into the text box
             $("input#${objectVar}").attr("value", data[1]); // dump the uri into the hidden form input
           });
}
);
})
</script>

<h2><%=formTitle%></h2>
<form class="editForm" action="<c:url value="/edit/processRdfForm2.jsp"/>" >
    <c:if test="${predicate.offerCreateNewOption == true}">
        <c:url var="createNewUrl" value="/edit/editRequestDispatch.jsp">
            <c:param name="subjectUri" value="${param.subjectUri}"/>
            <c:param name="predicateUri" value="${param.predicateUri}"/>
            <c:param name="clearEditConfig" value="true"/>
            <c:param name="cmd" value="create"/>
        </c:url>
    </c:if>
    <c:if test="${!empty predicate.publicDescription}">
    	<p>${predicate.publicDescription}</p>
    </c:if>
    <v:input type="select" id="${objectVar}" size="80" />
    <v:input type="submit" id="submit" value="<%=submitLabel%>" cancel="true"/>
    <c:if test="${predicate.offerCreateNewOption == true}">
        <p>If you don't find the appropriate entry on the selection list,
        <button type="button" onclick="javascript:document.location.href='${createNewUrl}'">Add a new item to this list</button>
        </p>
    </c:if>
</form>

<c:if test="${!empty param.objectUri}" >
    <form class="deleteForm" action="editRequestDispatch.jsp" method="get">       
	 	<label for="delete"><h3>Delete this entry?</h3></label>
        <input type="hidden" name="subjectUri"   value="${param.subjectUri}"/>
        <input type="hidden" name="predicateUri" value="${param.predicateUri}"/>
        <input type="hidden" name="objectUri"    value="${param.objectUri}"/>    
        <input type="hidden" name="cmd"          value="delete"/>
        <v:input type="submit" id="delete" value="Delete" cancel="" />
    </form>
</c:if>

<jsp:include page="${postForm}"/>
