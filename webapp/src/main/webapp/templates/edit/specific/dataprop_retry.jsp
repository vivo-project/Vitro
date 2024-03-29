<%-- $This file is distributed under the terms of the license in LICENSE$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<jsp:directive.page import="edu.cornell.mannlib.vedit.controller.BaseEditController"/>

<%-- colspan set to 4 in DatapropRetryController.java --%>
<tr class="editformcell">
	<td valign="top" colspan="2">
		<b>Public label</b><br/>
		<input type="text" class="fullWidthInput" name="PublicName" value="<form:value name="PublicName"/>"/>
        <c:set var="PublicNameError"><form:error name="PublicName"/></c:set>
        <c:if test="${!empty PublicNameError}">
            <span class="notice"><c:out value="${PublicNameError}"/></span>
        </c:if>
	</td>
	<td valign="top" colspan="2">
		<b>Property group</b><br/>
		<select name="GroupURI">
		    <form:option name="GroupURI"/>
		</select><br/>
		<i>for grouping properties on individual pages</i><br/>
	</td>
</tr>
<tr><td colspan="5"><hr class="formDivider"/></td></tr>
<tr class="editformcell">
	<!-- c:set var="existingLocalName" value="<form:value name='LocalName'/>"/ -->
	<td valign="top" colspan="2">
		<b>Ontology</b><br/>
        <c:choose>
        	<c:when test="${_action eq 'update'}">
				<select name="Namespace" disabled="disabled">
					<form:option name="Namespace"/>
				</select><br/>
        		<i>Edit via "change URI" on previous screen</i><br/>
			</c:when>
			<c:otherwise>
				<select name="Namespace">
					<form:option name="Namespace"/>
				</select><br/>
			</c:otherwise>
		</c:choose>
	</td>
	<td valign="top" colspan="2">
		<b>Internal name*</b> (RDF local name)<br/>
        <c:choose>
        	<c:when test="${_action eq 'update'}">
        		<input type="text" class="fullWidthInput" name="LocalName" value="<form:value name="LocalName"/>" disabled="disabled"/><br/>
				<i>Edit via &quot;change URI&quot;</i><br/>
			</c:when>
			<c:otherwise>
				<input type="text" class="fullWidthInput" name="LocalName" value="<form:value name="LocalName"/>"/><br/>
				<i>must be valid XML without spaces; by</i><br/>
				<i>convention use camel case with no initial capital</i><br/>
			</c:otherwise>
		</c:choose>
        <c:set var="LocalNameError"><form:error name="LocalName"/></c:set>
        <c:if test="${!empty LocalNameError}">
            <span class="notice"><c:out value="${LocalNameError}"/></span>
        </c:if>
	</td>
</tr>
<tr><td colspan="5"><hr class="formDivider"/></td></tr>
<tr class="editformcell">
	<td valign="top" colspan="2">
		<b>Domain class</b><br/>
		<select name="DomainClassURI">
			<form:option name="DomainClassURI"/>
		</select>
	</td>
	<td valign="top" colspan="2">
		<b>Range datatype</b><br/>
		<select name="RangeDatatypeURI">
			<form:option name="RangeDatatypeURI"/>
		</select><br/>
		<i>Use &quot;untyped&quot; instead of &quot;string&quot; to allow language tags</i>
	</td>
</tr>
<tr><td colspan="5"><hr class="formDivider"/></td></tr>
<tr class="editformcell">
    <td valign="top" colspan="4">
    	<b>Make this property be:</b><br/>
        <c:set var="functionalLabel" value=" functional"/>
        <c:choose>
             <c:when test="${functional}">
    	         <input name="Functional" type="checkbox" value="TRUE" checked="checked"/>${functionalLabel}<br/>
             </c:when>
             <c:otherwise>
                 <input name="Functional" type="checkbox" value="TRUE"/>${functionalLabel}<br/>
             </c:otherwise>
        </c:choose>
    	<i>A &quot;functional&quot; property has at most one value for each individual</i></br>
    </td>
</tr>
<tr><td colspan="5"><hr class="formDivider"/></td></tr>
<tr class="editformcell">
	<td valign="top" colspan="4">
		<b>Public description</b> for front-end users, as it will appear on editing forms<br/>
		<textarea class="matchingInput" name="PublicDescription"><form:value name="PublicDescription"/></textarea>
		<c:set var="PublicDescriptionError"><form:error name="PublicDescription"/></c:set>
		<c:if test="${!empty PublicDescriptionError}">
			<span class="notice"><c:out value="${PublicDescriptionError}"/></span>
		</c:if>
	</td>
</tr>
<tr class="editformcell">
    <td valign="top" colspan="4">
        <b>Example</b> for ontology editors<br/>
    	<textarea class="matchingInput" name="Example"><form:value name="Example"/></textarea>
        <c:set var="ExampleError"><form:error name="Example"/></c:set>
        <c:if test="${!empty ExampleError}">
            <span class="notice"><c:out value="${ExampleError}"/></span>
        </c:if>
    </td>
</tr>
<tr class="editformcell">
	<td valign="top" colspan="4">
		<b>Description</b> for ontology editors<br/>
		<textarea class="matchingInput" name="Description"><form:value name="Description"/></textarea>
        <c:set var="DescriptionError"><form:error name="Description"/></c:set>
        <c:if test="${!empty DescriptionError}">
            <span class="notice"><c:out value="${DescriptionError}"/></span>
        </c:if>
	</td>
</tr>
<tr><td colspan="5"><hr class="formDivider"/></td></tr>
<!-- Permissions -->
<c:if test="${!empty operationsToRoles}">
	<input id="_permissions" type="hidden" name="_permissions" value="enabled" />
	<input id="${BaseEditController.ENTITY_URI_ATTRIBUTE_NAME}" type="hidden" name="${BaseEditController.ENTITY_URI_ATTRIBUTE_NAME}" value="${_permissionsEntityURI}" />
	<input id="${BaseEditController.ENTITY_TYPE_ATTRIBUTE_NAME}" type="hidden" name="${BaseEditController.ENTITY_TYPE_ATTRIBUTE_NAME}" value="DATA_PROPERTY" />
	<c:forEach var="entry" items="${operationsToRoles}">
		<tr class="editformcell">
			<td valign="top" colspan="5">${i18n.text('operation_permissions_for_this_property', entry.key)}<br /> 
				<c:set var="operationLowercase" value="${fn:toLowerCase(entry.key)}" />
				<c:forEach var="role" items="${entry.value}">
					<input id="${operationLowercase}${role.label}"
						type="checkbox" name="${operationLowercase}Roles"
						value="${role.uri}" ${role.granted?'checked':''}
						${role.enabled?'':'disabled'} />
					<label class="inline" for="${operationLowercase}${role.label}">${role.label}</label>
				</c:forEach>
			</td>
		</tr>
		<tr>
			<td colspan="5">
				<hr class="formDivider" />
			</td>
		</tr>
	</c:forEach>
	<!-- Property suppressions (not related) -->
	<c:if test="${!empty propertySuppressionsNotRelated}">
	    <input id="${BaseEditController.PROPERTY_SUPPRESSIONS_NOT_RELATED}" type="hidden" name="${BaseEditController.PROPERTY_SUPPRESSIONS_NOT_RELATED}" value="enabled" />
		<c:forEach var="entry" items="${propertySuppressionsNotRelated}">
			<tr class="editformcell">
				<td valign="top" colspan="5">${i18n.text('suppress_operation_for_this_property_in_unrelated_individuals', entry.key)}<br /> 
					<c:set var="operationLowercase" value="${fn:toLowerCase(entry.key)}" />
					<c:forEach var="role" items="${entry.value}">
						<input id="propertySuppressionNotRelated${operationLowercase}${role.label}"
							type="checkbox" name="propertySuppressionNotRelated${operationLowercase}Roles"
							value="${role.uri}" ${role.granted?'checked':''}
							${role.enabled?'':'disabled'} />
						<label class="inline" for="${operationLowercase}${role.label}">${role.label}</label>
					</c:forEach>
				</td>
			</tr>
			<tr>
				<td colspan="5">
					<hr class="formDivider" />
				</td>
			</tr>
		</c:forEach>
	</c:if>
	<!-- Property suppressions (not related) end -->
	
</c:if>
<!-- Permissions End -->
<tr class="editformcell">
	<td valign="top" colspan="2">
		<b>Display tier</b> within property group<br/>
		<input type="text" class="shortInput" name="DisplayTier" value="<form:value name="DisplayTier"/>"/>
        <c:set var="DisplayTierError"><form:error name="DisplayTier"/></c:set>
        <c:if test="${!empty DisplayTierError}">
            <span class="notice"><c:out value="${DisplayTierError}"/></span>
        </c:if>
	</td>
	<td valign="top" colspan="2">
		<b>Display limit</b> before &quot;more ...&quot; button is displayed<br/>
		<input type="text" class="shortInput" name="DisplayLimit" value="<form:value name="DisplayLimit"/>"/>
        <c:set var="DisplayLimitError"><form:error name="DisplayLimit"/></c:set>
        <c:if test="${!empty DisplayLimitError}">
            <span class="notice"><c:out value="${DisplayLimitError}"/></span>
        </c:if>
	</td>
</tr>
<tr><td colspan="5"><hr class="formDivider"/></td></tr>
<tr class="editformcell">
	<td valign="top" colspan="2">
    	<b>Custom entry form</b><br/>
		<input type="text" class="fullWidthInput" name="CustomEntryForm" value="<form:value name="CustomEntryForm"/>" />
        <c:set var="CustomEntryFormError"><form:error name="CustomEntryForm"/></c:set>
        <c:if test="${!empty CustomEntryFormError}">
            <span class="notice"><c:out value="${CustomEntryFormError}"/></span>
        </c:if>
    	</td>
	</td>
	<td valign="top" colspan="2">
		<b>Rich text editing</b><br/>
		<select name="Editing">
		    <form:option name="Editing"/>
		</select>
	</td>
</tr>
<tr><td colspan="5"><hr class="formDivider"/></td></tr>

