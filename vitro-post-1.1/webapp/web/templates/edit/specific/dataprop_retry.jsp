<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%-- colspan set to 4 in DatapropRetryController.java --%>
<tr class="editformcell">
	<td valign="top" colspan="1">
		<b>Public Name</b><br/><i>as will display on public pages</i><br/>
		<input name="PublicName" value="<form:value name="PublicName"/>"/>
		<div class="warning"><form:error name="PublicName"/></div>
	</td>
	<td valign="top" colspan="1">
		<b>Property Group</b><br/>
		<i>(for display headers and dashboard)</i><br/>
		<select name="GroupURI">
		  <form:option name="GroupURI"/>
		</select>
		<div class="warning"><form:error name="GroupURI"/></div>
	</td>
	<td valign="bottom" colspan="1">
        <b>Display Level</b><br /><i>(specify least restrictive level allowed)</i><br/>
        <select name="HiddenFromDisplayBelowRoleLevelUsingRoleUri"><form:option name="HiddenFromDisplayBelowRoleLevelUsingRoleUri"/></select>
        <font size="2" color="red"><form:error name="HiddenFromDisplayBelowRoleLevelUsingRoleUri"/></font>
    </td>
    <td valign="bottom" colspan="1">
        <b>Update Level</b><br /><i>(specify least restrictive level allowed)</i><br />
        <select name="ProhibitedFromUpdateBelowRoleLevelUsingRoleUri"><form:option name="ProhibitedFromUpdateBelowRoleLevelUsingRoleUri"/></select>
        <font size="2" color="red"><form:error name="ProhibitedFromUpdateBelowRoleLevelUsingRoleUri"/></font>
    </td>
</tr>
<tr class="editformcell">
	<!-- c:set var="existingLocalName" value="<form:value name='LocalName'/>"/ -->
	<td valign="top" colspan="2">
		<b>Ontology</b><br/>
        <c:choose>
        	<c:when test="${_action eq 'update'}">
        		<i>Change only via the "change URI" button on the previous screen</i><br/>
				<select name="Namespace" disabled="disabled">
					<form:option name="Namespace"/>
				</select>
			</c:when>
			<c:otherwise>
				<i>specifies Namespace</i><br/>
				<select name="Namespace">
					<form:option name="Namespace"/>
				</select>
			</c:otherwise>
		</c:choose>
		<div class="warning"><form:error name="Namespace"/></div>
	</td>
	<td valign="top" colspan="2">
		<b>Local Name</b>
        <c:choose>
        	<c:when test="${_action eq 'update'}">
        		<br/><i>Change only via the "change URI" button on the previous screen</i><br/>
				<input name="LocalName" value="<form:value name="LocalName"/>" disabled="disabled"/>
			</c:when>
			<c:otherwise>
				<i>(must be a valid XML name)<br/>startLowercaseAndUseCamelStyle</i><br/>
				<input name="LocalName" value="<form:value name="LocalName"/>"/>
			</c:otherwise>
		</c:choose>
		<div class="warning"><form:error name="LocalName"/></div>
	</td>
</tr>

<tr class="editformcell">
	<td valign="top" colspan="2">
		<b>Domain Class</b><br/>
		<select name="DomainClassURI">
			<form:option name="DomainClassURI"/>
		</select>
		<span class="warning"><form:error name="DomainClassURI"/></span>
	</td>
</tr>

<tr class="editformcell">
	<td valign="top" colspan="2">
		<b>Range Datatype</b><br/>
		<select name="RangeDatatypeURI">
			<form:option name="RangeDatatypeURI"/>
		</select>
		<span class="warning"><form:error name="RangeDatatypeURI"/></span>
	</td>
    <td valign="bottom" colspan="2">
        <c:set var="functionalLabel" value="<b>Functional property</b> <i>(has at most one value for each individual)</i>" />
        <c:choose>
             <c:when test="${functional}">
    	         <input name="Functional" type="checkbox" value="TRUE" checked="checked"/>${functionalLabel}
             </c:when>
             <c:otherwise>
                 <input name="Functional" type="checkbox" value="TRUE"/>${functionalLabel}
             </c:otherwise>
        </c:choose>
    </td>
</tr>

<tr class="editformcell">
    <td valign="top" colspan="4">
        <b>Example</b><br/>
        <textarea name="Example"><form:value name="Example"/></textarea>
        <span class="warning"><form:error name="Example"/></span>
    </td>
</tr>

<tr class="editformcell">
	<td valign="top" colspan="4">
		<b>Description</b> for ontology editors<br/>
		<textarea name="Description"><form:value name="Description"/></textarea>
		<span class="warning"><form:error name="Description"/></span>
	</td>
</tr>

<tr class="editformcell">
	<td valign="top" colspan="4">
		<b>Public Description</b> for front-end users, as it will appear on editing forms<br/>
		<textarea name="PublicDescription"><form:value name="PublicDescription"/></textarea>
		<span class="warning"><form:error name="PublicDescription"/></span>
	</td>
</tr>

<tr class="editformcell">
	<td valign="top" colspan="1">
		<b>Display Tier</b><br/>
		<input name="DisplayTier" value="<form:value name="DisplayTier"/>"/>
		<span class="warning"><form:error name="DisplayTier"/></span>
	</td>
	<td valign="top" colspan="1">
		<b>Display Limit</b><br/>
		<input name="DisplayLimit" value="<form:value name="DisplayLimit"/>"/>
		<span class="warning"><form:error name="DisplayLimit"/></span>
	</td>
	<td valign="top" colspan="1">
    	<em>Optional: <b>custom entry form</b></em><br />
    	<input name="CustomEntryForm" size="30" value="<form:value name="CustomEntryForm"/>" />
    	<span class="warning"><form:error name="CustomEntryForm"/></span>
    	</td>
	</td>
</tr>

