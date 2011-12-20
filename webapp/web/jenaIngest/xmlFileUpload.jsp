<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.net.URLEncoder" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages" %>
<% request.setAttribute("requestedActions", new UseAdvancedDataToolsPages()); %>
<vitro:confirmAuthorization />

    <h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Load XML and convert to RDF</h2>

    <form action="jenaXmlFileUpload" method="post" enctype="multipart/form-data">
    
    <input type="file" name="xmlfile"/>

    <p>XML file</p>
    
	<select name="targetModel">
        <option value="vitro:baseOntModel">webapp assertions</option>
        <c:forEach var="modelName" items="${modelNames}">
            <option value="${modelName}">${modelName}</option>
        </c:forEach>
        </select>

        <p>Destination model</p>

        <input id="submit" type="submit" name="submit" value="upload XML and convert to RDF"/>
    </form>
    
