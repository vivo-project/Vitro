<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.flags.PortalFlagChoices" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page errorPage="/error.jsp"%>
<%  /***********************************************
          Displays the little group of things at the bottom of the page
          for administrators and editors.
         
         request.attributes:
         an Entity object with the name "entity" 
         
         
         request.parameters:
         None, should only work with requestScope attributes for security reasons.
         
          Consider sticking < % = MiscWebUtils.getReqInfo(request) % > in the html output
          for debugging info.
                 
         bdc34 2006-01-22 created        
        **********************************************/ 
        Individual entity=(Individual)request.getAttribute("entity");                         
        if (entity == null){
            String e="entityAdmin.jsp expects that request attribute 'entity' be set to the Entity object to display.";
            throw new JspException(e);
        }
%>
<c:if test="${sessionScope.loginHandler != null &&
              sessionScope.loginHandler.loginStatus == 'authenticated' &&
              sessionScope.loginHandler.loginRole >= sessionScope.loginHandler.editor }">
    <c:set var='entity' value='${requestScope.entity}'/><%/* just moving this into page scope for easy use */ %>
    <c:set var='portal' value='${requestScope.portal}'/> 
    <div class='admin top'>
        <h3 class="toggle">Admin Panel</h3>
        <div class="panelContents">
            <c:url var="editHref" value="/entityEdit">
      		    <c:param name="home" value="${currentPortalId}"/>
              <c:param name="uri" value="${entity.URI}"/>
            </c:url>
          <a href='<c:out value="${editHref}"/>'> edit this individual</a> | 
          <c:url var="cloneHref" value="/cloneEntity">
              <c:param name="home" value="${currentPortalId}"/>
              <c:param name="uri" value="${entity.URI}"/>
          </c:url>
          <a href='<c:out value="${cloneHref}"/>'> clone this individual </a>
          <p>Resource URI: <c:out value="${entity.URI}"/></p>
          </div>
    </div>

</c:if>
