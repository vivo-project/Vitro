<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
	<tr class="editformcell">
		<td valign="bottom" colspan="2">
			<b>Tab Title*</b> <i>(a short phrase to appear on headings, menus or pick lists)</i><br/>
				<input type="text" name="Title" value="<form:value name="Title"/>" style="width:60%;" maxlength="80" />
				<font size="2" color="red"><form:error name="Title"/></font>
		</td>
		<td valign="bottom" colspan="1">
			<b>Tab Shorthand</b> <i>(an alternate phrase for display as <strong>More ...</strong> in the application)</i><br/>
				<input type="text" name="ShortHand" value="<form:value name="MoreTag"/>" style="width:80%;" maxlength="80" />
				<font size="2" color="red"><form:error name="MoreTag"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>Tab Description</b> <i>A single short sentence to summarize the contents for users</i><br/>
			<strong>Note that this now becomes the tool tip when the mouse rolls over a primary or secondary tab name</strong><br/>
				<input type="text" name="Description" value="<form:value name="Description"/>" style="width:90%;" maxlength="255" />
				<font size="2" color="red"><form:error name="Description"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>Tab Body</b> <i>enter any longer text or comment here</i><br/>
				<textarea name="Body" rows="10" wrap="soft"><form:value name="Rows"/><form:value name="Body"/></textarea>
				<font size="2" color="red"><form:error name="Body"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="top" colspan="1">
		<% /*
			<b>Originator</b><br>
				<input type="text" disabled="disabled" name="UserId" value="<form:value name="UserId"/>"/>
				<font size="2" color="red"><form:error name="UserId"/></font>
				*/ %>
		</td>
		<td valign="bottom" colspan="1">
			<b>Tab Type</b><br>
				<select name="TabtypeId" >
					<form:option name="TabtypeId"/>
				</select>
				<font size="2" color="red"><form:error name="TabtypeId"/></font>
		</td>
		<td valign="bottom" colspan="1">
			<b>Individual link method</b><br /><i>(if method is "by tab-type relationships", select types after editing this tab itself)</i><br />
				<select name="EntityLinkMethod" >
					<form:option name="EntityLinkMethod"/>
				</select>
				<font size="2" color="red"><form:error name="EntityLinkMethod"/></font>
		</td>	</tr>
	<tr class="editformcell"><td colspan="3"><hr /></td></tr>
	<% /*
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>URL of RSS feed</b> (must include the full http://... path)<br/>
					<input type="text" name="RssURL" value="<form:value name="RssURL"/>" style="width:60%;" maxlength="255" />
				<font size="2" color="red"><form:error name="RssURL"/></font>
		</td>
	</tr>

	*/ %>
	
	<tr class="editformcell">
		<td valign="bottom" colspan="1">
			<b>Order for display within parent tab</b><br />
				<input type="text" name="DisplayRank" value="<form:value name="DisplayRank"/>" size="5" maxlength="11" />
				<font size="2" color="red"><form:error name="DisplayRank"/></font>
		</td>
		<td valign="bottom" colspan="2">
			<b>Optional time limit for entities</b> <i>in days; use negative values for the past; not active for manual linking or image galleries</i><br/>
			<i>positive values will key off entity <b>timekey</b> field; negative values off entity <b>sunrise</b> field</i><br/>
				<input type="text" name="DayLimit" value="<form:value name="DayLimit"/>" size="5" maxlength="11" />
				<font size="2" color="red"><form:error name="DayLimit"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="1">
			<b>Sort field for related entities</b><br /><i><strong>name</strong>,rand(),timekey,sunset,sunrise</i><br/>
				<input type="text" name="EntitySortField" value="<form:value name="EntitySortField"/>" size="10" maxlength="50" />
				<font size="2" color="red"><form:error name="EntitySortField"/></font>
		</td>
		<td valign="bottom" colspan="2">
			<b>Sort direction for related entities</b><br /><i>blank for ascending,"desc" for descending</i><br/>
				<input type="text" name="EntitySortDirection" value="<form:value name="EntitySortDirection"/>" size="4" maxlength="4" />
				<font size="2" color="red"><form:error name="EntitySortDirection"/></font>
		</td>
	</tr>
	<tr class="editformcell"><td colspan="3"><hr /></td></tr>
	<tr class="editformcell"><td colspan="3">The following fields apply only when images are involved</td></tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="1">
			<b>Columns of images to display</b><br/>
			<i>9 columns of images at 94 pixels wide fits a 1024x768 display window</i><br/>
				<input type="text" name="GalleryCols" value="<form:value name="GalleryCols"/>" size="5" maxlength="11" />
				<font size="2" color="red"><form:error name="GalleryCols"/></font>
		</td>
		<td valign="bottom" colspan="1">
			<b>Rows of images to display</b><br/>
			<i>if only a single row is specified, the A|B|C...|Z choices don't appear with image galleries</i><br/>
				<input type="text" name="GalleryRows" value="<form:value name="GalleryRows"/>" size="5" maxlength="11" />
				<font size="2" color="red"><form:error name="GalleryRows"/></font>
		</td>
		<td valign="bottom" colspan="1">
			<b>Width for image display</i><br/>
				<input type="text" name="ImageWidth" value="<form:value name="ImageWidth"/>" size="5" maxlength="11" />
				<font size="2" color="red"><form:error name="ImageWidth"/></font>
		</td>
	</tr>
