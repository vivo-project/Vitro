<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%-- colspan set to 4 in DatapropRetryController.java --%>
<tr class="editformcell">
	<td valign="top" colspan="1">
		<b>Public Name</b><br/><i>as will display on public pages</i><br/>
		<input name="PublicName" value="<form:value name="PublicName"/>"/>
        <c:set var="PublicNameError"><form:error name="PublicName"/></c:set>
        <c:if test="${!empty PublicNameError}">
            <span class="notice"><c:out value="${PublicNameError}"/></span>
        </c:if>
	</td>
	<td valign="top" colspan="1">
		<b>Property Group</b><br/>
		<i>(for display headers and dashboard)</i><br/>
		<select name="GroupURI">
		  <form:option name="GroupURI"/>
		</select>
	</td>
	<td valign="bottom" colspan="1">
        <b>Display Level</b><br /><i>(specify least restrictive level allowed)</i><br/>
        <select name="HiddenFromDisplayBelowRoleLevelUsingRoleUri"><form:option name="HiddenFromDisplayBelowRoleLevelUsingRoleUri"/></select>
    </td>
    <td valign="bottom" colspan="1">
        <b>Update Level</b><br /><i>(specify least restrictive level allowed)</i><br />
        <select name="ProhibitedFromUpdateBelowRoleLevelUsingRoleUri"><form:option name="ProhibitedFromUpdateBelowRoleLevelUsingRoleUri"/></select>
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
	</td>
	<td valign="top" colspan="2">
		<b>Local Name*</b>
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
        <c:set var="LocalNameError"><form:error name="LocalName"/></c:set>
        <c:if test="${!empty LocalNameError}">
            <span class="notice"><c:out value="${LocalNameError}"/></span>
        </c:if>
	</td>
</tr>

<tr class="editformcell">
	<td valign="top" colspan="2">
		<b>Domain Class</b><br/>
		<select name="DomainClassURI">
			<form:option name="DomainClassURI"/>
		</select>
	</td>
</tr>

<tr class="editformcell">
	<td valign="top" colspan="2">
		<b>Range Datatype</b><br/>
		<select name="RangeDatatypeURI">
			<form:option name="RangeDatatypeURI"/>
		</select>
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
        <c:set var="ExampleError"><form:error name="Example"/></c:set>
        <c:if test="${!empty ExampleError}">
            <span class="notice"><c:out value="${ExampleError}"/></span>
        </c:if>
    </td>
</tr>

<tr class="editformcell">
	<td valign="top" colspan="4">
		<b>Description</b> for ontology editors<br/>
		<textarea name="Description"><form:value name="Description"/></textarea>
        <c:set var="DescriptionError"><form:error name="Description"/></c:set>
        <c:if test="${!empty DescriptionError}">
            <span class="notice"><c:out value="${DescriptionError}"/></span>
        </c:if>
	</td>
</tr>

<tr class="editformcell">
	<td valign="top" colspan="4">
		<b>Public Description</b> for front-end users, as it will appear on editing forms<br/>
		<textarea name="PublicDescription"><form:value name="PublicDescription"/></textarea>
        <c:set var="PublicDescriptionError"><form:error name="PublicDescription"/></c:set>
        <c:if test="${!empty PublicDescriptionError}">
            <span class="notice"><c:out value="${PublicDescriptionError}"/></span>
        </c:if>
	</td>
</tr>

<tr class="editformcell">
	<td valign="top" colspan="1">
		<b>Display Tier</b><br/>
		<input name="DisplayTier" value="<form:value name="DisplayTier"/>"/>
        <c:set var="DisplayTierError"><form:error name="DisplayTier"/></c:set>
        <c:if test="${!empty DisplayTierError}">
            <span class="notice"><c:out value="${DisplayTierError}"/></span>
        </c:if>
	</td>
	<td valign="top" colspan="1">
		<b>Display Limit</b><br/>
		<input name="DisplayLimit" value="<form:value name="DisplayLimit"/>"/>
        <c:set var="DisplayLimitError"><form:error name="DisplayLimit"/></c:set>
        <c:if test="${!empty DisplayLimitError}">
            <span class="notice"><c:out value="${DisplayLimitError}"/></span>
        </c:if>
	</td>
	<td valign="top" colspan="1">
    	<em>Optional: <b>custom entry form</b></em><br />
    	<input name="CustomEntryForm" size="30" value="<form:value name="CustomEntryForm"/>" />
        <c:set var="CustomEntryFormError"><form:error name="CustomEntryForm"/></c:set>
        <c:if test="${!empty CustomEntryFormError}">
            <span class="notice"><c:out value="${CustomEntryFormError}"/></span>
        </c:if>
    	</td>
	</td>
</tr>

