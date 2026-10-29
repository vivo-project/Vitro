<%-- $This file is distributed under the terms of the license in LICENSE$ --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission" %>
<% request.setAttribute("requestedActions", SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION); %>
<vitro:confirmAuthorization />

<div class="staticPageBackground">

<h2>Add or Remove RDF Data</h2>

<form action="uploadRDF" method="post"  enctype="multipart/form-data" >

<c:if test="${!empty param.errMsg}">
    <p><strong class="warning">${errMsg}</strong></p>
</c:if>

    <p><label for="rdf-url">Enter Web-accessible URL of document containing RDF to add or remove:</label></p>
    <p><input id="rdf-url" name="rdfUrl" type="text" style="width:67%;" value="<c:out value='${param.rdfUrl}'/>"/></p>

    <p><label for="rdf-file">Or upload a file from your computer:</label></p>
    <p><input type="file" id="rdf-file" name="rdfStream" size="60"/> </p>

    <ul style="list-style-type:none;">
        <li><label class="inline"><input type="radio" name="mode" value="directAddABox" checked="checked"/>add instance data (supports large data files)</label></li>
        <li><label class="inline"><input type="radio" name="mode" value="add"/>add mixed RDF (instances and/or ontology)</label></li>
        <li><label class="inline"><input type="radio" name="mode" value="remove"/>remove mixed RDF (instances and/or ontology)</label></li>
    </ul>

    <label class="inline" for="rdf-language">RDF format</label>
    <select id="rdf-language" name="language">
        	<option value="RDF/XML">RDF/XML</option>
        	<option value="N3">N3</option>
        	<option	value="N-TRIPLE">N-Triples</option>
            <option value="TTL">Turtle</option>
    </select>
    <p><label class="inline"><input type="checkbox" name="makeClassgroups" value="true"/> create classgroups automatically</label></p>

    <p><input id="submit" type="submit" name="submit" value="submit" aria-label="submit"/></p>
</form>

</div>
