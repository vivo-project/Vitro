<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.net.URLEncoder" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission" %>
<% request.setAttribute("requestedActions", SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION); %>
<vitro:confirmAuthorization />

<script type="text/javascript">
function init(){
	var modelType = document.ingesting.modelType.value;
	if(modelType == "CONFIGURATION"){
		document.showConfigurationModelsForm.submit.disabled="true";
	}
	else{
		document.showContentModelsForm.submit.disabled="true";
	}
}
</script>

    <h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Available Jena Models</h2>

    <table>
    <tr>
    <td>
     <form name="showContentModelsForm" action="ingest" method="get">
        <input type="hidden" name="action" value="contentModels"/>
        <input type="submit" name="submit" value="Main Store Models"/>
    </form>
    </td>
    <td>
    <form name="showConfigurationModelsForm" action="ingest" method="get">
        <input type="hidden" name="action" value="configModels"/>
        <input type="submit" name="submit" value="Configuration Models"/>
    </form>
    </td>
    </tr>
    </table>
    <form name="ingesting" action="ingest" method="get">
        <input type="hidden" name="action" value="createModel"/>
        <input type="hidden" name="modelType" value="${modelType}"/>
        <input type="submit" name="submit" value="Create Model"/>
    </form>
    
            Currently showing <font color="red">${infoLine}</font>
    <ul>
      <c:forEach var="modelName" items="${modelNames}">
          <li style="padding-bottom:2em;padding-top:2em;"> ${modelName}
            <table style="margin-left:2em;"><tr>
            <td>
            <form action="ingest" method="get">
                <input type="hidden" name="action" value="loadRDFData"/>
                <input type="hidden" name="modelName" value="${modelName}"/>
                <input type="hidden" name="modelType" value="${modelType}"/>
                <input type="submit" name="submit" value="load RDF data"/>
            </form>
            </td>
            <td>
            <c:url var="outputModelURL" value="ingest">
            	<c:param name="action" value="outputModel"/>
            	<c:param name="modelName" value="${modelName}"/>
            </c:url>
            <a href="${outputModelURL}">output model</a>
            </td>
            <td>
            <form action="ingest" method="post">
                <input type="hidden" name="action" value="clearModel"/>
                <input type="hidden" name="modelName" value="${modelName}"/>
                <input type="hidden" name="modelType" value="${modelType}"/>
                <input type="submit" name="submit" value="clear statements"/>
            </form>
            </td>
            <td>
            <form action="ingest" method="post">
                <input type="hidden" name="action" value="removeModel"/>
                <input type="hidden" name="modelName" value="${modelName}"/>
                <input type="hidden" name="modelType" value="${modelType}"/>
                <input type="submit" name="submit" value="remove"/>
            </form>
            </td>
            </tr>
            <tr>
            <td>
            <form action="ingest" method="post">
                <input type="hidden" name="action" value="attachModel"/>
                <input type="hidden" name="modelName" value="${modelName}"/>
                <input type="hidden" name="modelType" value="${modelType}"/>
                <input type="submit" name="submit" value="attach snapshot to ontology"/>
            </form>
            </td>
            <td>
            <form action="ingest" method="post">
                <input type="hidden" name="action" value="detachModel"/>
                <input type="hidden" name="modelName" value="${modelName}"/>
                <input type="hidden" name="modelType" value="${modelType}"/>
                <input type="submit" name="submit" value="detach snapshot from ontology"/>
            </form>
            </td>
            <td>
			<form action="ingest" method="get">
			    <input type="hidden" name="action" value="permanentURI" />
			    <input type="hidden" name="modelName" value="${modelName}" /> 
				<input type="hidden" name="modelType" value="${modelType}"/>
				<input type="submit" name="submit" value="generate permanent URIs" /></form>
			</td>
            <td>&nbsp;</td>
            </tr>
            </table>
        </li>
      </c:forEach>
    </ul>

