<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%

    ModelMaker maker = (ModelMaker) request.getSession().getAttribute("vitroJenaModelMaker");
    if (maker == null) {
	maker = (ModelMaker) getServletContext().getAttribute("vitroJenaModelMaker");
    }

%>

<p><a href="ingest">Ingest Home</a></p>

<h2>Assign Permanent URI To Individuals</h2>
<form action="ingest" method="get" >
<%String modelName = (String)request.getAttribute("modelName"); %>
<input type="hidden" name="oldModel" value="<%=modelName%>"/>
<input type="hidden" name="action" value="permanentURI" />
<p>Namespace of resources  <select name=oldNamespace><%List namespaces = (List)request.getAttribute("namespaceList");
if(namespaces != null){
	Iterator namespaceItr = namespaces.iterator();
	Integer count = 0;
	while (namespaceItr.hasNext()){
		String namespaceText = (String) namespaceItr.next();
		%>
<option value="<%=namespaceText%>"><%=namespaceText%></option><%}}%>
</select></p>

<p>Model to save  <select name=newModel>
<%
    for (Iterator it = maker.listModels(); it.hasNext(); ) {
	String modelNames = (String) it.next();
        %>
<option value="<%=modelNames%>"><%=modelNames%></option>
<%    
    }
%>
</select></p>
<p>New Namespace for resources  <input type="text" name="newNamespace" /></p>
Or <%String defaultNamespace = (String)request.getAttribute("defaultNamespace");%>
<p>Choose Default Namespace <%=defaultNamespace%>  <input type="checkbox" name="defaultNamespace" value ="<%=defaultNamespace%>"/>
</p>

<p><input type="submit" name="submit" value="submit" /></p>
</form>