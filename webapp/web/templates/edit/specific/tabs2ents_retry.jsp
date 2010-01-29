<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %> 
	<tr class="editformcell">
		<td valign="top">
			<b>Tab</b><br/>
				<select name="TabId" >
					<form:option name="TabId"/>
				</select>
				<font size="2" color="red"><form:error name="TabId"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="top">
			<b>Individual</b><br/>
				<select name="EntURI" >
					<form:option name="EntId"/>
				</select>
				<font size="2" color="red"><form:error name="EntId"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="1">
			<b>Order for display</b> in selected tab; blank will default to zero<br/>
				<input type="text" name="DisplayRank" value="<form:value name="DisplayRank"/>" size="5" maxlength="11" />
				<font size="2" color="red"><form:error name="DisplayRank"/></font>
		</td>
	</tr>
