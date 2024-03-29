<%-- $This file is distributed under the terms of the license in LICENSE$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<jsp:directive.page import="edu.cornell.mannlib.vedit.controller.BaseEditController"/>

<tr class="editformcell">
    <td valign="top" colspan="2">
        <b>Base property</b><br/>
        <input type="text" class="fullWidthInput" name="Base property" value="<form:value name="BaseLabel"/>" disabled="disabled" /></br>
        <i>a specification of this property</i></br>
    </td>
    <td valign="top" colspan="2">
	    <b>Property group</b><br/>
	    <select name="GroupURI"><form:option name="GroupURI"/></select><br/>
        <i>for grouping properties on individual pages</i><br/>
	</td>
</tr>

<tr class="editformcell">
    <td style="vertical-align:top;" valign="top" colspan="2">
        <br /><b>Label for public display</b><br/>
        <input type="text" class="fullWidthInput" name="DisplayName" value="<form:value name="DisplayName"/>" maxlength="80" />
        <c:set var="DisplayNameError"><form:error name="DisplayName"/></c:set>
        <c:if test="${!empty DisplayNameError}">
            <span class="notice"><c:out value="${DisplayNameError}"/></span>
        </c:if>
    </td>
</tr>

<tr><td colspan="5"><hr class="formDivider"/></td></tr>

<tr class="editformcell">
    <td valign="top" colspan="2">
        <b>Domain class</b><br />
        <select name="DomainURI"><form:option name="DomainURI"/></select>
    </td>
    <td valign="top" colspan="2">
        <b>Range class*</b><br />
        <select name="RangeURI" ><form:option name="RangeURI"/></select>
        <c:set var="RangeURIError"><form:error name="RangeURI"/></c:set>
        <c:if test="${!empty RangeURIError}">
            <span class="notice"><c:out value="${RangeURIError}"/></span>
        </c:if>
    </td>
</tr>

<tr><td colspan="5"><hr class="formDivider"/></td></tr>

<tr class="editformcell">
	<td valign="top" colspan="5">
		<b>Public Description</b> for front-end users, as it will appear on editing forms<br/>
	    <textarea class="matchingInput" name="PublicDescription"><form:value name="PublicDescription"/></textarea>
        <c:set var="PublicDescriptionError"><form:error name="PublicDescription"/></c:set>
        <c:if test="${!empty PublicDescriptionError}">
            <span class="notice"><c:out value="${PublicDescriptionError}"/></span>
        </c:if>
	</td>
</tr>

<tr><td colspan="5"><hr class="formDivider"/></td></tr>

<!-- Permissions -->
<c:if test="${!empty operationsToRoles}">
    <input id="_permissions" type="hidden" name="_permissions" value="enabled" />
    <input id="${BaseEditController.ENTITY_URI_ATTRIBUTE_NAME}" type="hidden" name="${BaseEditController.ENTITY_URI_ATTRIBUTE_NAME}" value="${_permissionsEntityURI}" />
    <input id="${BaseEditController.ENTITY_TYPE_ATTRIBUTE_NAME}" type="hidden" name="${BaseEditController.ENTITY_TYPE_ATTRIBUTE_NAME}" value="${_faux_property_type}" />
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
    <td valign="top" colspan="1">
        <b>Display tier</b> for this property<br/>
        <input type="text" class="shortInput" name="DisplayTier" value="<form:value name="DisplayTier"/>" /><br/>
            <i><b>lower</b> numbers display first</i><br/>
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
    <td valign="top" colspan="2">
        When displaying related individuals from different classes,<br/>
        <c:choose>
            <c:when test="${collateBySubclass}">
                <input name="CollateBySubclass" type="checkbox" value="TRUE" checked="checked"/>collate by subclass
            </c:when>
            <c:otherwise>
                <input name="CollateBySubclass" type="checkbox" value="TRUE"/>collate by subclass
            </c:otherwise>
        </c:choose>
    </td>
</tr>

<tr><td colspan="5"><hr class="formDivider"/></td></tr>

<tr class="editformcell">
    <td valign="top" colspan="2">
    	Select related individuals from existing choices?<br/>
       	<c:choose>
            <c:when test="${selectFromExisting}">
    	       	<input name="SelectFromExisting" type="checkbox" value="TRUE" checked="checked"/>provide selection
            </c:when>
            <c:otherwise>
               	<input name="SelectFromExisting" type="checkbox" value="TRUE"/>provide selection
            </c:otherwise>
        </c:choose>
    </td>
    <td valign="top" colspan="1">
    	Allow creating new related individuals?<br/>
       	<c:choose>
            <c:when test="${offerCreateNewOption}">
    	       	<input name="OfferCreateNewOption" type="checkbox" value="TRUE" checked="checked"/>offer create option
            </c:when>
            <c:otherwise>
               	<input name="OfferCreateNewOption" type="checkbox" value="TRUE"/>offer create option
            </c:otherwise>
        </c:choose>
    </td>
</tr>

<tr><td colspan="5"><hr class="formDivider"/></td></tr>

<tr class="editformcell">
    <td valign="top" colspan="5">
        <b>Custom entry form</b><br/>
        <input type="text" class="fullWidthInput" name="CustomEntryForm" value="<form:value name="CustomEntryForm"/>" />
            <c:set var="CustomEntryFormError"><form:error name="CustomEntryForm"/></c:set>
            <c:if test="${!empty CustomEntryFormError}">
                <span class="notice"><c:out value="${CustomEntryFormError}"/></span>
            </c:if>
    </td>
</tr>
<tr class="editformcell">
	    <td valign="top" colspan="5">
        <b>Custom list view</b><br/>
        <input type="text" class="fullWidthInput" name="CustomListView" value="<form:value name="CustomListView"/>" />
            <c:set var="CustomListViewError"><form:error name="CustomListView"/></c:set>
            <c:if test="${!empty CustomListViewError}">
                <span class="notice"><c:out value="${CustomListViewError}"/></span>
            </c:if>
    </td>
</tr>

<tr><td colspan="5"><hr class="formDivider"/></td></tr>
