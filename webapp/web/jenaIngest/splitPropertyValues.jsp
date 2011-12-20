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
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages" %>
<% request.setAttribute("requestedActions", new UseAdvancedDataToolsPages()); %>
<vitro:confirmAuthorization />

    <h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Split Property Value Strings into Multiple Property Values</h2>

    <form action="ingest" method="get">
        <input type="hidden" name="action" value="splitPropertyValues"/>
    <h3>Select Source Models</h3>
    <ul>
		  <li><input type="checkbox" name="sourceModelName" value="vitro:jenaOntModel"/>webapp model</li>
		  <li><input type="checkbox" name="sourceModelName" value="vitro:baseOntModel"/>webapp assertions</li>
          <c:forEach var="modelName" items="${modelNames}">
              <li> <input type="checkbox" name="sourceModelName" value="${modelName}"/>${modelName}</li>
          </c:forEach>
    </ul>

	
	<input type="text" name="propertyURI"/>
    <p>Property URI for which Values Should Be Split</p>	

	<input type="text" name="splitRegex"/>
    <p>Regex Pattern on which To Split</p>
	
	<input type="text" name="newPropertyURI"/>
    <p>Property URI To Be Used with the Newly-Split Values</p>
	
	<h3></h3>
	
	<p>
	<input type="checkbox" name="trim" value="true"/> trim bordering whitespace
	</p>
	
    <h3>Select Destination Model</h3>

    <select name="destinationModelName">
        <c:forEach var="modelName" items="${modelNames}">
            <option value="${modelName}"/>${modelName}</option>
        </c:forEach>
    </select>

    <input id="submit" type="submit" value="Split Values"/>
