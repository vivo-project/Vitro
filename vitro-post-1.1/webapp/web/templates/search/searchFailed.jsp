<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %><%/* this odd thing points to something in web.xml */ %>
<%@ page errorPage="/error.jsp"%>
<%	/***********************************************
		 Used when the search results are empty.
		 
		 request.attributes:
		 		 
		 request.parameters:
		 None yet.
		 
		  Consider sticking < % = MiscWebUtils.getReqInfo(request) % > in the html output
		  for debugging info.
		  		 		 		
    **********************************************/
%>
<c:set var='lists' value='${requestScope.collatedResultsLists}'/>
<c:set var='groupNames' value='${requestScope.collatedGroupNames}'/>
<c:set var='portal' value='${requestScope.portal}'/>
<c:set var='portalBean' value='${requestScope.portalBean}'/>
<c:set var='portalId' scope='request' value='${portalBean.portalId}'/>
<c:set var='entitiesListJsp' value='/templates/entity/entityList.jsp'/>
<div id="content">
	<div class="contents searchFailed">
        <p class="warning">
          <c:out value='${requestScope.message}'
                 default='No results were found for your query.  Please modify your search and try again.'
                 escapeXml='false'/>

	    </p>
		
		<jsp:include page="searchTips.jsp"/>		
	</div><!-- contents -->
</div><!-- content -->
