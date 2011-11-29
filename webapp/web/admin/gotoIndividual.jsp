<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousAdminPages" %>
<% request.setAttribute("requestedActions", new UseMiscellaneousAdminPages()); %>
<vitro:confirmAuthorization />

<%
if( request.getParameter("uri") != null ){
    %> <c:redirect url="/entity"><c:param name="uri" value="${param.uri}"/></c:redirect> <%
    return;
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>  <!-- gotoIndividual.jsp -->           
    <link rel="stylesheet" type="text/css" href="<c:url value="/${themeDir}css/screen.css"/>" media="screen"/>  
    <link rel="stylesheet" type="text/css" href="<c:url value="/${themeDir}css/formedit.css" />" media="screen"/>
    
    <title>Enter a URI</title>
</head>
<body class="formsEdit">
<div id="wrap">

<% /* BJL23 put this is in a catch block because it seems to fail ungracefully for 
      some clones */ %>

<form>
<input name="uri" type="text" size="200" />
<input type="submit" value="Lookup Individual for URI"/>
</form>

</div>
</body>
</html>