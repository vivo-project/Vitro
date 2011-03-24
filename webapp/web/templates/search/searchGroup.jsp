<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %><%/* this odd thing points to something in web.xml */ %>
<%@ page errorPage="/error.jsp"%>
<%	/***********************************************
		 Display a single search result group
		 
		 request.attributes:
		 a List with objects with the named "entities" 
		 a ClassGroup object named "classgroup"
		 
		 request.parameters:
		 None yet.
		 
		  Consider sticking < % = MiscWebUtils.getReqInfo(request) % > in the html output
		  for debugging info.		  		 		 		
        **********************************************/		                      
		if (request.getAttribute("entities") == null){
        	String e="searchGroup.jsp expects that request attribute 'entities' be set to the Entity object to display.";
    	    throw new JspException(e);
        }
        if (request.getAttribute("classgroup") == null){
        	String e="searchGroup.jsp expects that request attribute 'classgroup' be set to the Entity object to display.";
    	    throw new JspException(e);
        }
%>
<c:set var='imageDir' value='images' />
<c:set var='entities' value='${requestScope.entities}'/><%/* just moving this into page scope for easy use */ %>
<c:set var='classgroup' value='${requestScope.classgroup}'/>

<c:set var='portal' value='${requestScope.portal}'/>
<c:set var='portalBean' value='${requestScope.portalBean}'/>

		<div class='contents entity entity${entity.id}'>
				<h1><c:out value="${entity.name}"/></h1>
				<c:out value="${entity.moniker}" default="moniker?"/>		
				<c:if test="${!empty entity.anchor}">
					<a href='<c:url value="${entity.url}"/>'>${entity.anchor}</a>
				</c:if>
				<c:forEach items="${entity.linksList}" var='link'>
					| <a href='<c:url value="${link.url}"/>'${link.anchor}</a>
				</c:forEach>
				<c:if test="${!empty entity.imageThumb}">
				<div class="thumbnail">
					<c:if test="${!empty entity.imageFile}"><a target="_new" href="<c:url value='${imageDir}/${entity.imageFile}'/>"></c:if>
					<img src="<c:url value='${imageDir}/${entity.imageThumb}'/>" title="click to view larger image in new window" width="150">
					<c:if test="${!empty entity.imageFile}"></a></c:if>
				</div>
				</c:if>
				<c:import url="${entityPropsListJsp}" /><%/* here we import the properties for the entity */ %>
				<div class='description'>
				<c:out value="${entity.description}" escapeXml ='false'/>
				</div>
				<jsp:include page="entityAdmin.jsp"/>
		</div>
