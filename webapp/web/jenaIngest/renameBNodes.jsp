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

    <form action="ingest" method="get">
        <input type="hidden" name="action" value="renameBNodes"/>

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

    <input class="submit" type="submit" value="Next Step"/>
    </form>
