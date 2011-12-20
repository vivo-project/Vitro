<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages" %>
<% request.setAttribute("requestedActions", new UseAdvancedDataToolsPages()); %>
<vitro:confirmAuthorization />

<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jenaIngest/ingestUtils.js"></script>

<h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Merge Resources</h2>

<p>This tool allows two resources with different URIs to be collapsed into a 
   single URI. Any statements using the "duplicate resource URI" will be 
   rewritten using the "primary resource URI."  If there are multiple 
   statements for a property that can have only a single value, the extra
   statements will be retracted from the model and offered for download.</p>
<p>This tool operates on the main web application model only, not on any 
   of the additional Jena models.</p> 

<form id="takeuri" action="ingest" method="get">
    <input type="hidden" name="action" value="mergeResources"/>
    <table>
    <tr>
        <td>Primary resource URI</td><td><input id="uri1" type="text" size="52" name="uri1"/></td>
    </tr>
    <tr>
        <td>Duplicate resource URI</td><td><input id="uri2" type="text" size="52" name="uri2"/></td>
    </tr>
    </table>
    <input type="checkbox" name="usePrimaryLabelOnly" value="Use Primary Label Only">Retain rdfs:labels only from Primary Resource</input>

    <p><input class="submit"type="submit" name="submit" value="Merge resources" /></p>
</form>
