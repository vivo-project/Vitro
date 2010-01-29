<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@page import="org.apache.commons.logging.Log"%>
<%@page import="org.apache.commons.logging.LogFactory"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jstl/functions" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/string-1.1" prefix="str" %>


<%
    /* Clear any cruft from session. */
    String redirectTo = null;
    String urlPattern = null;
    if( session != null ) {
        EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,request);
        //In order to support back button resubmissions, don't remove the editConfig from session.
        //EditConfiguration.clearEditConfigurationInSession(session, editConfig);
        
        EditSubmission editSub = EditSubmission.getEditSubmissionFromSession(session,editConfig);        
        EditSubmission.clearEditSubmissionInSession(session, editSub);

        if( editConfig != null && editConfig.getEntityToReturnTo() != null ){
            String predicateUri = editConfig.getPredicateUri();
            log.debug("Return to property after submitting form: " + predicateUri);
            %>
            <c:set var="predicateUri" value="<%=predicateUri%>" />
            <c:set var="localName" value="${fn:substringAfter(predicateUri, '#')}" />
            <%  
                        
            if( editConfig.getEntityToReturnTo().startsWith("?") ){
                redirectTo = (String)request.getAttribute("entityToReturnTo");
            }else{            
                redirectTo = editConfig.getEntityToReturnTo();
            }
              
        }
        if( editConfig != null && editConfig.getUrlPatternToReturnTo() != null){
            urlPattern = editConfig.getUrlPatternToReturnTo();
        }
    }

    if( redirectTo != null ){
        request.setAttribute("redirectTo",redirectTo);    %>

        <%-- <c:redirect url="/entity">
            <c:param name="uri" value="${redirectTo}" />
            <c:param name="property" value="${localName}" />
        </c:redirect> --%>

      <%  if( urlPattern != null && urlPattern.endsWith("entity")){  %>
		      <%-- Here we're building the redirect URL to include an (unencoded) fragment identifier such as: #propertyName  --%>               
		      <c:url context="/" var="encodedUrl" value="/entity">
			     <c:param name="uri" value="${redirectTo}" />
              </c:url>
		      <c:redirect url="${encodedUrl}${'#'}${localName}" />
       <% } else {
              request.setAttribute("urlPattern",urlPattern);%>
              <c:url context="/" var="encodedUrl" value="${urlPattern}">
                 <c:param name="uri" value="${redirectTo}" />
              </c:url>
              <c:redirect url="${encodedUrl}${'#'}${localName}" />                    
		<%} %>
    <% } else { %>
        <c:redirect url="<%= Controllers.LOGIN %>" />
    <% } %>


<%!
Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.edit.postEditCleanUp.jsp");
%>



