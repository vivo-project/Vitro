<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
	<tr class="editformcell">
		<td valign="bottom" colspan="2">
			<b>Keyword</b><br/>
				<select name="KeyId">
					<form:option name="KeyId"/>
				</select>
				<font size="2" color="red"><form:error name="KeyId"/></font>
		</td>
	</tr>
