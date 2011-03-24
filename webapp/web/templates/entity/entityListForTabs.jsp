<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%><%/* this odd thing points to something in web.xml */ %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page errorPage="/error.jsp"%>
<%  /***********************************************
        Display a list of entities for a tab.

         request.attributes:
         a List of Entity objects with the name "entities"
         portal id as "portal"

         request.parameters:
         None yet.

          Consider sticking < % = MiscWebUtils.getReqInfo(request) % > in the html output for debugging info.

         bdc34 2006-01-27 created
    **********************************************/
        if (request.getAttribute("entities") == null){
            String e="entityListForTabs.jsp expects that request attribute 'entities' be set to a List of Entity objects.";
            throw new JspException(e);
        }
%>
<c:set var="searchViewPrefix" value="/templates/search/"/>
<c:set var='entities' value='${requestScope.entities}' /><%/* just moving this into page scope for easy use */ %>
<c:set var='portal' value='${requestScope.portal}' />
<c:set var='IMG_DIR' value='images/' />
<c:set var='IMG_WIDTH' value='75'/>
<jsp:include page="/templates/alpha/alphaIndex.jsp"/>
<ul class='tabEntities entityListForTab'>
    <c:forEach items='${entities}' var='ent'>
	<c:url var="entHref" value="/entity">
		<c:param name="home" value="${sessionScope.currentPortalId}"/>
		<c:param name="uri" value="${ent.URI}"/>
	</c:url>
        <li>
            <a href='<c:out value="${entHref}"/>'>${ent.name}</a> 
			<c:if test="${!empty ent.moniker}">
			  <span class="tab-moniker">
				| <c:out value="${ent.moniker}"/>
  			</span>
			</c:if>
    	    <c:if test="${!empty ent.VClasses}">
                <c:forEach items="${ent.VClasses}" var="type">
					<c:if test="${!empty type.customSearchView}">
						<c:set var="customSearchView" value="${type.customSearchView}"/>
 					</c:if>
				</c:forEach>
			</c:if>
			<c:set var="anchorText" value="${ent.anchor}"/>
						
			<c:if test="${(!empty customSearchView) && (!empty anchorText)}">
                <c:set scope="request" var="individual" value="${ent}"/>
                <c:if test="${!empty ent.url}">
                    <c:set scope="request" var="individualURL" value="${ent.url}"/>
                </c:if>
                <jsp:include page="${searchViewPrefix}${customSearchView}" flush="true"/>
                <c:remove var="individual"/>
                <c:remove var="individualURL"/>
                <c:remove var="anchorText"/>
			</c:if>		
			
            <c:if test="${!empty anchorText}">
              <span class="tab-extLink"> |
                <c:choose>
                    <c:when test="${!empty ent.url}">
	                    <c:url var="entUrl" value="${ent.url}"/>
                        <a class="externalLink" href="<c:out value="${entUrl}"/>">${anchorText}</a>
                    </c:when>
                    <c:otherwise>
                        <span style="font-style: italic; text-size: 0.75em;">${anchorText}</span>
                    </c:otherwise>
                </c:choose>
              </span>
            </c:if>
            <c:forEach items='${ent.linksList}' var="entLink"><span class="tab-extLink"> | <c:url var="entLinkUrl" value="${entLink.url}"/><a class="externalLink" href="<c:out value="${entLinkUrl}"/>">${entLink.anchor}</a></span></c:forEach>
            <c:choose>
            <c:when test='${not empty ent.imageThumb }'>
            	<c:url var="imageHref" value="entity">
    						<c:param name="home" value="${sessionScope.currentPortalId}"/>
    						<c:param name="uri" value="${ent.URI}"/>
    					</c:url>
            	<c:url var="imageSrc" value="${IMG_DIR}${ent.imageThumb}"/>
                  <div class="tab-image"><a class="image" href="<c:out value="${imageHref}"/>"><img width="${IMG_WIDTH}" src="<c:out value="${imageSrc}"/>" title="${ent.name}" alt="" /></a></div>
                  <c:if test="${not empty ent.blurb}"><div class='blurb'>${ent.blurb}</div></c:if>
            </c:when>
            <c:otherwise>
                <c:if test="${not empty ent.blurb}"><div class='blurb'>${ent.blurb}</div></c:if>
            </c:otherwise>
            </c:choose>
        </li>
    </c:forEach>
</ul>


