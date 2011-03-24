<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="contents">

<h1>Internal Server Error</h1>

<p class="warning">An internal error has occurred in the server</p>

<p>Please try again later.</p>

<p>If the problem persists, please consider <a href="<c:url value="comments"/>">contacting us</a> and telling us how you arrived here.</p>

<!-- _______________________________Exception__________________________________

500
Request URI:  ${param.uriStr}
Exception:    ${param.errStr}
Stack trace:
<c:forEach var="trace" items="${pageContext.exception.stackTrace}">
  ${trace} <%="\n"%>
</c:forEach>
___________________________________________________________________________ -->

</div><!-- contents -->

