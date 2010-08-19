<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Link" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>

<%! 
public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.templates.entity.linkShortView.jsp");
%>

<%-- this comes in as an attribute called "individual" but the object has to be created as a Link using the Link dao getLinkByURI() method in order to render correctly
     when the link uri may sometimes be of XSD type anyURI and sometimes of XSD type string --%>
<%                             
Individual ind = (Individual)request.getAttribute("individual");
WebappDaoFactory wdf = (WebappDaoFactory)getServletContext().getAttribute("webappDaoFactory");
if (wdf!=null) {
    Link link = wdf.getLinksDao().getLinkByURI(ind.getURI());                                       
    if (link!=null) {
        request.setAttribute("linkIndividual", link);
    } else {
        log.error("Cannot create linkIndividual from individual "+ind.getUrl());
    }
} else {
    log.error("Cannot create WebappDaoFactory in linkShortView.jsp");
}%>


<c:choose>
	<c:when test="${!empty linkIndividual}">
        <c:choose>
            <c:when test="${!empty linkIndividual.anchor}">
                <c:choose>
                    <c:when test="${!empty linkIndividual.url}">
                        <c:url var="link" value="${linkIndividual.url}" />
                        <a class="externalLink" href="<c:out value="${link}"/>">${linkIndividual.anchor}</a>
                    </c:when>
                    <c:otherwise>
                        <c:out value="${linkIndividual.anchor}"/>
                    </c:otherwise>
                </c:choose>
            </c:when>
            <c:otherwise>
                <c:out value="link anchor is not populated"/>
            </c:otherwise>
        </c:choose>
	</c:when>
	<c:otherwise>
		<c:out value="link individual not found ..."/>
	</c:otherwise>
</c:choose>
