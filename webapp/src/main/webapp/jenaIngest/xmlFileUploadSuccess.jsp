<%-- $This file is distributed under the terms of the license in LICENSE$ --%>

<%@ page import="org.apache.jena.rdf.model.ModelMaker" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.net.URLEncoder" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission" %>
<% request.setAttribute("requestedActions", SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION); %>
<vitro:confirmAuthorization />

<p>Uploaded XML files and converted to RDF.</p>
<p>Loaded <%= request.getAttribute("statementCount") %> statements to the model <%= request.getAttribute("targetModel") %>.</p>

<h2><a class="ingestMenu" href="ingest">Ingest Menu</a></h2>
