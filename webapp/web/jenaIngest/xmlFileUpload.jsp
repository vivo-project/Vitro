<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.net.URLEncoder" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<%

    ModelMaker maker = (ModelMaker) request.getSession().getAttribute("vitroJenaModelMaker");
    if (maker == null) {
		maker = (ModelMaker) getServletContext().getAttribute("vitroJenaModelMaker");
    }

%>

    <h3><a href="ingest">Ingest Home</a></h3>
    

    <form action="jenaXmlFileUpload" method="post" enctype="multipart/form-data">
    
    <input type="file" name="xmlfile"/>

    <p>XML file</p>
    
	<select name="targetModel">
        <option value="vitro:baseOntModel">webapp assertions</option>
<%  for (Iterator it = maker.listModels(); it.hasNext(); ) {
	String modelName = (String) it.next();        %> 
	  <option value="<%=modelName%>"><%= modelName %></option>                
<% } %>
        </select>

        <p>Destination model</p>

        <input id="submit" type="submit" name="submit" value="upload XML and convert to RDF"/>
    </form>
    
