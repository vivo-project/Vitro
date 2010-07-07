<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Portal" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.policy.RoleBasedPolicy" %>

<%@ page errorPage="/error.jsp"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %><%/* this odd thing points to something in web.xml */ %>
<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>

<jsp:useBean id="loginHandler" class="edu.cornell.mannlib.vedit.beans.LoginFormBean" scope="session" />

<%
    Portal portal = (Portal) request.getAttribute("portalBean");
    final String DEFAULT_SEARCH_METHOD = "fulltext"; /* options are fulltext/termlike */     
    
    int securityLevel = loginHandler.ANYBODY;
    String loginStatus = loginHandler.getLoginStatus(); 
    if ( loginStatus.equals("authenticated")) {
       securityLevel = Integer.parseInt( loginHandler.getLoginRole() );
    }
%>


<div id="content">

    <div class="tab">
        <h2>Site Administration</h2>
    </div>
    
	<div id="adminDashboard">
	
	    <%@ include file="loginForm.jsp" %>
	    
	    <%@ include file="dataInput.jsp" %>
	    
	    <%@ include file="siteConfiguration.jsp" %>
	
	    <%@ include file="ontologyEditor.jsp" %>
        
        <%@ include file="advancedDataTools.jsp" %> 
        
        <%@ include file="customReports.jsp" %> 
        <%-- 
        <%@ include file="sessionPreferences.jsp" %>      
        --%>
    </div> <!--  end adminDashboard -->
    
</div> <!-- end content -->


