<?xml version="1.0" encoding="UTF-8"?>

<!-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="http://java.sun.com/jstl/core" xmlns:form="http://vitro.mannlib.cornell.edu/edit/tags" version="2.0">

	<tr class="editformcell">
		<td valign="bottom" colspan="2">
			<b>Class Name</b><i> for editing pick lists and the Index</i><br/>
			<i>... use initial capital letters; spaces OK</i><br/>
			<input type="text" name="Name" value="${formValue['Name']}" style="width:90%" maxlength="120" />
			<span class="warning"><form:error name="Name"/></span>
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
		<td valign="bottom" colspan="1">
			<b>Class Group</b><br/>
			<i>for search results and the Index</i><br/>
			<select name="GroupURI" ><form:option name="GroupURI"/></select>
			<span class="warning"><form:error name="GroupURI"/></span>
		</td>
		<td valign="bottom" colspan="2">
			<b>Ontology</b><br/>
            <c:choose>
                <c:when test="${_action eq 'update'}">
                    <i>Change via the "change URI" button on previous screen</i><br/>
                    <select name="Namespace" disabled="disabled"><form:option name="Namespace"/></select>
                </c:when>
                <c:otherwise>
				    <i>(must be a valid XML name without spaces)</i><br/>
				    <select name="Namespace"><form:option name="Namespace"/></select>
                </c:otherwise>
            </c:choose>
		</td>
		<td valign="bottom" colspan="1">
			<b>Internal Name*</b><br/>
            <c:choose>
                <c:when test="${_action eq 'update'}">
                    <i>Change via "change URI"</i><br/>
                    <input name="LocalName" disabled="disabled" value="${formValue['LocalName']}" style="width:90%"/>
                </c:when>
                <c:otherwise>
                	<i>must be valid XML</i><br/><i>by convention starts with a capital letter</i><br/>
                    <input name="LocalName" value="${formValue['LocalName']}" style="width:90%"/>
                </c:otherwise>
            </c:choose>
            <c:set var="LocalNameError"><form:error name="LocalName"/></c:set>
            <c:if test="${!empty LocalNameError}">
			    <span class="notice"><c:out value="${LocalNameError}"/></span>
			</c:if>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="top" colspan="4">
			<b>Short Definition</b><br/>
			<input type="text" name="ShortDef" value="${formValue['ShortDef']}" style="width:95%" maxlength="255" />
            <c:set var="ShortDefError"><form:error name="ShortDef"/></c:set>
            <c:if test="${!empty ShortDefError}">
                <span class="notice"><c:out value="${ShortDefError}"/></span>
            </c:if>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="top" colspan="4">
			<b>Example</b><br/>
			<input type="text" name="Example" value="${formValue['Example']}" style="width:95%" maxlength="120" />
            <c:set var="ExampleError"><form:error name="Example"/></c:set>
            <c:if test="${!empty ExampleError}">
                <span class="notice"><c:out value="${ExampleError}"/></span>
            </c:if>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="4">
			<b>Description</b><br/>
			<textarea style="width:95%;height:10ex;" name="Description"><form:value name="Description"/></textarea>
            <c:set var="DescriptionError"><form:error name="Description"/></c:set>
            <c:if test="${!empty DescriptionError}">
                <span class="notice"><c:out value="${DescriptionError}"/></span>
            </c:if>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="top" colspan="1">
			<b>Display Limit</b><br/>
			<input style="width:95%;" type="text" name="DisplayLimit" value="${formValue['DisplayLimit']}" maxlength="120" />
            <c:set var="DisplayLimitError"><form:error name="DisplayLimit"/></c:set>
            <c:if test="${!empty DisplayLimitError}">
                <span class="notice"><c:out value="${DisplayLimitError}"/></span>
            </c:if>
		</td>
		<td valign="top" colspan="1">
			<b>Display Rank</b><br/>
			<input size="4" type="text" name="DisplayRank" value="${formValue['DisplayRank']}" maxlength="120" />
            <c:set var="DisplayRankError"><form:error name="DisplayRank"/></c:set>
            <c:if test="${!empty DisplayRankError}">
                <span class="notice"><c:out value="${DisplayRankError}"/></span>
            </c:if>
		</td>
	</tr>
	<tr class="editformcell">
    	<td valign="bottom" colspan="1">
    		<em>Optional: <strong>custom entry form</strong></em><br />
    		<input name="CustomEntryForm" style="width:90%" value="${formValue['CustomEntryForm']}"/>
            <c:set var="CustomEntryFormError"><form:error name="CustomEntryForm"/></c:set>
            <c:if test="${!empty CustomEntryFormError}">
                <span class="notice"><c:out value="${CustomEntryFormError}"/></span>
            </c:if>
    	</td>
    	<td valign="bottom" colspan="1">
    		<em>Optional: <strong>custom display view</strong></em><br />
    		<input name="CustomDisplayView" style="width:90%" value="${formValue['CustomDisplayView']}"/>
            <c:set var="CustomEntryFormError"><form:error name="CustomEntryForm"/></c:set>
            <c:if test="${!empty CustomEntryFormError}">
                <span class="notice"><c:out value="${CustomEntryFormError}"/></span>
            </c:if>
    	</td>
    	<td valign="bottom" colspan="2">
    		<em>Optional: <strong>custom search view</strong></em><br />
    		<input name="CustomSearchView" style="width:45%" value="${formValue['CustomSearchView']}"/>
            <c:set var="CustomSearchViewError"><form:error name="CustomSearchView"/></c:set>
            <c:if test="${!empty CustomSearchViewError}">
                <span class="notice"><c:out value="${CustomSearchViewError}"/></span>
            </c:if>
    	</td>
	</tr>
</jsp:root>
