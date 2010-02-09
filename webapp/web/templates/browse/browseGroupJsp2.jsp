<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<div id="content" class="siteMap">
    
	<c:choose>
	    <c:when test="${!empty message}">
	        <p>${message}</p>
	    </c:when>
	
        <c:otherwise>
            <c:forEach var="group" items="${classGroups}">
                <%--  Doesn't work when classGroups is a List of VClassGroups: <h2>${group.publicName}</h2> --%>
                <h2>${group}</h2>
                <ul>
	                <c:forEach var="vclass" items="${group}">
	                    <c:url var="url" value="entitylist">
	                        <c:param name="vclassId" value="${vclass.URI}" />
	                    </c:url>
	                    <li><a href="${url}">${vclass.name}</a> (${vclass.entityCount})</li> 
	                </c:forEach> 
                </ul>
            </c:forEach>
        </c:otherwise>
	</c:choose>

</div> <!-- content -->
