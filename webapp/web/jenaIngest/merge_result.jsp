<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@page import="edu.cornell.mannlib.vitro.webapp.utils.jena.JenaIngestUtils.MergeResult"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages" %>
<% request.setAttribute("requestedActions", new UseAdvancedDataToolsPages()); %>
<vitro:confirmAuthorization />

<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>

<h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Merge Individuals</h2>

<%
    MergeResult resultObj = (MergeResult) request.getAttribute("result");
    String result = resultObj.getResultText();
%>

<p><b><%=result%></b></p>

<%if(!result.equals("resource 1 not present") && !result.equals("resource 2 not present")){ 
if(!result.equals("No statements merged") && !result.endsWith("statements.")){%>
<p>Download non-mergeable statements.</p>
<form action="ingest" method="get">
<input type="hidden" name="action" value="mergeResult"/>
<input class="submit"  type="submit" name="Download" value="Download" ></input>  
</form>        
<%}} %>