<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div>The file ${orginalFileName} was updated. The file received from you had the MD5 checksum ${checksum}.</div>

<c:if test="${useNewName}">
<div>The name of the file was also changed to ${newFileName}.</div>
</c:if>

<c:url value="/individual" var="url">
    <c:param name="uri" value="${fileUri}"/>
</c:url>

<div>Goto <a href="${url}">${newFileName}</a></div>
 