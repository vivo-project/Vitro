<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<tr class="editformcell">
	<td valign="top" colspan="3">
		<b>Property group name</b> (max 120 characters)<br />
		<input type="text" name="Name" value="<form:value name="Name"/>" size="50" maxlength="120" />
		<font size="2" color="red"><form:error name="Name"/></font>
	</td>
</tr>
<tr class="editformcell">
	<td valign="top" colspan="3">
		<b>Public description</b> (short explanation for dashboard)<br />
		<input type="text" name="PublicDescription" value="<form:value name="PublicDescription"/>" size="80" maxlength="255" />
		<font size="2" color="red"><form:error name="PublicDescription"/></font>
	</td>
</tr>
<tr class="editformcell">
	<td valign="top" colspan="3">
		<b>Display rank </b> (lower number displays higher)<br />
		<input type="text" name="DisplayRank" value="<form:value name="DisplayRank"/>" size="3" maxlength="11" />
		<font size="2" color="red"><form:error name="DisplayRank"/></font>
	</td>
</tr>
