<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<% if (loginBean.isLoggedInAtLeast(LoginStatusBean.EDITOR) { %>
    <div class="pageBodyGroup">
    
	    <h3>Data Input</h3>
	
	<%--	<form action="editForm" method="get">
		    <select id="VClassURI" name="VClassURI" class="form-item">
		        <form:option name="VClassId"/>
		    </select>
		    <input type="submit" class="add-action-button" value="Add individual of this class"/>
		    <input type="hidden" name="home" value="<%=portal.getPortalId()%>" />
		    <input type="hidden" name="controller" value="Entity"/>
		</form>  --%>
		
		<c:url var="editRequestDisUrl" value="/edit/editRequestDispatch.jsp"/>		
		<form action="${editRequestDisUrl}" method="get">
		    <select id="VClassURI" name="typeOfNew" class="form-item">
		        <form:option name="VClassId"/>
		    </select>
		    <input type="hidden" name="editform" value="newIndividualForm.jsp"/>
		    <input type="submit" class="add-action-button" value="Add individual of this class"/>
		</form>
		
	</div>
<% } %>