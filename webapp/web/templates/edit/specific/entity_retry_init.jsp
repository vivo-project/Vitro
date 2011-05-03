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
    <tr class='editformcell' id='specificTypeTr'>
        <td valign="top" id="specificTypeTd" colspan="1">
	        <b>Specific Type</b><br/>
				<select name="Moniker" id="Moniker">
					<form:option name="Moniker"/>
				</select>
				<br><font size="2" color="red"><form:error name="Moniker"/></font>
		</td>
		<td id="newMonikerTd" valign="top">If [new moniker] is selected, enter here:<br/>
                <input name="Moniker" value="<form:value name="Moniker"/>"/>
                <i>otherwise leave this field blank</i>
        </td>
    </tr>
    <tr class="editformcell" id='urlTr'>
        <td id="urlTd" valign="bottom" colspan="1">
			<b>URL</b><br />
				<input type="text" name="Url" value="<form:value name="Url"/>" size="80" maxlength="255" />
                <font size="2" color="red"><form:error name="Url"/></font>
        </td>
        <td id="urlAnchorTd" valign="bottom" colspan="1">
			<b>Anchor Text for URL</b> <em> Required if URL is to be visible</em><br />
				<input type="text" name="Anchor" value="<form:value name="Anchor"/>" size="30" maxlength="255" />
                <font size="2" color="red"><form:error name="Anchor"/></font>
        </td>
    </tr>
    <tr class="editformcell" id='blurbTr'>
        <td id="blurbTd" valign="bottom" colspan="2">
	        <b>Blurb</b> <em>Usually optional; shows up when this entity is included underneath a tab;</em> <b> max 255 chars</b><br />
                <input type="text" name="Blurb" value="<form:value name="Blurb"/>" size="153" maxlength="255" />
                <font size="2" color="red"><form:error name="Blurb"/></font>
        </td>
    </tr>
    <tr class="editformcell" id='descriptionTr'>
        <td id="descriptionTd" valign="bottom" colspan="2">
        	<b>Description</b> <em>Optional; may include valid HTML</em><br/>
                <textarea name="Description" ROWS="5" COLS="115" wrap="physical"><form:value name="Description"/></textarea>
                <font size="2" color="red"><form:error name="Description"/></font>
        </td>
    </tr>
    

