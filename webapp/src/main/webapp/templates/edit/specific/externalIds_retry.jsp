<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
	<tr class="editformcell">
		<td valign="top" colspan="3">
			<b>Individual Name</b><br/>
				<select name="IndividualURI" >
					<form:option name="IndividualURI"/>
 				</select>
		</td>
	</tr>
	<tr class='editformcell'>
		<td valign="top" colspan="3">
			<b>Identifier Type</b><br>
				<select name="DatapropURI" >
					<form:option name="DatapropURI"/>
				</select>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>Value</b><br/>
				<input type="text" name="Data" value="<form:value name="Data"/>" size="75%" maxlength="255" />
            <c:set var="DataError"><form:error name="Data"/></c:set>
            <c:if test="${!empty DataError}">
                <span class="notice"><c:out value="${DataError}"/></span>
            </c:if>
		</td>
	</tr>
