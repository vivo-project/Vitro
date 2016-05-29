<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils" %>
<%	/***********************************************
		 debug.jsp will just display the request info in a div.
		 
		 It is uesful to use at the body jsp for a new controller before
		 you have writen the jsp that you intend to use.
		  		 
		 bdc34 2006-02-06
        **********************************************/		                      
%>
<div class='debug'>
    <%= MiscWebUtils.getReqInfo(request) %>
</div>
