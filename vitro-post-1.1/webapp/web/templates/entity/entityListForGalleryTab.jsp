<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page	import="java.util.*,edu.cornell.mannlib.vitro.webapp.beans.Individual"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%><%/* this odd thing points to something in web.xml */ %>
<%@ page errorPage="/error.jsp"%>
<%	/***********************************************
		 Display a List of Entities in the most basic fashion.
		 
		 request.attributes:
		 a List of Entity objects with the name "entities" 
		 portal id as "portal"
		 
		 request.parameters:
		 "rows"  is number of rows in gallery table
		 "columns" is number of columns in gallery table
		 
		  Consider sticking < % = MiscWebUtils.getReqInfo(request) % > in the html output for debugging info.
		  		 
		 bdc34 2006-01-27 created		 
        **********************************************/		                      
		if (request.getAttribute("entities") == null){
        	String e="entityListForTabs.jsp expects that request attribute 'entities' be set to a List of Entity objects.";
    	    throw new JspException(e);
        }         
%>
<c:set var='entities' value='${requestScope.entities}' /><%/* just moving this into page scope for easy use */ %>
<c:set var='portal' value='${requestScope.portal}' />
<c:set var='count' value='0'/>

<c:set var='rows'>
	<c:out value="${requestScope.rows}" default="3"/>
</c:set>
<c:set var='columns'>
 	<c:out value="${requestScope.columns}" default="8"/>
</c:set>

<c:set var='IMG_WIDTH' value='100'/>

<table class='tabEntities entityListForGalleryTab'>
	<c:forEach var='row' begin="1" end="${rows}" step="1">
	<tr>
	  	<c:forEach var='col' begin="1" end="${columns}" step="1">
	  		<c:set var='ent' value='${entities[count]}'/>
			<c:set var='count' value='${count + 1}'/>
	  		<c:if test="${ not empty ent and not empty ent.thumbUrl}">
			<td>
				<c:url var="entityHref" value="/entity">
					<c:param name="home" value="${portal.portalId}"/>
					<c:param name="uri" value="${ent.URI}"/>
				</c:url>
				<a class="image" href="<c:out value="${entityHref}"/>" >
					<img width="${IMG_WIDTH}" src="${pageContext.request.contextPath}${ent.thumbUrl}" title="${ent.name}" alt="${ent.name}" />
				</a>
			</td>
			</c:if>
		</c:forEach>
	</tr>
	</c:forEach>
</table>


