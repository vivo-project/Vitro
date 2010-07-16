<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
	<tr class="editformcell">
		<td valign="top" colspan="3">
			<b>Individual Name</b><br/>
				<select name="IndividualURI" >
					<form:option name="IndividualURI"/>
 				</select>
				<font size="2" color="red"><form:error name="IndividualURI"/></font>
		</td>
	</tr>
	<tr class='editformcell'>
		<td valign="top" colspan="3">
			<b>Identifier Type</b><br>
				<select name="DatapropURI" >
					<form:option name="DatapropURI"/>
				</select>
				<br><font size="2" color="red"><form:error name="DatapropURI"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>Value</b><br/>
				<input type="text" name="Data" value="<form:value name="Data"/>" size="75%" maxlength="255" />
				<font size="2" color="red"><form:error name="Value"/></font>
		</td>
	</tr>
