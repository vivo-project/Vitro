<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page import="javax.servlet.ServletException" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Portal"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean"%>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%! 
public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.templates.page.headContent.jsp");
%>
<%
  VitroRequest vreq = new VitroRequest(request);  
  Portal portal = vreq.getPortal();
  
  String themeDir = portal != null ? portal.getThemeDir() : Portal.DEFAULT_THEME_DIR_FROM_CONTEXT;
  themeDir = request.getContextPath() + '/' + themeDir; 
  
%>
<!-- headContent.jsp -->
<link rel="stylesheet" type="text/css" href="<%=themeDir%>css/screen.css" media="screen"/>
<link rel="stylesheet" type="text/css" href="<%=themeDir%>css/print.css" media="print"/>

<%-- <c:url var="jqueryPath" value="/js/jquery.js"/>
<script type="text/javascript" src="${jqueryPath}"></script> --%>

<link rel="stylesheet" type="text/css" href="<%=themeDir%>css/edit.css"/>
<title><c:out value="${requestScope.title}"/></title>
<%
// nac26 080424: the following line should only be uncommented for PHILLIPS (vivo.cornell.edu) to ensure we're only tracking stats on the live site
// <script type="text/javascript" src="http://vivostats.mannlib.cornell.edu/?js"></script>
%>
<c:if test="${!empty scripts}"><jsp:include page="${scripts}"/></c:if>

<%-- 
mw542 021009: Brian C said this was ignoring the catch tags throwing exceptions. we should find a better way to include css/js anyway

<c:set var="customJsp"><c:out value="${requestScope.bodyJsp}" default="/debug.jsp"/></c:set>
<c:set var="customHeadJsp">
    <c:if test="${fn:substringAfter(customJsp,'.jsp') == ''}">${fn:substringBefore(customJsp,'.jsp')}${"Head.jsp"}</c:if>
</c:set>
<c:if test="${customJsp != '/debug.jsp' && customHeadJsp != ''}">
    <c:catch var="fileCheck">
        <c:import url="${customHeadJsp}"/>
    </c:catch>
</c:if> 
--%>

<!-- end headContent.jsp -->
     