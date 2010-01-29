<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
	<tr class="editformcell">
		<td valign="top" colspan="2">
			<b>Keyword</b><br />
				<input type="text" name="Term" value="<form:value name="Term"/>" size="60" maxlength="255" />
				<p><font size="2" color="red"><form:error name="Term"/></font></p>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="top" colspan="1">
			<b>Origin</b> <i>(source of 1st use of this keyword)</i><br/>
				<select name="Origin" >
					<form:option name="Origin"/>
				</select>
				<font size="2" color="red"><form:error name="Origin"/></font>
			</td>
			<td valign="top"><sup>*</sup>If [new origin] is selected, optionally enter a <b>new origin</b> here:<br/>
				<input type="text" name="Origin" size="30" maxlength="80" /><br>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>Comment</b> <i>limited to ~255 characters</i><br />
				<textarea name="Comments" ROWS="3" COLS="80" wrap="physical"><form:value name="Comments"/></textarea>
				<font size="2" color="red"><form:error name="Comments"/></font>
		</td>
	</tr>
