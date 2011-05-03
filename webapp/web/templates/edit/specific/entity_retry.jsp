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



