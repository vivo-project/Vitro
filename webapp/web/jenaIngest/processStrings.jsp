<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.ontology.Individual" %>
<%@ page import="com.hp.hpl.jena.ontology.OntModel" %>
<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker" %>
<%@ page import="com.hp.hpl.jena.shared.Lock" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission" %>
<% request.setAttribute("requestedActions", SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION); %>
<vitro:confirmAuthorization />

    <h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Process Property Value Strings</h2>

    <form action="ingest" method="get"i>
        <input type="hidden" name="action" value="processStrings"/>

    <input type="text" style="width:80%;" name="className"/>
    <p>String processor class</p>
    
    <input type="text" name="methodName"/>
    <p>String processor method</p>

    <input type="text" name="propertyName"/>
    <p>Property URI</p>

    <input type="text" name="newPropertyName"/>
    <p>New Property URI</p>

    <select name="destinationModelName">
    <c:forEach var="modelName" items="${modelName}">
        <option value="${modelName}"/>${modelName}</option>
    </c:forEach>
    </select>
    <input type="checkbox" name="processModel" value="TRUE"/> apply changes directly to this model
    <p>model to use</p>
   
    <select name="additionsModel">
		<option value="">none</option>
		<forEach var="modelName" items="${modelNames}">
            <option value="${modelName}">${modelName}</option>
        </forEach>
	</select>
    <p>model in which to save added statements</p>

    <select name="retractionsModel">
		<option value="">none</option>
		<c:forEach var="modelName" items="${modelNames}">
            <option value="${modelName}">${modelName}</option>
        </c:forEach>
	</select>
    <p>model in which to save retracted statements</p>

    <input class="submit" type="submit" value="Process property values"/>
