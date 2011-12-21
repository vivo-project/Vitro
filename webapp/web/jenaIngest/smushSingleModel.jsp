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
  
    <h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Smush Resources</h2>

    <form action="ingest" method="get"i>
        <input type="hidden" name="action" value="smushSingleModel"/>

    <h3>URI of Property with which To Smush
	<input type="text" style="width:40%;" name="propertyURI"/> 

    <h3>Select Source Models</h3>

    <ul>
        <li> <input type="checkbox" name="sourceModelName" value="vitro:baseOntModel"/>webapp assertions</li>
	    <c:forEach var="modelName" items="${modelNames}">
	         <li> <input type="checkbox" name="sourceModelName" value="${modelName}"/>${modelName}</li>
	    </c:forEach>
    </ul>

    <h3>Select Destination Model</h3>

    <select name="destinationModelName">
        <option value="vitro:baseOntModel">webapp assertions</option>
        <c:forEach var="modelName" items="${modelNames}">
            <option value="${modelName}"/>${modelName}</option>
        </c:forEach>
    </select>

    <input id="submit" type="submit" value="Smush Resources"/>
