<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<div>${checksum} "${fileName}"</div>

<c:url value="/individual" var="url">
    <c:param name="uri" value="${uri}"/>
</c:url>
<div>return to  <a href="${url}">${name}</a></div>
 