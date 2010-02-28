<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %><%/* this odd thing points to something in web.xml */ %>
<%@ page import="java.util.*" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Portal" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>

<%
VitroRequest vreq = new VitroRequest(request);
Portal portalBean=vreq.getPortal();
%>

<div id="content">
    <h2>Feedback</h2>
	<c:set var='themeDir'>
  		<c:if test="${!empty context && context != ''}">/${context}</c:if>/<%=portalBean.getThemeDir()%>
	</c:set>
    <img src="${themeDir}site_icons/mail.gif" alt="mailbox"/><br/>

    <p>Thank you for contacting our curation and development team. We will respond to your inquiry as soon as possible.</p>
    <p>Click to return to the <a href="index.jsp?home=<%=portalBean.getPortalId()%>">home page</a>.</p>
</div><!-- content -->
