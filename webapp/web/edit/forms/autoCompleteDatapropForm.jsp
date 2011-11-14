<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration" %>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils"%>

<%
    org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("edu.cornell.mannlib.vitro.jsp.edit.forms.autoCompleteDatapropForm.jsp");
	log.debug("Starting autoCompleteDatapropForm.jsp");
	
    String subjectUri   = request.getParameter("subjectUri");
    String predicateUri = request.getParameter("predicateUri");

    DataPropertyStatement dps = (DataPropertyStatement)request.getAttribute("dataprop");
    
    String datapropKeyStr = request.getParameter("datapropKey");
    int dataHash=0;

    DataProperty prop = (DataProperty)request.getAttribute("predicate");
    if( prop == null ) throw new Error("In autoCompleteDatapropForm.jsp, could not find predicate " + predicateUri);
    request.setAttribute("propertyName",prop.getPublicName());

    Individual subject = (Individual)request.getAttribute("subject");
    if( subject == null ) throw new Error("In autoCompleteDatapropForm.jsp, could not find subject " + subjectUri);
    request.setAttribute("subjectName",subject.getName());

    String rangeDatatypeUri = prop.getRangeDatatypeURI();
    request.setAttribute("rangeDatatypeUriJson", MiscWebUtils.escape(rangeDatatypeUri));
    
    if( dps != null ){
        try {
            dataHash = Integer.parseInt(datapropKeyStr);
            log.debug("dataHash is " + dataHash);            
        } catch (NumberFormatException ex) {
            log.debug("could not parse dataprop hash "+ 
                    "but there was a dataproperty; hash: '"+datapropKeyStr+"'"); 
        }
        
        String rangeDatatype = dps.getDatatypeURI();
        if( rangeDatatype == null ){
            log.debug("no range datatype uri set on data property statement when property's range datatype is "+prop.getRangeDatatypeURI()+" in autoCompleteDatapropForm.jsp");
            request.setAttribute("rangeDatatypeUriJson","");
        }else{
            log.debug("range datatype uri of ["+rangeDatatype+"] on data property statement in autoCompleteDatapropForm.jsp");
            request.setAttribute("rangeDatatypeUriJson",rangeDatatype);
        }
        String rangeLang = dps.getLanguage();
        if( rangeLang == null ) {
            log.debug("no language attribute on data property statement in autoCompleteDatapropForm.jsp");
            request.setAttribute("rangeLangJson","");
        }else{
            log.debug("language attribute of ["+rangeLang+"] on data property statement in autoCompleteDatapropForm.jsp");
            request.setAttribute("rangeLangJson", rangeLang);
        }
    } else {
        log.error("No incoming dataproperty statement attribute in autoCompleteDatapropForm.jsp");
    }
%>
<c:set var="dataLiteral" value="<%=prop.getLocalName()%>"/>

<v:jsonset var="n3ForEdit"  >
    ?subject ?predicate ?${dataLiteral}.
</v:jsonset>

<c:set var="editjson" scope="request">
  {
    "formUrl"              : "${formUrl}",
    "editKey"              : "${editKey}",
    "datapropKey"          : "<%=datapropKeyStr==null?"":datapropKeyStr%>",
    "urlPatternToReturnTo" : "/entity",

    "subject"   : ["subject",   "${subjectUriJson}" ],
    "predicate" : ["predicate", "${predicateUriJson}"],
    "object"    : ["${dataLiteral}","","DATAPROPHASH"],  
    
    "n3required"                : ["${n3ForEdit}"],
    "n3optional"                : [ ],
    "newResources"              : { },
    "urisInScope"               : { },
    "literalsInScope"           : { },
    "urisOnForm"                : [ ],
    "literalsOnForm"            : ["${dataLiteral}"],
    "filesOnForm"               : [ ],
    "sparqlForLiterals"         : { },
    "sparqlForUris"             : { },
    "sparqlForExistingLiterals" : { },
    "sparqlForExistingUris"     : { },
    "optionsForFields"          : { },
    "fields"                    : { "${dataLiteral}" : {
                                       "newResource"      : "false",
                                       "validators"       : ["nonempty"],
                                       "optionsType"      : "STRINGS_VIA_DATATYPE_PROPERTY",
                                       "literalOptions"   : [],
                                       "predicateUri"     : "${predicateUriJson}",
                                       "objectClassUri"   : "",
                                       "rangeDatatypeUri" : "${rangeDatatypeUriJson}"  ,
                                       "rangeLang"        : "${rangeLangJson}",
                                       "assertions"       : ["${n3ForEdit}"]
                                     }
                                  }
  }
</c:set>

<%
    if( log.isDebugEnabled()) log.debug(request.getAttribute("editjson"));

    EditConfiguration editConfig = new EditConfiguration((String)request.getAttribute("editjson"));
    EditConfiguration.putConfigInSession(editConfig, session);

    String formTitle   =""; // dont add local page variables to the request
    String submitLabel ="";

    if( datapropKeyStr != null && datapropKeyStr.trim().length() > 0  ) {
        Model model =  (Model)application.getAttribute("jenaOntModel");
        editConfig.prepareForDataPropUpdate(model,dps);
        formTitle   = "Change text for: <em>"+prop.getPublicName()+"</em>";
        submitLabel = "Save change";
    } else {
        formTitle   = "Add new entry for: <em>"+prop.getPublicName()+"</em>";
        submitLabel = "Save entry";
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
    $.getJSON("<c:url value="/dataservice"/>", {getN3EditOptionList:"1", field: "${dataLiteral}", editKey: key}, function(json){

    $("select#${dataLiteral}").replaceWith("<input type='hidden' id='${dataLiteral}' name='${dataLiteral}' /><input type='text' id='${dataLiteral}-entry' name='${dataLiteral}-entry' />");

    $("#${dataLiteral}-entry").autocomplete(json, {
            minChars: 1,
            width: 320,
            matchContains: true,
            mustMatch: 0,
            autoFill: false,
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
             $("input#${dataLiteral}-entry").attr("value", data[0]); // dump the string into the text box
             $("input#${dataLiteral}").attr("value", data[1]); // dump the uri into the hidden form input
           });
}
);
})
</script>

<h2><%=formTitle%></h2>
<form class="editForm" action="<c:url value="/edit/processDatapropRdfForm.jsp"/>" >
	<c:if test="${!empty predicate.publicDescription}">
    	<p class="propEntryHelpText">${predicate.publicDescription}</p>
	</c:if>
    <v:input type="select" id="${dataLiteral}"/>
    <v:input type="submit" id="submit" value="<%=submitLabel%>" cancel="true"/>
</form>
<jsp:include page="${postForm}"/>



