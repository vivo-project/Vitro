<%-- $This file is distributed under the terms of the license in LICENSE$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<jsp:directive.page import="edu.cornell.mannlib.vedit.controller.BaseEditController"/>

<!--  Uri suppressions -->
<c:if test="${!empty uriSuppressions}">
    <input id="_uriSuppressions" type="hidden" name="_uriSuppressions" value="enabled" />
    <input id="${BaseEditController.ENTITY_URI_ATTRIBUTE_NAME}" type="hidden" name="${BaseEditController.ENTITY_URI_ATTRIBUTE_NAME}" value="${_permissionsEntityURI}" />
    <input id="${BaseEditController.ENTITY_TYPE_ATTRIBUTE_NAME}" type="hidden" name="${BaseEditController.ENTITY_TYPE_ATTRIBUTE_NAME}" value="INDIVIDUAL" />
	<c:forEach var="entry" items="${uriSuppressions}">
		<tr class="editformcell">
			<td valign="top" colspan="5">${i18n.text('suppress_operation_for_roles', entry.key)} <br /> 
				<c:set var="operationLowercase" value="${fn:toLowerCase(entry.key)}" />
				<c:forEach var="role" items="${entry.value}">
					<input id="uriSuppression${operationLowercase}${role.label}"
						type="checkbox" name="uriSuppression${operationLowercase}Roles"
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
<!-- Uri suppressions end -->


    <tr class="editformcell" id="entityNameTr">
        <td valign="bottom" id="entityNameTd" colspan="2">
			<b>Individual Name *</b><br/>
            <input style="width:80%;" type="text" name="Name" value="<form:value name="Name"/>" />
           <c:set var="NameError"><form:error name="Name"/></c:set>
            <c:if test="${!empty NameError}">
                <span class="notice"><c:out value="${NameError}"/></span>
            </c:if>
        </td>
    </tr>

    <!-- begin datatype properties section -->

    <tr class="editformcell" style="border-collapse:collapse;">
        <td colspan="2">
            <ul style="margin-left:0;padding-left:0;list-style-type:none">
	        <form:dynamicFields type="dataprops" usePage="entity_retry_dataprops.jsp"/>
            </ul>
	</td>
    </tr>

    <!-- end datatype properties section -->



