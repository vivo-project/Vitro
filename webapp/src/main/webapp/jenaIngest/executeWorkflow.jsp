<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.ontology.Individual" %>
<%@ page import="com.hp.hpl.jena.ontology.OntModel" %>
<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess"%>
<%@ page import="com.hp.hpl.jena.shared.Lock" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission" %>
<% request.setAttribute("requestedActions", SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION); %>
<vitro:confirmAuthorization />

    <h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Execute RDF-Encoded Ingest Workflow</h2>

    <form action="ingest" method="get"i>
        <input type="hidden" name="action" value="executeWorkflow"/>

    <h3>Workflow</h3>
   
        <select name="workflowURI">
             
        <%
              OntModel jenaOntModel = ModelAccess.on(getServletContext()).getOntModel();
              jenaOntModel.enterCriticalSection(Lock.READ);
              try {
                  List savedQueries = (List) request.getAttribute("workflows");
	          for (Iterator it = savedQueries.iterator(); it.hasNext();)  {
	              Individual savedQuery = (Individual) it.next();
                      String queryURI = savedQuery.getURI();
                      String queryLabel = savedQuery.getLabel(null);
                      %> <option value="<%=queryURI%>"><%=queryLabel%></option> <%
                  }
              } finally {
                  jenaOntModel.leaveCriticalSection();
	      }
        %>
        </select>
  
    <input class="submit" type="submit" value="Next &gt;"/>
