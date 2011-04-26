<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %> 
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<vitro:requiresAuthorizationFor classNames="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageTabs" />

<div class="editingForm">

<form id="editForm" name="editForm" action="doTabHierarchyOperation" method="post">
    <input type="hidden" name="_epoKey" value="${epoKey}" />

<div align="center">
<table cellpadding="4" cellspacing="2" border="0">
	<tr><th colspan="3">
	<div class="entryFormHead">
		<h2>${title}</h2>
			<c:choose>
				<c:when test='${_action == "insert"}'>
					<h3>Creating New Record</h3>
				</c:when>
				<c:otherwise>
					<h3>Editing Existing Record</h3>
				</c:otherwise>
			</c:choose>
		<span class="entryFormHeadInstructions">(<sup>*</sup> Required Fields)</span>
	</div><!--entryFormHead-->
	</th></tr>
	
    <tr class="editformcell">
        <td valign="top">
            <b>Broader tab<sup>*</sup></b><br/>
			<select name="ParentId">
				<c:forEach var="option" items="${epo.formObject.optionLists['ParentId']}">
            		<option value="${option.value}">${option.body}</option> 
            	</c:forEach> 
			</select>
			<span class="warning"><form:error name="ParentId"/></span>
        </td>
    </tr>
    <tr class="editformcell">
        <td valign="top">
            <b>Narrower Tab<sup>*</sup></b><br/>
            <select name="ChildId" >
                <c:forEach var="option" items="${epo.formObject.optionLists['ChildId']}">
            		<option value="${option.value}">${option.body}</option> 
            	</c:forEach>
            </select>
            <span class="warning"><form:error name="ChildId"/></span>
        </td>
    </tr>
    
    	<tr class="editformcell">
		<td colspan="3" align="center">
			<c:choose>
				<c:when test='${_action == "insert"}'>
					<input id="primaryAction" type="submit" class="form-button" name="_insert" value="Create New Record"/>
				</c:when>
				<c:otherwise>		
					<input id="primaryAction" type="submit" class="form-button" name="_update" value="Submit Changes"/>
					<input type="submit" class="form-button" name="_insert" value="Save as New Record"/>
					<input type="submit" class="form-button" name="_delete" onclick="return confirmDelete();" value="Delete"/>
				</c:otherwise>
			</c:choose>
				<input type="reset"  class="form-button" value="Reset"/>
				<input type="submit" class="form-button" name="_cancel" value="Cancel"/>
		</td>
	</tr>
</table>
</div><!--alignCenter-->

</form>

</div><!--editingform-->
    

