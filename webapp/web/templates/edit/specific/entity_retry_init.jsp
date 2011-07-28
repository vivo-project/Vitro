<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>

    <tr class="editformcell" id="entityNameTr">
        <td valign="bottom" id="entityNameTd" colspan="1">
			<b>Individual Name</b><br/>
                <input type="text" name="field1Value" value="<form:value name="Name"/>" size="80" maxlength="255" />
                <font size="2" color="red"><form:error name="Name"/></font>
        </td>
        <td valign="top" id="displayStatusTd" colspan="1">
			<b>Display Status</b><br/>
		    <select name="StatusId" >
				<form:option name="StatusId"/>
			</select>
			<font size="2" color="red"><form:error name="StatusId"/></font>
        </td>
    </tr>
    <tr class='editformcell' id='GenericTypeTr'>
        <td valign="top" id="genericTypeTd" colspan="2">
			<b>Generic Type<br/>
			<select id="VclassId" name="VClassId" onChange="update();">
				<% // need to implement form:optgroup %>
					<form:option name="VClassId"/>
			</select>
			<br><font size="2" color="red"><form:error name="VClassId"/></font>
        </td>
    </tr>
    

