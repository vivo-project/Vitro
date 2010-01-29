<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<tr class="editformcell">
		<td valign="top" colspan="3">
			<b>Class group name</b> (max 120 characters)<br />
				<input type="text" name="PublicName" value="<form:value name="PublicName"/>" size="50" maxlength="120" />
				<font size="2" color="red"><form:error name="Name"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="top" colspan="3">
			<b>Display rank </b> (lower number displays first)<br />
				<input type="text" name="DisplayRank" value="<form:value name="DisplayRank"/>" size="3" maxlength="11" />
				<font size="2" color="red"><form:error name="DisplayRank"/></font>
		</td>
	</tr>
