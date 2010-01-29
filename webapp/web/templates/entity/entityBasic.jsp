<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission" %>
<%@ page import="edu.cornell.mannlib.vedit.beans.LoginFormBean" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://vitro.mannlib.cornell.edu/vitro/tags/StringProcessorTag" prefix="p" %>

<%@ page errorPage="/error.jsp"%>
<%! 
public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.templates.entity.entityBasic.jsp");
%>
<%
log.debug("Starting entityBasic.jsp");
Individual entity = (Individual)request.getAttribute("entity");
%>

<c:if test="${!empty entityURI}">
	<c:set var="myEntityURI" scope="request" value="${entityURI}"/>
	<%
		try {
			VitroRequest vreq = new VitroRequest(request);
			entity = vreq.getWebappDaoFactory().getIndividualDao().getIndividualByURI((String)request.getAttribute("myEntityURI"));
			System.out.println("entityBasic rendering "+entity.getURI());
		} catch (Exception e) {
			e.printStackTrace();
		}
	%>
</c:if>

<% 

if (entity == null){
    String e="entityBasic.jsp expects that request attribute 'entity' be set to the Entity object to display.";
    throw new JspException(e);
}

if (VitroRequestPrep.isSelfEditing(request) || LoginFormBean.loggedIn(request, LoginFormBean.NON_EDITOR) /* minimum level*/) {
    request.setAttribute("showSelfEdits",Boolean.TRUE);
}%>
<c:if test="${sessionScope.loginHandler != null &&
              sessionScope.loginHandler.loginStatus == 'authenticated' &&
              sessionScope.loginHandler.loginRole >= LoginFormBean.NON_EDITOR}">
    <c:set var="showCuratorEdits" value="${true}"/>
</c:if>
<c:set var='imageDir' value='images' />
<c:set var="themeDir"><c:out value="${portalBean.themeDir}" /></c:set>
<%
    //here we build up the url for the larger image.
    String imageUrl = null;
    if (entity.getImageFile() != null && 
        entity.getImageFile().indexOf("http:")==0) {
        imageUrl =  entity.getImageFile();
    } else {
        imageUrl = response.encodeURL( "/images/" + entity.getImageFile() );                     
    }

    //anytime we are at an entity page we shouldn't have an editing config or submission
    session.removeAttribute("editjson");
    EditConfiguration.clearAllConfigsInSession(session);
    EditSubmission.clearAllEditSubmissionsInSession(session);
%>

<c:set var='entity' value='${requestScope.entity}'/><%/* just moving this into page scope for easy use */ %>
<c:set var='entityMergedPropsListJsp' value='/entityMergedPropList'/>
<c:set var='portal' value='${currentPortalId}'/>
<c:set var='portalBean' value='${currentPortal}'/>

<c:set var='themeDir'><c:out value='${portalBean.themeDir}' /></c:set>

    <div id="content">
        <jsp:include page="entityAdmin.jsp"/> 
        
        <div class='contents entity'>

       		<div id="label">
            <c:choose>
	            <c:when test="${!empty relatedSubject}">
	                <h2><p:process>${relatingPredicate.domainPublic} for ${relatedSubject.name}</p:process></h2>
                    <c:url var="backToSubjectLink" value="/entity">
                        <c:param name="home" value="${portalBean.portalId}"/>
                        <c:param name="uri" value="${relatedSubject.URI}"/>
                    </c:url>
                    <p><a href="${backToSubjectLink}">&larr; return to ${relatedSubject.name}</a></p>
	            </c:when>
	            <c:otherwise>
	                <h2><p:process>${entity.name}</p:process></h2> 
	                <c:if test="${!empty entity.moniker}">
	                    <p:process><em class="moniker">${entity.moniker}</em></p:process>
	                </c:if>
	            </c:otherwise>
            </c:choose>
            </div><!-- entity label -->
            <c:if test="${ (!empty entity.anchor) || (!empty entity.linksList) }">
                <ul class="externalLinks">
	                <c:if test="${!empty entity.anchor}">
	                    <c:choose>
	                        <c:when test="${!empty entity.url}">
	                            <c:url var="entityUrl" value="${entity.url}" />
	                            <li class="primary"><a class="externalLink" href="<c:out value="${entityUrl}"/>"><p:process>${entity.anchor}</p:process></a></li>
	                        </c:when>
	                        <c:otherwise>
	                            <li class="primary"><span class="externalLink"><p:process>${entity.anchor}</p:process></span></li>
	                        </c:otherwise>
	                    </c:choose>
	                </c:if>
                    <c:if test="${!empty entity.linksList }">
	                    <c:forEach items="${entity.linksList}" var='link' varStatus="count">
	                        <c:url var="linkUrl" value="${link.url}" />
	                        <c:choose>
	                          <c:when test="${empty entity.url && count.first==true}"><li class="first"></c:when>
	                          <c:otherwise><li></c:otherwise>
	                        </c:choose>
	                        <a class="externalLink" href="<c:out value="${linkUrl}"/>"><p:process>${link.anchor}</p:process></a></li>
	                    </c:forEach>
	                </c:if>
                </ul>
	        </c:if>   
            <c:if test="${!empty entity.imageThumb}">
                <div class="thumbnail">
                    <c:if test="${!empty entity.imageFile}">
                        <c:url var="imageUrl" value="/${imageDir}/${entity.imageFile}" />
                        <a class="image" href="${imageUrl}">
                    </c:if>
                    <c:url var="imageSrc" value='/${imageDir}/${entity.imageThumb}'/>
                    <img src="<c:out value="${imageSrc}"/>" title="click to view larger image in new window" alt="" width="150"/>
                    <c:if test="${!empty entity.imageFile}"></a></c:if>
                </div>
                <c:if test="${!empty entity.citation}">
                    <div class="citation">${entity.citation}</div>
                </c:if>
            </c:if>
            <p:process>
                <div class='description'>${entity.blurb}</div>
                <div class='description'>${entity.description}</div>
            </p:process>
            <c:choose>
                <c:when test="${showCuratorEdits || showSelfEdits}">
                     <c:import url="${entityMergedPropsListJsp}">
                         <c:param name="mode" value="edit"/>
                         <c:param name="grouped" value="false"/>
                         <%-- unless a value is provided, properties not assigned to a group will not have a tab or appear on the page --%>
                         <c:param name="unassignedPropsGroupName" value=""/>
                     </c:import>
                 </c:when>
                 <c:otherwise>
                     <c:import url="${entityMergedPropsListJsp}">
                         <c:param name="grouped" value="false"/>
                         <%-- unless a value is provided, properties not assigned to a group will not have a tab or appear on the page --%>
                         <c:param name="unassignedPropsGroupName" value=""/>
                     </c:import>
                 </c:otherwise>
            </c:choose>
            <p/>
            <p:process>
                <c:if test="${(!empty entity.citation) && (empty entity.imageThumb)}">
                    <div class="citation">${entity.citation}</div>
                </c:if>
                <c:if test="${!empty entity.keywordString}">
               	    <p id="keywords">Keywords: ${entity.keywordString}</p>
                </c:if>
            </p:process>
            ${requestScope.servletButtons}
        </div>
    </div> <!-- content -->
