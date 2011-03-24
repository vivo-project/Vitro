<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page isThreadSafe="false" %>
<%@ page import="java.util.*" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<%  final int DEFAULT_PORTAL_ID=1;
    String portalIdStr=(portalIdStr=(String)request.getAttribute("home"))==null ?
        ((portalIdStr=request.getParameter("home"))==null?String.valueOf(DEFAULT_PORTAL_ID):portalIdStr):portalIdStr;
        //int incomingPortalId=Integer.parseInt(portalIdStr);    
%>

<jsp:useBean id="loginHandler" class="edu.cornell.mannlib.vedit.beans.LoginFormBean" scope="session">
    <jsp:setProperty name="loginHandler" property="*"/>
</jsp:useBean>

<c:url var="siteAdminUrl" value="<%= Controllers.SITE_ADMIN %>" />

<% 

    String submitModeStr = request.getParameter("loginSubmitMode");
    if ( submitModeStr == null ) {
        submitModeStr = "unknown";
    } 
    
    if ( submitModeStr.equalsIgnoreCase("Log Out")) { %>
        <jsp:forward page="/logout" >
            <jsp:param name="home" value="<%= portalIdStr %>" />
        </jsp:forward>
        
<%  } else if ( submitModeStr.equalsIgnoreCase("Log In")) {
        String loginNameStr = request.getParameter("loginName");
        String loginPasswordStr = request.getParameter("loginPassword"); %>
        <jsp:setProperty name="loginHandler" property="loginName" value="<%= loginNameStr %>" />
        <jsp:setProperty name="loginHandler" property="loginPassword" value="<%= loginPasswordStr %>" />
        <jsp:setProperty name="loginHandler" property="loginRemoteAddr" value="<%= request.getRemoteAddr() %>" />
        
<%      if ( loginHandler.validateLoginForm() ) { %>
            <jsp:forward page="/authenticate" >
                <jsp:param name="home" value="<%= portalIdStr %>" />
            </jsp:forward>
<%      } else {
            String redirectURL = "${siteAdminUrl}?home=" + portalIdStr + "&amp;login=block";
            response.sendRedirect(redirectURL);
       }
    }
%>