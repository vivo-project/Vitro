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
    <tr class="editformcell" id='citationTr'>
        <td id="citationTd" valign="bottom" colspan="2">
			<b>Citation</b> <em>Optional; max 255 chars; use also for image caption for news release
                <input type="text" name="Citation" value="<form:value name="Citation"/>" size="80" maxlength="255" />
                <font size="2" color="red"><form:error name="Citation"/></font>
        </td>
    </tr>
    <tr class="editformcell" id='sunriseTr'>
        <td id="sunriseTd" valign="bottom">
			<b>Sunrise</b>, or date first visible (e.g., 2004-09-17) <em>Optional; populated primarily for entities such as new releases that are auto-linked to tabs for a spcified number of days following their release date</em>
                <input type="text" name="Sunrise" value="<form:value name="Sunrise"/>" size="14" maxlength="14"><br>
                <font size="2" color="red"><form:error name="Sunrise"/></font>
        </td>
        <td id="sunsetTd" valign="bottom">
			<b>Sunset</b>, or date stops being visible (e.g., 2004-09-18) <em>Optional; used only to hide entities based on a date rather than the Display Status specified above.<br/>After this date, an entity will disappear except when searching in "entire database" mode</em>
                <input type="text" name="Sunset" value="<form:value name="Sunset"/>" size="19" maxlength="19"><br>
                <font size="2" color="red"><form:error name="Sunset"/></font>
        </td>
    </tr>
    <tr class="editformcell" id='timekeyTr'>
        <td id="timekeyTd" valign="bottom">
			<b>Timekey</b>, or date and time for event sort order (e.g., 2004-09-17 09:30:00) <em>Optional; populated primarily for entities such as seminars that are auto-linked to tabs for a specified number of days leading up to the event</em>
                <input type="text" name="Timekey" value="<form:value name="Timekey"/>" size="19" maxlength="19"><br>
                <font size="2" color="red"><form:error name="Timekey"/></font>
        </td>
    </tr>
    <tr class="editformcell" id='thumbnailFilenameTr'>
        <td id="thumbnailTd" valign="bottom" colspan="1">
			<b>Thumbnail Filename</b> <em>Optional and usually more convenient to upload from previous screen</em>
                <input type="text" name="ThumbnailFilename" value="<form:value name="ThumbnailFilename"/>" size="60" maxlength="255" />
                <font size="2" color="red"><form:error name="ThumbnailFilename"/></font>
        </td>
        <td id="optionalImageTd" valign="bottom" colspan="1">
			<b>Optional Larger Image</b> <em>(filename or full path)</em>
                <input type="text" name="LargerImage" value="<form:value name="LargerImage"/>" size="60" maxlength="255" />
                <font size="2" color="red"><form:error name="LargerImage"/></font>
        </td>
    </tr>
	<tr class="editformcell" id="portalFlagsTr">
       		<td id="portalFlagsTd" valign="bottom" colspan="3">
            	<b>portal</b> <i>(uncheck to hide in any portal)</i> <br />
      		</td>
	</tr>
	<tr class="editformcell" id="flag2SetTr">
        	<td id="collegeFlagsTd" valign="bottom" colspan="3">
            	<b>college</b>
       		</td>
 	</tr>
  	<tr class="editformcell" id="flag3SetTr">
        	<td id="campusFlagsTd" valign="bottom" colspan="3">
            	<b>campus</b>
       		</td>
   	</tr>

