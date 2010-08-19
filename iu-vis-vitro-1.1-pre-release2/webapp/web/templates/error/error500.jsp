<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.lang.Integer" %>
<%@page isErrorPage="true" %>

<html>
<head>
	<title>Internal Server Error</title>
</head>

<body style="margin:2%;font-family:Arial, Helvetica, sans-serif;">
<div class="contents">

<h1>Internal Server Error</h1>

<p style="color:red;">An internal error has occurred in the server.</p>

<p>Please try again later.</p>

<p>If the problem persists, please consider <a href="<c:url value="comments"/>">contacting us</a> and telling us how you arrived here.</p>

</div><!-- contents -->

<div id="hiddenErrorDiv" style="display:none;">
_______________________________Exception__________________________________

500
Request URI:  ${param.uriStr}
Exception:    ${param.errStr}
Stack trace:
<c:forEach var="trace" items="${pageContext.exception.stackTrace}">
  ${trace} <%="\n"%>
</c:forEach>
___________________________________________________________________________ 

</div><!-- hiddenErrorDiv -->

</body>
</html>




