<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
    
<%@page import="java.util.List"%><h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Connect to Jena Database</h2>

    <form action="ingest" method="post">
        <input type="hidden" name="action" value="connectDB"/>

    
	<input type="text" style="width:80%;" name="jdbcUrl" value="jdbc:mysql://localhost/"/>
    <p>JDBC URL</p>
 
    <input type="text" name="username"/>
    <p>username</p>

    <input type="password" name="password"/>
    <p>password</p>


		<input id="tripleStoreRDB" name="tripleStore" type="radio" checked="checked" value="RDB"/>
			<label for="tripleStoreRDB">Jena RDB</label>
		<input id="tripleStoreSDB" name="tripleStore" type="radio" value="SDB"/>
			<label for="tripleStoreRDB">Jena SDB (hash layout)</label>
    
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

    <p>database type</p>

    <input id="submit" type="submit" value="Connect Database" />
