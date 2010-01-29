<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

	<tr class="editformcell">
		<td valign="top" colspan="3">
			<b>Individual Name</b><br/>
				<select name="EntityId" >
					<form:option name="EntityId"/></option>
 				</select>
				<font size="2" color="red"><form:error name="EntityId"/></font>
		</td>
	</tr>
  <c:if test="${!empty epo.formObject.optionLists['TypeURI']}">
	<tr class='editformcell'>
		<td valign="top" colspan="3">
			<b>Link Type</b><br>
				<select name="TypeURI">
					<form:option name="TypeURI"/>
				</select>
				<br><font size="2" color="red"><form:error name="TypeURI"/></font>
		</td>
	</tr>
  </c:if>
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>URL</b> itself (http://...)<br/>
				<input type="text" name="Url" style="width:32em;" value="<form:value name="Url"/>"/>
				<font size="2" color="red"><form:error name="Url"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="2">
			<b>anchor text for above URL</b><br/>
				<input type="text" name="Anchor" value="<form:value name="Anchor"/>" style="width:24em;" maxlength="255" />
				<font size="2" color="red"><form:error name="Anchor"/></font>
		</td>
	</tr>

