<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission" %>
<% request.setAttribute("requestedActions", SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION); %>
<vitro:confirmAuthorization />

<script type="text/javascript" src="js/jquery.js"></script>

<h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Change Namespace of Resources</h2>

<p>This tool will change all resources in the supplied "old namespace" 
to be in the "new namespace."  Additionally, if the local names do not
already follow the established "n" + random integer naming convention, 
they will be updated to this format.</p>

<p>This tool operates on the main web application model only, not on any 
   of the additional Jena models.</p>

<c:if test="${!empty errorMsg}">
    <p class="notice">${errorMsg}</p>
</c:if>

<form id="takeuri" action="ingest" method="get">
<input type="hidden" name="action" value="renameResource"/>
<p>Old namespace: <input id="uri1" type="text" size="52" name="oldNamespace" value="${oldNamespace}" /></p>
<p>New namespace: <input id="uri2" type="text" size="52" name="newNamespace" value="${newNamespace}" /></p>
<p><input id="submit" type="submit" name="submit" value="Change namespace" /></p>
</form>