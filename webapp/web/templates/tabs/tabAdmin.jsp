<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Tab" %>
<%@page import="edu.cornell.mannlib.vedit.beans.LoginStatusBean"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ page errorPage="/error.jsp"%>
<% /***********************************************
 Displays the little group of things at the bottom of the page
 for administrators and editors.

 request.attributes:
 an Tab object with the name "leadingTab"


 request.parameters:
 None, should only work with requestScope attributes for security reasons.

 Consider sticking < % = MiscWebUtils.getReqInfo(request) % > in the html output
 for debugging info.

 bdc34 2006-01-22 created
 **********************************************/

    Tab leadingTab = (Tab) request.getAttribute("leadingTab");
    if (leadingTab == null) {
        String e = "tabAdmin.jsp expects that request attribute 'leadingTab' be set to a TabBean object";
        throw new JspException(e);
    }
%>

<% if ( LoginStatusBean.getBean(request).isLoggedInAtLeast(LoginStatusBean.EDITOR))  {  %>   	
  <c:set var='tab' value='${requestScope.leadingTab}'/><%/* just moving this into page scope for easy use */ %>
   	<c:set var='portal' value='${requestScope.portalBean.portalId}'/>
	<div class='admin bottom'>  	
		<c:url var="editHref" value="tabEdit">
			<c:param name="home" value="${currentPortalId}"/>
			<c:param name="controller" value="Tab"/>
			<c:param name="id" value="${tab.tabId}"/>	
		</c:url>
		<c:set var="editHref">
			<c:out value="${editHref}" escapeXml="true"/>
		</c:set>
    	<a href="${editHref}">edit tab: <em>${tab.title}</em></a> 
    	<% /* | <a href='<c:url value="cloneEntity?home=${portal}&tabId=${tab.tabId}"/>'> <i>clone tab</i> ${tab.title}</a> */ %>      
    </div>
<% } %>
