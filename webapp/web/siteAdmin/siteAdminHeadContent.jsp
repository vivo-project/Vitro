<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.User" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<c:set var="portal" value="${requestScope.portalBean}"/>
<c:set var="contextPath"><c:out value="${pageContext.request.contextPath}" /></c:set>
<c:set var="themeDir" value="${contextPath}/${portal.themeDir}"/>

<link rel="stylesheet" type="text/css" href="${contextPath}/css/login.css"/>
<link rel="stylesheet" type="text/css" href="${themeDir}css/formedit.css"/>

<script type="text/javascript" src="${contextPath}/js/jquery.js"></script> 
<script type="text/javascript" src="${contextPath}/js/login/loginUtils.js"></script>