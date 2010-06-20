<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>

    <tr class="editformcell" id="entityNameTr">
        <td valign="bottom" id="entityNameTd" colspan="2">
			<b>Individual Name</b><br/>
            <input style="width:80%;" type="text" name="Name" value="<form:value name="Name"/>" maxlength="255" />
            <p class="error"><form:error name="Name"/></p>
        </td>
    </tr>

    <tr class='editformcell' id='GenericTypeTr'>
        <td valign="top" id="genericTypeTd" colspan="2">
			<b>Generic Type<br/>
			<select disabled="disabled" id="VClassURI" name="VClassURI" onChange="update();">
					<form:option name="VClassURI"/>
			</select>
			<p class="error"><form:error name="VClassURI"/></p>
        </td>
    </tr>

    
    <tr class='editformcell' id='specificTypeTr'>

        <td style="width:50%;" valign="top" id="specificTypeTd" colspan="1">
	        <b>Specific Type</b><br/>
				<select style="width:80%;" name="Moniker" id="Moniker">
					<form:option name="Moniker"/>
				</select>
				<p class="error"><form:error name="Moniker"/></p>
		</td>
		
		<td id="newMonikerTd" valign="top">If [new moniker] is selected, enter here:<br/>
                <input id="MonikerSelectAlt" name="Moniker" value="<form:value name="Moniker"/>"/>
                <i>otherwise leave this field blank</i>
        </td>
    </tr>
    <tr class="editformcell" id='urlTr'>
        <td id="urlTd" valign="bottom" colspan="1">
			<b>URL</b><br />
				<input style="width:80%;" type="text" name="Url" value="<form:value name="Url"/>" maxlength="255" />
                <p><form:error name="Url"/></p>
        </td>
        <td id="urlAnchorTd" valign="bottom" colspan="1">
			<b>Anchor Text for URL</b> <em> Required if URL is to be visible</em><br />
				<input style="width:65%;" type="text" name="Anchor" value="<form:value name="Anchor"/>" maxlength="255" />
                <p class="error"><form:error name="Anchor"/></p>
        </td>
    </tr>

    <!-- begin datatype properties section -->
    
    <tr class="editformcell" style="border-collapse:collapse;">
        <td colspan="2">
            <ul style="margin-left:0;padding-left:0;list-style-type:none">
	        <form:dynamicFields type="dataprops" usePage="entity_retry_dataprops.jsp"/>
            </ul>
	</td>
    </tr>

    <!-- end datatype properties section -->    
		
	    
    <tr class="editformcell" id='blurbTr'>
        <td id="blurbTd" valign="bottom" colspan="2">
	        <b>Blurb</b> <em>Usually optional; shows up when this entity is included underneath a tab;</em> <b> max 255 chars</b><br />
                <input style="width:80%;" type="text" name="Blurb" value="<form:value name="Blurb"/>" maxlength="255" />
                <p class="error"><form:error name="Blurb"/></p>
        </td>
    </tr>
    <tr class="editformcell" id='descriptionTr'>
        <td id="descriptionTd" valign="bottom" colspan="2">
        	<b>Description</b> <em>Optional.  Consider using more specific datatype properties where possible.</em><br/>
                <textarea id="Description" name="Description" ROWS="15" COLS="115" wrap="physical"><form:value name="Description"/></textarea>
                <p class="error"><form:error name="Description"/></p>
        </td>
    </tr>
    <tr class="editformcell" id='citationTr'>
        <td id="citationTd" valign="bottom" colspan="2">
			<b>Citation</b> <em>Optional; max 255 chars; use also for image caption for news release<br/>
                <input style="width:60%;" type="text" name="Citation" value="<form:value name="Citation"/>" maxlength="255" />
                <p class="error"><form:error name="Citation"/></p>
        </td>
    </tr>
    <tr class="editformcell" id='sunriseTr'>
        <td id="sunriseTd" valign="bottom">
			<b>Sunrise</b>, or date first visible (e.g., 2004-09-17) <br/><em>Optional; populated primarily for entities such as new releases that are auto-linked to tabs for a spcified number of days following their release date</em><br/>
                <input type="text" name="Sunrise" value="<form:value name="Sunrise"/>" size="14" maxlength="14"><br>
                <p class="error"><form:error name="Sunrise"/></p>
        </td>
        <td id="sunsetTd" valign="bottom">
			<b>Sunset</b>, or date stops being visible (e.g., 2004-09-18) <br/><em>Optional; used only to hide entities based on a date rather than the Display Status specified above.<br/>After this date, an entity will disappear except when searching in "entire database" mode</em><br/>
                <input type="text" name="Sunset" value="<form:value name="Sunset"/>" size="19" maxlength="19"><br>
                <p class="error"><form:error name="Sunset"/></p>
        </td>
    </tr>
    <tr class="editformcell" id='timekeyTr'>
        <td id="timekeyTd" valign="bottom">
			<b>Timekey</b>, or date and time for event sort order (e.g., 2004-09-17 09:30:00) <br/><em>Optional; populated primarily for entities such as seminars that are auto-linked to tabs for a specified number of days leading up to the event</em><br/>
                <input type="text" name="Timekey" value="<form:value name="Timekey"/>" size="19" maxlength="19"><br>
                <p class="error"><form:error name="Timekey"/></p>
        </td>
    </tr>



