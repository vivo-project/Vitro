<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="editingForm">

<c:set var="colspan">
	<c:out value="${colspan}" default="3"/>
</c:set>

<c:set var="onSubmit">
   <c:out value="${formOnSubmit}" default="return true;"/>
</c:set>

<c:set var="action">
    <c:out value="${editAction}" default="doEdit"/>
</c:set>

<form id="editForm" name="editForm" action="${action}" method="post" onsubmit="${onSubmit}">
    <input type="hidden" name="_epoKey" value="${epoKey}" />

<table cellpadding="4" cellspacing="2"  style="background-color:#fff">
	<tr><th colspan="${colspan}">
	<div>
		<h2>${title}</h2>
			<c:choose>
				<c:when test='${_action == "insert"}'>
					<h3 class="blue">Creating New Record
				</c:when>
				<c:otherwise>
					<h3 class="blue">Editing Existing Record
				</c:otherwise>
			</c:choose>
	 <span class="note">(<sup>*</sup> Required Fields)</span></h3>
	</div><!--entryFormHead-->
	</th></tr>
	
	<c:if test="${!empty globalErrorMsg}">
	    <tr><td><span class="notice">${globalErrorMsg}</span></td></tr>
	</c:if>
	
	<jsp:include page="${formJsp}"/>
	
	<tr class="editformcell">
		<td colspan="${colspan}">
			<c:choose>
				<c:when test='${_action == "insert"}'>
					<input id="primaryAction" type="submit" class="submit" name="_insert" value="Create New Record"/>
				</c:when>
				<c:otherwise>		
    				<input id="primaryAction" type="submit" class="submit" name="_update" value="Submit Changes"/>
                    <c:if test="${ ! (_cancelButtonDisabled == 'disabled') }">	
				        <input type="submit" class="delete" name="_delete" onclick="return confirmDelete();" value="Delete"/>
                    </c:if>
				</c:otherwise>
			</c:choose>
			
			<input type="reset"  class="delete" value="Reset"/>
			
            <c:choose>
                <c:when test="${!empty formOnCancel}">
                    <input type="submit" class="delete" name="_cancel" onclick="${formOnCancel}" value="Cancel"/> 
                </c:when>
                <c:otherwise>
		            <input type="submit" class="delete" name="_cancel" value="Cancel"/>
                </c:otherwise>
            </c:choose>
		</td>
	</tr>
</table>

</form>

</div><!--editingform-->
