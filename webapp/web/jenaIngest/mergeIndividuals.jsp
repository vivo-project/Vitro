<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages" %>
<% request.setAttribute("requestedActions", new UseAdvancedDataToolsPages()); %>
<vitro:confirmAuthorization />

<%

    ModelMaker maker = (ModelMaker) request.getSession().getAttribute("vitroJenaModelMaker");
    if (maker == null) {
	maker = (ModelMaker) getServletContext().getAttribute("vitroJenaModelMaker");
    }

%>
<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jenaIngest/ingestUtils.js"></script>

<h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Merge Individuals</h2>

<p>This tool allows two individuals with different URIs to be collapsed into a 
   single URI.  Any statements using the "duplicate individual URI" will be 
   rewritten using the "primary individual URI."  If there are multiple 
   statements for a property that can have only a single value, the extra
   statements will be retracted from the model and offered for download.</p>
<p>This tool operates on the main web application model only, not on any 
   of the additional Jena models.</p> 

<form id="takeuri" action="ingest" method="get">
<input type="hidden" name="action" value="mergeIndividuals"/>
<table>
<tr>
    <td>Primary individual URI</td><td><input id="uri1" type="text" size="52" name="uri1"/></td>
</tr>
<tr>
    <td>Duplicate individual URI</td><td><input id="uri2" type="text" size="52" name="uri2"/></td>
</tr>
</table>
<input class="submit"type="submit" name="submit" value="Merge individuals" /></p>
</form>

