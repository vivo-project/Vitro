<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

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

    <p>Enter Web-accessible URL of document containing RDF to add or remove:</p>
    <p><input name="rdfUrl" type="text" style="width:67%;" value="<c:out value='${param.rdfUrl}'/>"/></p>
    
    <p>Or upload a file from your computer: </p>
    <p><input type="file" name="rdfStream" size="60"/> </p>
    
    <ul style="list-style-type:none;">
        <li><input type="radio" name="mode" value="directAddABox" checked="checked"/>add instance data (supports large data files)</li> 
        <li><input type="radio" name="mode" value="add"/>add mixed RDF (instances and/or ontology)</li> 
        <li><input type="radio" name="mode" value="remove"/>remove mixed RDF (instances and/or ontology)</li>
    </ul>

    <select name="language">
        	<option value="RDF/XML">RDF/XML</option>
        	<option value="N3">N3</option>
        	<option	value="N-TRIPLE">N-Triples</option>
            <option value="TTL">Turtle</option>
    </select>
    <p><input type="checkbox" name="makeClassgroups" value="true"/> create classgroups automatically</p>

    <p><input id="submit" type="submit" name="submit" value="submit"/></p>     
</form>

</div>
