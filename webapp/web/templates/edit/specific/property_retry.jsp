<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<%-- colspan set to 4 in PropertyRetryController.java --%>
<tr class="editformcell">
    <td valign="top" colspan="1">
        <b>Parent property</b><br/><br/>
        <select name="ParentURI"><form:option name="ParentURI"/></select>
    </td>
    <td valign="top" colspan="1">
	    <b>Property Group</b><br />
	    <em>(for display headers and dashboard)</em><br/>
	    <select name="GroupURI"><form:option name="GroupURI"/></select>
	</td>
	<td valign="bottom" colspan="1">
        <b>Display Level</b><br /><i>(specify least restrictive level allowed)</i><br/>
        <select name="HiddenFromDisplayBelowRoleLevelUsingRoleUri"><form:option name="HiddenFromDisplayBelowRoleLevelUsingRoleUri"/></select>
    </td>
    <td valign="bottom" colspan="1">
        <b>Update Level</b><br />(specify least restrictive level allowed)<br />
        <select name="ProhibitedFromUpdateBelowRoleLevelUsingRoleUri"><form:option name="ProhibitedFromUpdateBelowRoleLevelUsingRoleUri"/></select>
    </td>
</tr>
<tr class="editformcell">
	<td style="vertical-align:bottom;" valign="bottom" colspan="1">
		<b>Ontology</b>
        <c:choose>
        	<c:when test="${_action eq 'update'}">
        		<br/><i>Change only via the "change URI" button on the previous screen</i><br/>
				<select name="Namespace" disabled="disabled"><form:option name="Namespace"/></select>
			</c:when>
			<c:otherwise>
				<br/><select name="Namespace"><form:option name="Namespace"/></select>
			</c:otherwise>
		</c:choose>
	</td>
	<td style="vertical-align:bottom;" valign="bottom" colspan="1">
		<b>Local name for property*</b>
        <c:choose>
        	<c:when test="${_action eq 'update'}">
        		<br/><i>Change only via the "change URI" button on the previous screen</i><br/>
        		<input name="LocalName" value="<form:value name="LocalName"/>" style="width:90%;" disabled="disabled"/>
        	</c:when>
        	<c:otherwise>
				<br/><i>(must be a valid XML name without spaces)</i><br/>
				<input name="LocalName" value="<form:value name="LocalName"/>" style="width:90%;"/>
			</c:otherwise>
		</c:choose>
        <c:set var="LocalNameError"><form:error name="LocalName"/></c:set>
        <c:if test="${!empty LocalNameError}">
            <span class="notice"><c:out value="${LocalNameError}"/></span>
        </c:if>
	</td>
    <td style="vertical-align:bottom;" valign="bottom" colspan="2">
        <b>Optional: Label for public display</b><br />
        <input type="text" name="DomainPublic" value="<form:value name="DomainPublic"/>" style="width:90%;" maxlength="80" />
        <c:set var="DomainPublicError"><form:error name="DomainPublic"/></c:set>
        <c:if test="${!empty DomainPublicError}">
            <span class="notice"><c:out value="${DomainPublicError}"/></span>
        </c:if>
    </td>
</tr>
<tr class="editformcell">
    <td style="vertical-align:bottom;" valign="bottom" colspan="1">
		<b>Optional: Inverse property ontology</b>
        <c:choose>
        	<c:when test="${_action eq 'update'}">
        		<br/><i>Change only via the "change URI" button on the previous screen</i><br/>
				<select name="NamespaceInverse" disabled="disabled"><form:option name="NamespaceInverse"/></select>
			</c:when>
			<c:otherwise>
				<br/><select name="NamespaceInverse"><form:option name="NamespaceInverse"/></select>
			</c:otherwise>
		</c:choose>
	</td>
    <td style="vertical-align:bottom;" valign="bottom" colspan="1">
		<b>Optional: Inverse property local name</b>
        <c:choose>
        	<c:when test="${_action eq 'update'}">
        		<br/><i>Change only via the "change URI" button on the previous screen</i><br/>
				<input name="LocalNameInverse" value="<form:value name="LocalNameInverse"/>" style="width:90%;" disabled="disabled"/>
			</c:when>
			<c:otherwise>
				<br/><i>(must be a valid XML name without spaces)</i><br/>
				<input name="LocalNameInverse" value="<form:value name="LocalNameInverse"/>" style="width:90%;"/>
			</c:otherwise>
		</c:choose>
        <c:set var="LocalNameInverseError"><form:error name="LocalNameInverse"/></c:set>
        <c:if test="${!empty LocalNameInverseError}">
            <span class="notice"><c:out value="${LocalNameInverseError}"/></span>
        </c:if>
	</td>
    <td valign="bottom" style="vertical-align:bottom;" colspan="2">
        <b>Optional: Inverse property label for public display</b><br />
        <input type="text" name="RangePublic" value="<form:value name="RangePublic"/>" style="width:90%;" maxlength="80" />
        <c:set var="RangePublicError"><form:error name="RangePublic"/></c:set>
        <c:if test="${!empty RangePublicError}">
            <span class="notice"><c:out value="${RangePublicError}"/></span>
        </c:if>
    </td>
</tr>
<tr class="editformcell">
    <td valign="top" colspan="2">
        <b>Domain class</b><br />
        <select name="DomainVClassURI"><form:option name="DomainVClassURI"/></select>
    </td>
    <td valign="top" colspan="2">
        <b>Range class</b><br />
        <select name="RangeVClassURI" ><form:option name="RangeVClassURI"/></select>
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
    <td valign="bottom" colspan="1">
        <c:choose>
            <c:when test="${transitive}">
                <input name="Transitive" type="checkbox" value="TRUE" checked="checked"/>transitive
            </c:when>
            <c:otherwise>
                <input name="Transitive" type="checkbox" value="TRUE"/>transitive
            </c:otherwise>
        </c:choose>
    </td>
    <td valign="bottom" colspan="1">
        <c:choose>
            <c:when test="${symmetric}">
                <input name="Symmetric" type="checkbox" value="TRUE" checked="checked"/> symmetric
            </c:when>
            <c:otherwise>
                <input name="Symmetric" type="checkbox" value="TRUE"/> symmetric
            </c:otherwise>
        </c:choose>
    </td>
    <td valign="bottom" colspan="1">
        <c:choose>
             <c:when test="${functional}">
                 <input name="Functional" type="checkbox" value="TRUE" checked="checked"/> functional
             </c:when>
             <c:otherwise>
                 <input name="Functional" type="checkbox" value="TRUE"/> functional
             </c:otherwise>
        </c:choose>
    </td>
    <td valign="bottom" colspan="1">
        <c:choose>
             <c:when test="${inverseFunctional}">
                 <input name="InverseFunctional" type="checkbox" value="TRUE" checked="checked"/> inverse functional
             </c:when>
             <c:otherwise>
                 <input name="InverseFunctional" type="checkbox" value="TRUE"/> inverse functional
             </c:otherwise>
        </c:choose>
    </td>
</tr>
<tr class="editformcell">
    <td valign="bottom" colspan="1">
        <em>Optional: display tier for this property<br />
	    (<strong>lower</strong> numbers display first)</em><br/>
        <input name="DomainDisplayTier" value="<form:value name="DomainDisplayTier"/>" style="width:15%;" />
        <c:set var="DomainDisplayTierError"><form:error name="DomainDisplayTier"/></c:set>
        <c:if test="${!empty DomainDisplayTierError}">
            <span class="notice"><c:out value="${DomainDisplayTierError}"/></span>
        </c:if>
    </td>
    <td valign="bottom" colspan="1">
        <em>Optional: display tier for this property's inverse<br />
	    (<strong>lower</strong> numbers display first)</em><br/>
        <input name="RangeDisplayTier" value="<form:value name="RangeDisplayTier"/>" style="width:15%;" />
        <c:set var="RangeDisplayTierError"><form:error name="RangeDisplayTier"/></c:set>
        <c:if test="${!empty RangeDisplayTierError}">
            <span class="notice"><c:out value="${RangeDisplayTierError}"/></span>
        </c:if>
    </td>
    <td valign="bottom" colspan="1">
        <em>Related <strong>object individuals</strong> to display without collapsing<br />
	    (<strong>lower</strong> numbers display first)</em><br/>
        <input name="DomainDisplayLimit" value="<form:value name="DomainDisplayLimit"/>" style="width:15%;" />
        <c:set var="DomainDisplayLimitError"><form:error name="DomainDisplayLimit"/></c:set>
        <c:if test="${!empty DomainDisplayLimitError}">
            <span class="notice"><c:out value="${DomainDisplayLimitError}"/></span>
        </c:if>
    </td>    
    <td valign="bottom" colspan="1">
        <em>Related <strong>object individuals of inverse property</strong> to display without collapsing</em><br />
        (<strong>lower</strong> numbers display first)<br/>
        <input name="RangeDisplayLimit" value="<form:value name="RangeDisplayLimit"/>" style="width:15%;" />
        <c:set var="RangeDisplayLimitError"><form:error name="RangeDisplayLimit"/></c:set>
        <c:if test="${!empty RangeDisplayLimitError}">
            <span class="notice"><c:out value="${RangeDisplayLimitError}"/></span>
        </c:if>
    </td>
</tr>
<tr class="editformcell">
    <td valign="bottom" colspan="1">      
        <c:choose>
            <c:when test="${collateBySubclass}">
                <input name="CollateBySubclass" type="checkbox" value="TRUE" checked="checked"/>collate by subclass
            </c:when>
            <c:otherwise>
                <input name="CollateBySubclass" type="checkbox" value="TRUE"/>collate by subclass
            </c:otherwise>            
        </c:choose>            
    </td>
	<td valign="bottom" colspan="1">
		<em><strong>Caution:</strong>delete object when statement deleted?</em><br />
   		<c:choose>
        	<c:when test="${stubObjectRelation}">
	        	<input name="StubObjectRelation" type="checkbox" value="TRUE" checked="checked"/>stub object relation with force delete
        	</c:when>
        	<c:otherwise>
            	<input name="StubObjectRelation" type="checkbox" value="TRUE"/>stub object relation with force delete
        	</c:otherwise>
    	</c:choose>
	</td>
	<td valign="bottom" colspan="1">
		<em>select from existing choices when adding statements?</em><br />
   		<c:choose>
        	<c:when test="${selectFromExisting}">
	        	<input name="SelectFromExisting" type="checkbox" value="TRUE" checked="checked"/>provide selection
        	</c:when>
        	<c:otherwise>
            	<input name="SelectFromExisting" type="checkbox" value="TRUE"/>provide selection
        	</c:otherwise>
    	</c:choose>
	</td>
	<td valign="bottom" colspan="1">
		<em>when adding a new statement, also offer option to create new individual?</em><br />
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
<tr class="editformcell">  
    <td valign="bottom" colspan="4">
        <em>custom entry form</em><br />
        <input name="CustomEntryForm" size="30" value="<form:value name="CustomEntryForm"/>" />
        <c:set var="CustomEntryFormError"><form:error name="CustomEntryForm"/></c:set>
        <c:if test="${!empty CustomEntryFormError}">
            <span class="notice"><c:out value="${CustomEntryFormError}"/></span>
        </c:if>
    </td>
</tr>
<tr class="editformcell">
    <td valign="bottom" colspan="4">
        <b>Example</b><br />
        <input name="Example" style="width:90%;" value="<form:value name="Example"/>" />
        <c:set var="ExampleError"><form:error name="Example"/></c:set>
        <c:if test="${!empty ExampleError}">
            <span class="notice"><c:out value="${ExampleError}"/></span>
        </c:if>
    </td>
</tr>
    <tr class="editformcell">
    <td valign="bottom" colspan="4">
        <b>Description</b> for ontology editors<br />
        <textarea name="Description" style="width:90%;"><form:value name="Description"/></textarea>
        <c:set var="DescriptionError"><form:error name="Description"/></c:set>
        <c:if test="${!empty DescriptionError}">
            <span class="notice"><c:out value="${DescriptionError}"/></span>
        </c:if>
    </td>
</tr>

