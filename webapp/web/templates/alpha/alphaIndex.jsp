<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%/* this odd thing points to something in web.xml */ %>
<%@ page errorPage="/error.jsp"%>
<%  /***********************************************
        alphaIndex.jsp will just display the just the index, no entites.
         
        request attributres:
        'alpha' - set to currently displaying alpha, 'none' or 'all'
        'controllerParam' - parameter for controller
        'count' - count of entites in the index
        'letters' - List of STrings, letters for index.
        'servlet' - name of servlet to put in links.
        
        put something like this in for debuggin: < % =  MiscWebUtils.getReqInfo(request) % >
         bdc34 2006-02-06
        **********************************************/
       
%>

<c:if test="${ requestScope.showAlpha == 1 }">
<div class='alphaIndex'>
    <c:forEach items='${requestScope.letters}' var='letter'>
        <c:if test="${letter == requestScope.alpha}"> ${requestScope.alpha }&nbsp;</c:if>
        <c:if test="${letter != requestScope.alpha}"> 
            <c:url var="url" value=".${requestScope.servlet}">
                <c:param name="alpha">${letter}</c:param>
            </c:url>
            <a href='<c:url value="${url}&amp;${requestScope.controllerParam}"/>'>${letter} </a>
         </c:if> 
    </c:forEach> 
    
    <%  if( request.getAttribute("alpha") != null && ! "all".equalsIgnoreCase((String)request.getAttribute("alpha"))) {  %>         
        <a href='<c:url value=".${requestScope.servlet}?&amp;alpha=all&amp;${requestScope.controllerParam}"/>'>all </a>
        <c:if test='${not empty requestScope.count }'>
            (${requestScope.count} that start with ${requestScope.alpha })
        </c:if>    
     <% }else{ %>
        (${requestScope.count})      
     <% } %>             
</div>
</c:if>