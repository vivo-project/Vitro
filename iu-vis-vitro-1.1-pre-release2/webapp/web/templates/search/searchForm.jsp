<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %><%/* this odd thing points to something in web.xml */ %>
<!-- %@ page errorPage="/error.jsp"% -->
<%	/***********************************************
		 Used to display a search form.
		 
		 request.attributes:
		 		 
		 request.parameters:
		 None yet.
		 
		  Consider sticking < % = MiscWebUtils.getReqInfo(request) % > in the html output
		  for debugging info.
		  		 		 		
    **********************************************/
%>
<c:set var='portal' value='${requestScope.portal}'/>
<c:set var='portalBean' value='${requestScope.portalBean}'/>

<c:set var='themeDir' >
	<c:out value='${portal.themeDir}'/>
</c:set>
<div class='contents searchForm'>

<div class="advancedSearchForm">
<form name="filterForm" method="post" action="search">
<h3>Search</h3>
<input class="top_padded" style="width:97%;" name="querytext" value="" type="text"/>
<!-- supplanted by including OR, NOT etc. with search terms
<input name="inclusion" value="all" checked="checked" type="radio"/> all terms entered
   <input name="inclusion" value="any" type="radio"/> any terms entered
</p>
-->

<p><input class="form-button" value=" Search" type="submit"/></p>
</form>		
</div><!--advancedSearchForm-->	
<div class='searchTips'>
<jsp:include page="searchTips.jsp"/>
</div>					

</div>

