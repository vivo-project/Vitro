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

    <h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Convert Blank Nodes to Named Resources</h2>

    <form action="ingest" method="get"i>
        <input type="hidden" name="action" value="renameBNodes"/>

    <h3>Select URI prefix</h3>
   
	<p>URIs will be constructed from the following string concatenated with a random integer:</p>
	<input type="text" style="width:65%;" name="namespaceEtcStr"/> 

    <p/>

    <h3>Select Source Models</h3>

    <ul>

<%
    for (Iterator it = maker.listModels(); it.hasNext(); ) {
	String modelName = (String) it.next();
        %> <li> <input type="checkbox" name="sourceModelName" value="<%=modelName%>"/><%=modelName%></li>
        <%    
    }
%>
    </ul>

    <h3>Select Destination Model</h3>

    <select name="destinationModelName">
        <option value="vitro:baseOntModel">webapp assertions</option>
<%
    for (Iterator it = maker.listModels(); it.hasNext(); ) {
	String modelName = (String) it.next();
        %> <option value="<%=modelName%>"/><%=modelName%></option>
        <%    
    }
%>   
    </select>

    <input id="submit" type="submit" value="Rename Blank Nodes"/>
