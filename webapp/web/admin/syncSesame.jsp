<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>


<%@page import="edu.cornell.mannlib.vitro.webapp.utils.jena.SesameSyncUtils"%>
<%@page import="com.hp.hpl.jena.rdf.model.ModelFactory"%>
<%@page import="com.hp.hpl.jena.shared.Lock"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.jena.JenaModelUtils"%>
<%@page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.util.Properties"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousAdminPages" %>
<% request.setAttribute("requestedActions", new UseMiscellaneousAdminPages()); %>
<vitro:confirmAuthorization />

<%!

    final String SESAME_PROPS_PATH = "/WEB-INF/classes/sesame.sync.properties" ;
    final String SESAME_SERVER = "vitro.sesame.server" ;
    final String SESAME_REPOSITORY = "vitro.sesame.repository" ;
    final String SESAME_CONTEXT = "vitro.sesame.context" ;
    
    final String USER_SPARQL_QUERY =
    	"PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
        "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n\n" +
        "DESCRIBE ?user WHERE { \n " +
        "    ?user rdf:type vitro:User \n" +
        "}";

%>

<%
    long startTime = System.currentTimeMillis();

    Properties sesameProperties = new Properties();
    InputStream propStream = getServletContext().getResourceAsStream(SESAME_PROPS_PATH);
    if (propStream == null) {
    	response.setStatus(500, "Sesame properties not found at " + SESAME_PROPS_PATH);
    	return;
    }
    sesameProperties.load(propStream);
    String sesameLocation = sesameProperties.getProperty(SESAME_SERVER);
    if (sesameLocation == null) {
    	response.setStatus(500, "Missing property " + SESAME_SERVER);
    }
    String sesameRepository = sesameProperties.getProperty(SESAME_REPOSITORY);
    if (sesameRepository == null) {
    	response.setStatus(500, "Missing property " + SESAME_REPOSITORY);
    }
    String contextId = sesameProperties.getProperty(SESAME_CONTEXT);
    
    Model fullModel = (Model) getServletContext().getAttribute(JenaBaseDao.JENA_ONT_MODEL_ATTRIBUTE_NAME);
    // Copy the model to avoid locking the main model during sync.  Assumes enough memory.
    Model copyModel = ModelFactory.createDefaultModel();
    fullModel.enterCriticalSection(Lock.READ);
    try {
    	copyModel.add(fullModel); 
    } finally {
    	fullModel.leaveCriticalSection();
    }
    
    Model userDataToRetract = ModelFactory.createDefaultModel();
    Query userDataQuery = QueryFactory.create(USER_SPARQL_QUERY);
    QueryExecution qe = QueryExecutionFactory.create(userDataQuery, copyModel);
    qe.execDescribe(userDataToRetract);
    copyModel.remove(userDataToRetract);
    
    System.out.println("Not sharing " + userDataToRetract.size() + " statements of user data");
    
    System.out.println("Using Sesame server at " + sesameLocation);
    System.out.println("Using Sesame repository at " + sesameRepository);
    System.out.println("Using context " + contextId);
    
    try {
        (new SesameSyncUtils()).writeModelToSesameContext(copyModel, sesameLocation, sesameRepository, contextId);
    } catch (Throwable t) {
    	t.printStackTrace();
    	throw new Error(t);
    }
    
    System.out.println((System.currentTimeMillis() - startTime) + " ms to sync");

%>


<%@page import="com.hp.hpl.jena.rdf.model.StmtIterator"%>
<%@page import="com.hp.hpl.jena.rdf.model.Statement"%>
<%@page import="com.hp.hpl.jena.query.Query"%>
<%@page import="com.hp.hpl.jena.query.QueryFactory"%>
<%@page import="com.hp.hpl.jena.query.QueryExecution"%>
<%@page import="com.hp.hpl.jena.query.QueryExecutionFactory"%><html>
    <head>
        <title>Sync successful</title>
    </head>
</html>
