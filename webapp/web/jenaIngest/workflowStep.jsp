<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.ontology.Individual" %>
<%@ page import="com.hp.hpl.jena.ontology.OntModel" %>
<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker" %>
<%@ page import="com.hp.hpl.jena.shared.Lock" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages" %>
<% request.setAttribute("requestedActions", new UseAdvancedDataToolsPages()); %>
<vitro:confirmAuthorization />

    <h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Execute RDF-Encoded Ingest Workflow</h2>

    <form action="ingest" method="get"i>
        <input type="hidden" name="action" value="executeWorkflow"/>

    <h3>Choose a Workflow Step at Which To Start</h3>
   
        <input type="hidden" name="workflowURI" value="${param.workflowURI}"/>

		<select name="workflowStepURI">
             
        <%
              OntModel jenaOntModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
              jenaOntModel.enterCriticalSection(Lock.READ);
              try {
                  List workflowSteps  = (List) request.getAttribute("workflowSteps");
	          for (Iterator it = workflowSteps.iterator(); it.hasNext();)  {
	              Individual workflowStep = (Individual) it.next();
                      String workflowStepURI = workflowStep.getURI();
                      String workflowStepLabel = workflowStep.getLabel(null);
					  String workflowStepString = (workflowStepLabel != null) ? workflowStepLabel : workflowStepURI;
                      %> <option value="<%=workflowStepURI%>"><%=workflowStepString%></option> <%
                  }
              } finally {
                  jenaOntModel.leaveCriticalSection();
	      }
        %>
        </select>
  
    <input id="submit" type="submit" value="Execute Workflow"/>
