<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames"%>
<% request.setAttribute("requestedActions", SimplePermission.USE_MISCELLANEOUS_CURATOR_PAGES.ACTION); %>
<vitro:confirmAuthorization />

<%
    String resourceURIStr = request.getParameter("resourceURI");
    if (resourceURIStr != null) {
        
        String describeQueryStr = 
            "DESCRIBE <" + resourceURIStr + ">";
            
    	OntModel ontModel = ModelAccess.on(getServletContext()).getOntModel(ModelNames.FULL_ASSERTIONS);
        Model resourceDescription = ModelFactory.createDefaultModel();
        try {
            ontModel.enterCriticalSection(Lock.READ);
            Query describeQuery = QueryFactory.create(describeQueryStr, Syntax.syntaxARQ);
            QueryExecution qe = QueryExecutionFactory.create(describeQuery, ontModel);
            qe.execDescribe(resourceDescription);
            
            resourceDescription.add(ontModel.listStatements((Resource) null, (Property) null, ontModel.getResource(resourceURIStr)));
            
        } finally {
            ontModel.leaveCriticalSection();
        }
        
        
        
        List<String> actionStrList = (request.getParameterValues("action") != null)
           ? Arrays.asList(request.getParameterValues("action"))
           : new ArrayList<String>();
        if (actionStrList.contains("remove")) {
            try {
                ontModel.enterCriticalSection(Lock.WRITE);
                ontModel.remove(resourceDescription);
            } finally {
                ontModel.leaveCriticalSection();
            }
        }
        if (actionStrList.contains("describe")) {
            resourceDescription.write(response.getOutputStream(), "TTL");
            return;
        }
        
    }

%>


<%@page import="org.apache.jena.ontology.OntModel"%>
<%@page import="org.apache.jena.shared.Lock"%>
<%@page import="org.apache.jena.query.Syntax"%>
<%@page import="org.apache.jena.query.Query"%>
<%@page import="org.apache.jena.query.QueryFactory"%>
<%@page import="org.apache.jena.query.QueryExecutionFactory"%>
<%@page import="org.apache.jena.rdf.model.ModelFactory"%>
<%@page import="org.apache.jena.rdf.model.Model"%>
<%@page import="org.apache.jena.query.QueryExecution"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.apache.jena.rdf.model.Resource"%>
<%@page import="org.apache.jena.rdf.model.Property"%>
<%@page import="org.apache.jena.rdf.model.AnonId"%><html>
<head>
    <title>Anonymous Concept Repair Tools</title>
</head>
<body>
    <h1>Remove resource using DESCRIBE</h1>
    <form action="" method="post">
        <p>Resource URI: <input name="resourceURI"/></p>
        <p><input type="checkbox" name="action" value="describe"/> describe resource</p>
        <p><input type="checkbox" name="action" value="remove"/> remove resource</p> 
        <p><input type="submit" value="Perform action"/></p>
    </form>
</body></html>    
    