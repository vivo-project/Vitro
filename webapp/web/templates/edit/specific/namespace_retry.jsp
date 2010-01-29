<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>

	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>Name (for convenience only)</b><br/>
				<input type="text" name="Name" value="<form:value name="Name"/>" size="40%" maxlength="120" />
				<font size="2" color="red"><form:error name="Name"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>Namespace URI</b><br/>
			<input name="NamespaceURI" value="<form:value name="NamespaceURI"/>" size="80%" maxlength="255" />
				<font size="2" color="red"><form:error name="NamespaceURI"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>Optional Namespace Prefix (for exports)</b><br/>
			<input name="Prefix" value="<form:value name="Prefix"/>" size="15%" maxlength="25" />
				<font size="2" color="red"><form:error name="Prefix"/></font>
		</td>
	</tr>


