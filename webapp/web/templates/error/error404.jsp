<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isErrorPage="true" %>

<% if ( pageContext.getErrorData().getRequestURI().indexOf("/images/") < 0 ) {
    request.setAttribute("bodyJsp", "/templates/error/error404content.jsp");
    request.setAttribute("title", "Not Found");
%>

<jsp:forward page="/templates/page/basicPage.jsp">
  <jsp:param name="uriStr" value="${pageContext.errorData.requestURI}"/>
</jsp:forward>

<% } %>



