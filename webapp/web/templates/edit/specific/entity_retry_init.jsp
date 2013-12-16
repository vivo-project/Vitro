<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

    <tr class="editformcell" id="entityNameTr">
        <td valign="bottom" id="entityNameTd" colspan="1">
			<b>Individual Name</b><br/>
                <input type="text" name="field1Value" value="<form:value name="Name"/>" size="80" maxlength="255" />
            <c:set var="NameError"><form:error name="Name"/></c:set>
            <c:if test="${!empty NameError}">
                <span class="notice"><c:out value="${NameError}"/></span>
            </c:if>
        </td>
        <td valign="top" id="displayStatusTd" colspan="1">
			<b>Display Status</b><br/>
		    <select name="StatusId" >
				<form:option name="StatusId"/>
			</select>
        </td>
    </tr>
    <tr class='editformcell' id='GenericTypeTr'>
        <td valign="top" id="genericTypeTd" colspan="2">
			<b>Generic Type<br/>
			<select id="VclassId" name="VClassId" onChange="update();">
				<% // need to implement form:optgroup %>
					<form:option name="VClassId"/>
			</select>
        </td>
    </tr>
    

