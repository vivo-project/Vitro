<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page language="java" %>
<%@ page errorPage="error.jsp"%>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.TabMenu" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Portal" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.BreadCrumbsUtil" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page import="edu.cornell.mannlib.vedit.beans.LoginStatusBean" %>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<%    
    LoginStatusBean loginBean = LoginStatusBean.getBean(request);
    boolean isLoggedIn = loginBean.isLoggedIn();
    String loginName = loginBean.getUsername();

     // VITRO FILE
    final Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.web.menu.jsp");

    VitroRequest vreq = new VitroRequest(request);
    Portal portal = vreq.getPortal();

    int portalId = -1;
    if (portal==null) {
        log.error("Attribute 'portalBean' missing or null; portalId defaulted to 1");
        portalId=1;
    } else {
        portalId=portal.getPortalId();
    }
    String fixedTabStr=(fixedTabStr=request.getParameter("fixed"))==null?null:fixedTabStr.equals("")?null:fixedTabStr;
    
%>
<c:set var='context' value="<%=vreq.getContextPath()%>" />
<c:set var='themePath'>
  <c:if test="${!empty context && context != ''}">/${context}</c:if>/<%=portal.getThemeDir()%>
</c:set>
<c:set var='themeDir'><c:out value='${themePath}'/></c:set>
<c:set var="currentPortal" value="<%=portal.getPortalId()%>"/>

<%
String homeURL = (portal.getRootBreadCrumbURL()!=null && portal.getRootBreadCrumbURL().length()>0) ?
portal.getRootBreadCrumbURL() : request.getContextPath()+"/";
%>

<%-- <h1><%=vreq.getContextPath()%></h1> --%>

<div id="identity">
  
  <%-- <a href="<%=homeURL%>"><img class="closecrop" src="${themeDir}site_icons/<%=appBean.getRootLogotypeImage()%>" width="<%=appBean.getRootLogotypeWidth()%>" height="<%=appBean.getRootLogotypeHeight()%>" alt="<%=appBean.getRootLogotypeTitle()%>"/></a> --%>
  <h1>
    <a href="<%=homeURL%>">
      <%-- <img class="closecrop" src="${themeDir}site_icons/<%=portal.getLogotypeImage()%>" width="<%=portal.getLogotypeWidth()%>" height="<%=portal.getLogotypeHeight()%>" alt="<%=portal.getAppName()%>"/> --%>
      <%-- <img src="${themeDir}site_icons/<%=portal.getLogotypeImage()%>" alt="<%=portal.getAppName()%>"/> --%>
      <% if (!(portal.getLogotypeImage() == null || portal.getLogotypeImage().equals("")))
      { %>
        <img src="${themeDir}site_icons/<%=portal.getLogotypeImage()%>" alt="<%=portal.getAppName()%>"/>
      <% } else { 
        out.print(portal.getAppName()); 
      } %>

    </a>
  </h1>
   
  <ul id="otherMenu">
  
    <%-- A user is logged in --%>
    <% if (isLoggedIn) { %>

      <c:url var="logoutHref" value="<%= Controllers.LOGOUT_JSP %>">
        <c:param name="home" value="${currentPortal}" />
        <c:param name="loginSubmitMode" value="Log Out" /> 
      </c:url>
  
      <c:url var="siteAdminHref" value="<%= Controllers.SITE_ADMIN %>">
        <c:param name="home" value="${currentPortal}" />
      </c:url>
 
      <li class="border">
        Logged in as <strong><%= loginName %></strong> (<a href="${logoutHref}">Log out</a>)     
      </li>
      
      <li class="border"><a href="${siteAdminHref}" >Site Admin</a></li>
       
    <%-- A user is not logged in --%>
    <% } else { %>
  
      <c:url var="loginHref" value="<%= Controllers.LOGIN %>">
        <c:param name="home" value="${currentPortal}"/>
        <c:param name="login" value="block"/>
      </c:url>
    
      <li class="border"><a title="log in to manage this site" href="${loginHref}">Log in</a></li>
    <% } %>

    <c:url var="aboutHref" value="<%= Controllers.ABOUT %>">
      <c:param name="home" value="${currentPortal}"/>
    </c:url>
    <c:set var="aboutHref">
      <c:out value="${aboutHref}" escapeXml="true"/>
    </c:set>
  
    <li class="border"><a href="${aboutHref}" title="more about this web site">About</a></li>
    <li><a href='<c:url value="/comments"><c:param name="home" value="${currentPortal}"/></c:url>'>Contact Us</a></li>
  </ul>

</div><!-- end identity -->
