<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ page errorPage="/error.jsp"%>

<c:set var="errorMsg">
We are not sure what you would like to edit.
</c:set>

<jsp:include page="/edit/formPrefix.jsp"/>
<div id="content" class="full">
    <div align="center">${errorMsg}</div>


<%
VitroRequest vreq = new VitroRequest(request);
if( vreq.getParameter("subjectUri") != null ){ 
    Individual individual = vreq.getWebappDaoFactory().getIndividualDao().getIndividualByURI(vreq.getParameter("subjectUri"));
    String name = "the individual you were trying to edit.";
    if( individual != null && individual.getName() != null ){ %>
          name = individual.getName() + ".";
    <% } %>    
     <c:url value="/entity" var="entityPage">
       <c:param name="uri"><%=vreq.getParameter("subjectUri")%></c:param>
     </c:url>  
     <div align="center">                
      <button type="button" 
       onclick="javascript:document.location.href='${entityPage}'">
       Return to <%=name%></button>
     </div>
<%}else{ %>
    <c:url value="/" var="siteRoot"/>   
    <div align="center">                
      <button type="button" 
        onclick="javascript:document.location.href='${siteRoot}'">
        Return to main site</button>
    </div>
<%} %>
 
<jsp:include page="/edit/formSuffix.jsp"/>
