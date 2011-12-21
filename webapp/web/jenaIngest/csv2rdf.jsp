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

    <h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Convert CSV to RDF</h2>

    <form action="csv2rdf" method="post" enctype="multipart/form-data">
        <input type="hidden" name="action" value="csv2rdf"/>

	<p><input type="radio" name="separatorChar" value="comma" checked="checked"/> comma separated 
	<input type="radio" name="separatorChar" value="tab"/> tab separated </p>

    <input type="text" style="width:80%;" name="csvUrl"/>
    <p>CSV file URL (e.g. "file:///")</p>
    
    <p>Or upload a file from your computer:</p>
    <p><input type="file" name="filePath" /></p>

    <p/>
    <p>This tool will automatically generate a mini ontology to represent the 
    data in the CSV file.  A property will be produced for each column in the 
    spreadsheet, based on the text in the header for that column.</p><p>In what 
    namespace should these properties be created?</p>
	<input type="text" name="tboxNamespace"/>
    <p>Namespace in which to generate properties</p>

<!-- 
 <input type="checkbox" name="discardTbox"/> do not add TBox or RBox to result model
-->

    <p>
    <p>Each row in the spreadsheet will produce a resource.  Each of these
    resources will be a member of a class in the namespace selected above.</p>  
    <p>What should the local name of this class be? This is normally a word or two 
    in "camel case" starting with an uppercase letter.  (For example, if the 
    spreadsheet represents a list of faculty members, you might enter 
    "FacultyMember" on the next line.)</p> 
    <input type="text" name="typeName"/>
    <p>Class Local Name for Resources</p>

    <select name="destinationModelName">
        <option value="vitro:baseOntModel">webapp assertions</option>
        <c:forEach var="modelName" items="${modelNames}">
           <option value="${modelName}">${modelName}</option>
        </c:forEach>
        <option value="">(none)</option>
    </select>
    <p>Model in which to save the converted spreadsheet data</p>

   <select name="tboxDestinationModelName">
        <option value="vitro:baseOntModel">webapp assertions</option>
        <c:forEach var="modelName" items="${modelNames}">
           <option value="${modelName}">${modelName}</option>
        </c:forEach>
   <option value="">(none)</option>
    </select>
    <p>Model in which to save the automatically-generated ontology</p>

    <p/>
    <p>The data in the CSV file will initially be represented using blank
    nodes (RDF resources without URIs).  You will choose how to assign
    URIs to these resources in the next step.</p>

    <input class="submit" type="submit" value="Next Step"/>
