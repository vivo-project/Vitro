<%-- $This file is distributed under the terms of the license in LICENSE$ --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames"%>

<% request.setAttribute("requestedActions", SimplePermission.USE_MISCELLANEOUS_CURATOR_PAGES.ACTION); %>
<vitro:confirmAuthorization />

<%
    if (request.getParameter("execute") != null) {
     	OntModel ontModel = ModelAccess.on(getServletContext()).getOntModel(ModelNames.FULL_ASSERTIONS);
    	int results = doRemoval(ontModel);
    	request.setAttribute("removalCount", results);
    }

%>

<%!
    private int doRemoval(OntModel ontModel) {
	    int removedStmts = 0;
	    List<String> bnodeIds = new ArrayList<String>();
    	ontModel.enterCriticalSection(Lock.READ);
    	try {
    		Iterator<Restriction> restIt = ontModel.listRestrictions();
    		while(restIt.hasNext()) {
    			Restriction rest = restIt.next();
    			if (rest.isAnon()) {
    			    boolean bad = false;
    			    bad |= (rest.getPropertyValue(OWL.onProperty) == null);
    			    bad |= ( !(
    			    		   (rest.getPropertyValue(OWL.someValuesFrom) != null) ||
    			    		   (rest.getPropertyValue(OWL.allValuesFrom) != null)  ||
    			    	       (rest.getPropertyValue(OWL.hasValue) != null)  ||
    			    	       (rest.getPropertyValue(OWL.cardinality) != null)  ||
    			    	       (rest.getPropertyValue(OWL.minCardinality) != null)  ||
    			    	       (rest.getPropertyValue(OWL.maxCardinality) != null)
    			    	      )
    			           );
    			    if (bad) {
    			    	bnodeIds.add(rest.getId().toString());
    			    }
    			}
    		}
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
    	for (String id : bnodeIds) {
    		Model toRemove = describeBnode(id);
    		ontModel.enterCriticalSection(Lock.WRITE);
    		try {
    			ontModel.remove(toRemove);
    		} finally {
    			ontModel.leaveCriticalSection();
    		}
    		removedStmts += toRemove.size();
    	}
    	return removedStmts;
    }

    private Model describeBnode(String bnodeId) {
        String describeQueryStr =
            "PREFIX afn: <http://jena.apache.org/ARQ/function#> \n\n" +
            "DESCRIBE ?bnode \n" +
            "WHERE { \n" +
            "    FILTER(afn:bnode(?bnode) = \"" + bnodeId + "\")\n" +
            "}";

    	OntModel ontModel = ModelAccess.on(getServletContext()).getOntModel(ModelNames.FULL_ASSERTIONS);
        Model conceptDescription = ModelFactory.createDefaultModel();
        try {
            ontModel.enterCriticalSection(Lock.READ);
            Query describeQuery = QueryFactory.create(describeQueryStr, Syntax.syntaxARQ);
            QueryExecution qe = QueryExecutionFactory.create(describeQuery, ontModel);
            qe.execDescribe(conceptDescription);

            conceptDescription.add(ontModel.listStatements((Resource) null, (Property) null, ontModel.createResource(new AnonId(bnodeId))));
            return conceptDescription;
        } finally {
            ontModel.leaveCriticalSection();
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
<%@page import="org.apache.jena.rdf.model.AnonId"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.apache.jena.ontology.Restriction"%>
<%@page import="org.apache.jena.vocabulary.OWL"%><html>
<head>
    <title>Remove Bad Restrictions</title>
</head>
<body>
    <c:if test="${!empty requestScope.removalCount}">
        <p>${removalCount} statements removed</p>
    </c:if>

    <h1>Remove Bad Restrictions</h1>
    <form action="" method="post">
        <p><input name="execute" type="submit" value="Remove now"/></p>
    </form>
</body></html>
