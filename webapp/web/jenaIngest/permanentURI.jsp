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

<h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Assign Permanent URIs To Resources</h2>

<p>This tool will rename the resources in the selected model to with new
randomly-generated URIs following the pattern used in the main application.  
The tool will generate URIs that are not already in use in the main web 
application model. Statements using the newly-renamed resources will be 
written to the "model to save."</p> 

<p>The permanent URIs may be generated in an arbitrary "new namespace for 
resources."  Otherwise, the "use default namespace" option will generate
URIs exactly of the form created through the GUI interface.</p>

<form action="ingest" method="get" >
<input type="hidden" name="oldModel" value="${modelName}"/>
<input type="hidden" name="action" value="permanentURI" />
<p>Current namespace of resources  
<select name=oldNamespace>
<%List namespaces = (List)request.getAttribute("namespaceList");
if(namespaces != null) {
	Iterator namespaceItr = namespaces.iterator();
	Integer count = 0;
	while (namespaceItr.hasNext()){
		String namespaceText = (String) namespaceItr.next();
		%>
        <option value="<%=namespaceText%>"><%=namespaceText%></option>
<%  }
}%>
</select></p>

<p>Model in which to save results  <select name="newModel">
<c:forEach var="modelName" items="${modelNames}">
    <option value="${modelName}">${modelName}</option>
</c:forEach>

</select></p>
<p>New namespace for resources  <input type="text" name="newNamespace" /></p>
<p>Use default namespace ${defaultNamespace}  <input type="checkbox" name="defaultNamespace" value ="${defaultNamespace}"/>
</p>

<p><input class="submit" type="submit" name="submit" value="Generate URIs" /></p>
</form>
