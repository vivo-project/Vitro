<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.ontology.Individual" %>
<%@ page import="com.hp.hpl.jena.ontology.OntModel" %>
<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker" %>
<%@ page import="com.hp.hpl.jena.shared.Lock" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>

<%

    ModelMaker maker = (ModelMaker) request.getSession().getAttribute("vitroJenaModelMaker");
    if (maker == null) {
        maker = (ModelMaker) getServletContext().getAttribute("vitroJenaModelMaker");
    }

%>
    <h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Split Property Value Strings into Multiple Property Values</h2>

    <form action="ingest" method="get">
        <input type="hidden" name="action" value="splitPropertyValues"/>
    <h3>Select Source Models</h3>
    <ul>
		  <li><input type="checkbox" name="sourceModelName" value="vitro:jenaOntModel"/>webapp model</li>
		  <li><input type="checkbox" name="sourceModelName" value="vitro:baseOntModel"/>webapp assertions</li>
<%
    for (Iterator it = maker.listModels(); it.hasNext(); ) {
	String modelName = (String) it.next();
        %> <li> <input type="checkbox" name="sourceModelName" value="<%=modelName%>"/><%=modelName%></li>
        <%    
    }
%>
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
<%
    for (Iterator it = maker.listModels(); it.hasNext(); ) {
	String modelName = (String) it.next();
        %> <option value="<%=modelName%>"/><%=modelName%></option>
        <%    
    }
%>   
    </select>

    <input id="submit" type="submit" value="Split Values"/>
