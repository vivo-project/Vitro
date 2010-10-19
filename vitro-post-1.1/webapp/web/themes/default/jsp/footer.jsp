<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page language="java"%>
<%@ page import="java.util.Calendar" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.*"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Portal" %>
<%@ page import="edu.cornell.mannlib.vedit.beans.LoginStatusBean" %>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %><%/* this odd thing points to something in web.xml */ %>
<%
    final Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.web.themes.default.footer.jsp");

    VitroRequest vreq = new VitroRequest(request);

    Portal portal = vreq.getPortal();
    if (portal==null) {
    	log.error("portal from vreq.getPortal() null in themes/default/footer.jsp");
    }

	boolean isEditor = LoginStatusBean.getBean(request).isLoggedInAtLeast(LoginStatusBean.EDITOR);
%>
<c:set var="currentYear" value="<%=  Calendar.getInstance().get(Calendar.YEAR) %>" />
<div class='footer'><div class='footerLinks'>
	<% String rootBreadCrumb = BreadCrumbsUtil.getRootBreadCrumb(vreq,"",portal); 
	   if (rootBreadCrumb != null && rootBreadCrumb.length()>0) { %>
	    <%=rootBreadCrumb%> | 
	<% } %>
      <a href="browsecontroller?home=<%=portal.getPortalId()%>">Index</a>
    | <a href="comments?home=<%=portal.getPortalId()%>">Contact Us</a>
    <% if (isEditor) { %>
        | admin [
	    <a href="http://validator.w3.org/check?uri=referer">validate xhtml</a>
	    <a href="http://jigsaw.w3.org/css-validator/check/referer">validate css</a>
	    ]
    <% } %>
    </div>
    <% if (portal.getCopyrightAnchor() != null && portal.getCopyrightAnchor().length()>0) { %> 
	    <div class='copyright'>
		    &copy;${currentYear}&nbsp;
			<% if (portal.getCopyrightURL() != null && portal.getCopyrightURL().length()>0) { %>
				<a href="<%=portal.getCopyrightURL()%>">
			<% } %>
			<%=portal.getCopyrightAnchor()%>
			<% if (portal.getCopyrightURL() != null && portal.getCopyrightURL().length()>0) { %>
				</a>.
			<% } %>
	    </div>
	    <div class='copyright'>
		    All Rights Reserved. <a href="termsOfUse?home=<%=portal.getPortalId()%>">Terms of Use</a>
	    </div>
	<% } %> 
</div>
