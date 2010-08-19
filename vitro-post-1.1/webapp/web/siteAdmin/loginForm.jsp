<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%-- Included in siteAdmin/main.jsp to handle login/logout form and processing --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.login.LoginTemplateHelper" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>

<%
   String themeDir = new VitroRequest(request).getPortal().getThemeDir().replaceAll("/$", "");
%>

<%= new LoginTemplateHelper(request).showLoginPage(request) %>