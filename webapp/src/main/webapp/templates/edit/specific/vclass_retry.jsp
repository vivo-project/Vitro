<?xml version="1.0" encoding="UTF-8"?>

<!-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="http://java.sun.com/jsp/jstl/core" xmlns:form="http://vitro.mannlib.cornell.edu/edit/tags" version="2.0">

<tr class="editformcell">
	<td valign="bottom" colspan="2">
		<b>Class label</b><br/>
	    <input type="text" class="fullWidthInput" name="Name" value="${formValue['Name']}" maxlength="120" /><br/>
		<i>by convention use initial capital letters; spaces OK</i><br/>
		<span class="warning"><form:error name="Name"/></span>
	</td>
	<td valign="bottom" colspan="2">
		<b>Class group</b><br/>
		<select name="GroupURI" ><form:option name="GroupURI"/></select><br/>
		<i>for menu pages, search results and the index page</i><br/>
		<span class="warning"><form:error name="GroupURI"/></span>
	</td>
</tr>
<tr><td colspan="4"><hr class="formDivider"/></td></tr>
<tr class="editformcell">
	<td valign="bottom" colspan="2">
		<b>Ontology</b><br/>
        <c:choose>
            <c:when test="${_action eq 'update'}">
                <select name="Namespace" disabled="disabled"><form:option name="Namespace"/></select><br/>
                <i>Edit via "change URI" on previous screen</i><br/>
            </c:when>
            <c:otherwise>
				<select name="Namespace"><form:option name="Namespace"/></select>
            </c:otherwise>
        </c:choose>
	</td>
	<td valign="bottom" colspan="1">
		<b>Internal name*</b> (RDF local name)<br/>
        <c:choose>
            <c:when test="${_action eq 'update'}">
                <input type="text" class="fullWidthInput" disabled="disabled" name="LocalName" value="${formValue['LocalName']}" /><br/>
                <i>Edit via "change URI"</i><br/>
            </c:when>
            <c:otherwise>
                <input type="text" class="fullWidthInput" name="LocalName" value="${formValue['LocalName']}" /><br/>
                <i>must be valid XML without spaces; by</i><br/>
            	<i>convention use camel case with an initial capital</i><br/>
            </c:otherwise>
        </c:choose>
        <c:set var="LocalNameError"><form:error name="LocalName"/></c:set>
        <c:if test="${!empty LocalNameError}">
			<span class="notice"><c:out value="${LocalNameError}"/></span>
		</c:if>
	</td>
</tr>
<tr><td colspan="4"><hr class="formDivider"/></td></tr>
<tr class="editformcell">
	<td valign="top" colspan="4">
		<b>Short definition</b> to display publicly<br/>
	    <input type="text" class="fullWidthInput" name="ShortDef" value="${formValue['ShortDef']}" maxlength="255" />
        <c:set var="ShortDefError"><form:error name="ShortDef"/></c:set>
        <c:if test="${!empty ShortDefError}">
            <span class="notice"><c:out value="${ShortDefError}"/></span>
        </c:if>
	</td>
</tr>
<tr class="editformcell">
	<td valign="top" colspan="4">
		<b>Example</b> for ontology editors<br/>
	    <input type="text" class="fullWidthInput" name="Example" value="${formValue['Example']}" maxlength="120" />
        <c:set var="ExampleError"><form:error name="Example"/></c:set>
        <c:if test="${!empty ExampleError}">
            <span class="notice"><c:out value="${ExampleError}"/></span>
        </c:if>
	</td>
</tr>
<tr class="editformcell">
	<td valign="bottom" colspan="4">
		<b>Description</b> for ontology editors<br/>
		<textarea class="matchingInput" name="Description"><form:value name="Description"/></textarea>
        <c:set var="DescriptionError"><form:error name="Description"/></c:set>
        <c:if test="${!empty DescriptionError}">
            <span class="notice"><c:out value="${DescriptionError}"/></span>
        </c:if>
	</td>
</tr>
<tr><td colspan="4"><hr class="formDivider"/></td></tr>
<tr class="editformcell">
    <td valign="bottom" colspan="1">
        <b>Display level</b><br/>
        <select name="HiddenFromDisplayBelowRoleLevelUsingRoleUri">
            <form:option name="HiddenFromDisplayBelowRoleLevelUsingRoleUri"/>
        </select>
    </td>
    <td valign="bottom" colspan="1">
        <b>Update level</b><br/>
        <select name="ProhibitedFromUpdateBelowRoleLevelUsingRoleUri">
            <form:option name="ProhibitedFromUpdateBelowRoleLevelUsingRoleUri"/>
        </select>
    </td>
</tr>
<tr class="editformcell">
    <td valign="bottom" colspan="1">
        <b>Publish level</b><br/>
        <select name="HiddenFromPublishBelowRoleLevelUsingRoleUri">
            <form:option name="HiddenFromPublishBelowRoleLevelUsingRoleUri"/>
        </select>
    </td>
</tr>
<tr><td colspan="4"><hr class="formDivider"/></td></tr>
<tr class="editformcell">
	<!--td valign="top" colspan="1">
		<b>Display Limit</b><br/>
		<input style="width:95%;" type="text" name="DisplayLimit" value="${formValue['DisplayLimit']}" maxlength="120" />
        <c:set var="DisplayLimitError"><form:error name="DisplayLimit"/></c:set>
        <c:if test="${!empty DisplayLimitError}">
            <span class="notice"><c:out value="${DisplayLimitError}"/></span>
        </c:if>
    </td-->
	<td valign="top" colspan="2">
		<b>Display rank</b> when collating property by subclass<br/>
		<input type="text" class="shortInput" name="DisplayRank" value="${formValue['DisplayRank']}" maxlength="3" />
        <c:set var="DisplayRankError"><form:error name="DisplayRank"/></c:set>
        <c:if test="${!empty DisplayRankError}">
            <span class="notice"><c:out value="${DisplayRankError}"/></span>
        </c:if>
	</td>
	<td valign="top" colspan="2">
		<b>Custom entry form</b><br/>
		<input type="text" class="fullWidthInput" name="CustomEntryForm" value="${formValue['CustomEntryForm']}" maxlength="120"/>
		<c:set var="CustomEntryFormError"><form:error name="CustomEntryForm"/></c:set>
		<c:if test="${!empty CustomEntryFormError}">
			<span class="notice"><c:out value="${CustomEntryFormError}"/></span>
		</c:if>
	</td>
</tr>
<tr><td colspan="4"><hr class="formDivider"/></td></tr>
</jsp:root>
