<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages" %>
<% request.setAttribute("requestedActions", new UseAdvancedDataToolsPages()); %>
<vitro:confirmAuthorization />

<%@page import="java.util.List"%><h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Connect to Jena Database</h2>

    <form action="ingest" method="post">
        <input type="hidden" name="action" value="connectDB"/>

    <label for="JDBC URL">JDBC URL</label>
    <input type="text" style="width:80%;" name="jdbcUrl" value="jdbc:mysql://localhost/"/>
 
    <label for="username">Username</label>
    <input type="text" name="username"/>
    
    <label for="password">Password</label>
    <input type="password" name="password" class="block"/>

    <input id="tripleStoreRDB" name="tripleStore" type="radio" checked="checked" value="RDB"/>
    <label for="tripleStoreRDB" class="inline">Jena RDB</label>
    
    <input id="tripleStoreSDB" name="tripleStore" type="radio" value="SDB"/>
    <label for="tripleStoreRDB" class="inline">Jena SDB (hash layout)</label>
        
    <label for="database type">Database type</label>
    <select name="dbType">
        <c:forEach items="${requestScope.dbTypes}" var="typeName">
            <c:choose>
                <c:when test="${typeName eq 'MySQL'}">
                <option value="${typeName}" selected="selected">${typeName}</option>
                </c:when>
                    <c:otherwise>
                        <option value="${typeName}">${typeName}</option>
                    </c:otherwise>
            </c:choose>
        </c:forEach>
    </select>


    <input class="submit" type="submit" value="Connect Database" />
