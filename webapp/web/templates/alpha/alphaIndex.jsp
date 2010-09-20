<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Portal" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %><%/* this odd thing points to something in web.xml */ %>
<%@ page errorPage="/error.jsp"%>
<%  /***********************************************
        alphaIndex.jsp will just display the just the index, no entites.
         
        request attributres:
        'alpha' - set to currently displaying alpha, 'none' or 'all'
        'tabParam' - parameter for tab
        'count' - count of entites in the index
        'letters' - List of STrings, letters for index.
        'servlet' - name of servlet to put in links.
        
        put something like this in for debuggin: < % =  MiscWebUtils.getReqInfo(request) % >
         bdc34 2006-02-06
        **********************************************/
        
        /***************************************************
        nac26 2008-05-09 following brian's lead from menu.jsp to get the portalId so it can be added to the alpha index links */
        final Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.web.alphaIndex.jsp");

        Portal portal = (Portal)request.getAttribute("portalBean");
        int portalId = -1;
        if (portal==null) {
            log.error("Attribute 'portalBean' missing or null; portalId defaulted to 1");
            portalId=1;
        } else {
            portalId=portal.getPortalId();
        }
        /**************************************************/
%>
<c:set var="portalId" value="<%=portalId%>" />
<c:if test="${ requestScope.showAlpha == 1 }">
<div class='alphaIndex'>
    <c:forEach items='${requestScope.letters}' var='letter'>
        <c:if test="${letter == requestScope.alpha}"> ${requestScope.alpha }&nbsp;</c:if>
        <c:if test="${letter != requestScope.alpha}"> 
            <a href='<c:url value=".${requestScope.servlet}?&amp;alpha=${letter}&amp;${requestScope.tabParam}"/>'>${letter} </a>
         </c:if> 
    </c:forEach> 
    
    <%  if( request.getAttribute("alpha") != null && ! "all".equalsIgnoreCase((String)request.getAttribute("alpha"))) {  %>         
        <a href='<c:url value=".${requestScope.servlet}?&amp;alpha=all&amp;${requestScope.tabParam}"/>'>all </a>
        <c:if test='${not empty requestScope.count }'>
            (${requestScope.count} that start with ${requestScope.alpha })
        </c:if>    
     <% }else{ %>
        (${requestScope.count})      
     <% } %>             
</div>
</c:if>