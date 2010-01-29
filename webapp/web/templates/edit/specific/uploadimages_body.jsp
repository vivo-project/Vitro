<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table cellpadding="1" cellspacing="1" border="0" width="100%">
<c:choose>
	<c:when test="${!empty processError}">
		<tr>
			<td align="center" colspan="7">
				<font color="red">Results from processing input file:</font><br/>
				${processError}
				<c:if test="${!empty outputLink}">
					<br/><font color="red">Link to check uploaded image:</font><br/>
					<${outputLink}>
					<c:if test="${processErrorUpdated}">
						<p>
						<form action="entity" method="get">
							<input type="hidden" name="home" value="${portalBean.portalId}"/>
							<input type="submit" class="form-button" value="view updated record"/>
							<input type="hidden" name="entityUri" value="${individual.URI}"/>
						</form>
						</p>
					</c:if>
				</c:if>
			</td>
		</tr>
	</c:when>
</c:choose>
<tr><td align="center" colspan="7">
		<form name="uploadForm" action="uploadImages" method="post" ENCTYPE="multipart/form-data">
			<input type="hidden" name="home" value="${portalBean.portalId}"/>
			<input type="hidden" name="submitter" value="${loginName}"/>
			<input type="hidden" name="destination" value="images" />
			<input type="hidden" name="contentType" value="image/jpeg/gif/pjpeg" />
    	<table width="100%" cellpadding="4" cellspacing="2" border="1">
			<tr>
				<td bgcolor="#C8D8F8"  valign="middle" colspan="1" align="right">
					<b>Individual</b>
				</td>
				<td bgcolor="#C8D8F8" valign="middle" align="left" colspan="2">
					<c:choose>
						<c:when test="${individual != null}">
							<select name="entityUri" style="width:95%;">
								<option value="${individual.URI}">${individual.name}</option>
							</select>
						</c:when>
						<c:otherwise>
							<p>No individuals match the incoming parameters or no individual was specified in the request</p>
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
			<tr>
				<td bgcolor="#C8D8F8"  valign="middle" colspan="1" align="right">
					<b>Select Image Type</b><br/>
				</td>
				<td colspan="2" bgcolor="#C8D8F8" align="left">
					<input type="radio" name="type" value="thumb" checked="checked" onclick="refreshModeValue();" /> thumbnail (150px x 150px only)
					<input type="radio" name="type" value="larger" onclick="refreshModeValue();" /> optional larger image
				</td>
			</tr>
			<tr>
				<td bgcolor="#C8D8F8"  valign="middle" colspan="1" align="right">
					<b>Select Image File</b>
					<c:if test="${!empty inputLink}">
						<br/>${inputLink}
					</c:if>
				</td>
				<td colspan="2" bgcolor="#C8D8F8" align="left">
					<input type="file" size="55" name="file1"/>
				</td>
			</tr>
			<tr>
				<td bgcolor="#C8D8F8"  valign="middle" colspan="1" align="right">
					<b>Optional remote image link</b>
				</td>
				<td colspan="2" bgcolor="#C8D8F8" align="left">
					<div id="thumbnailExtra" class="dropdownExtra">
						(instead of uploading larger image -- use only when uploading thumbnail)<br/>
						<input type="text" size="55" name="remoteURL" value="http://"/>
					</div>
				</td>
			</tr>
			<tr>
				<td bgcolor="#C8D8F8"  valign="middle" colspan="1" align="right">
					<b>Destination Directory</b><br/>
				</td>
				<td colspan="2" bgcolor="#C8D8F8" align="left">
					<input type="radio" name="destination" value="buildings" /> buildings<br/>
					<input type="radio" name="destination" value="events" /> events<br/>
					<input type="radio" name="destination" value="logos" /> logos<br/>
					<input type="radio" name="destination" value="people" checked="checked" /> people<br/>
					<input type="radio" name="destination" value="projects" /> projects<br/>
					<input type="radio" name="destination" value="science" /> science<br/>
					<input type="radio" name="destination" value="other" /> other<br/>
				</td>
			</tr>
			<tr>
				<td bgcolor="#C8D8F8"  valign="middle" colspan="1" align="right">
					<b>Select Processing Mode</b><br/>
				</td>
				<td colspan="2" bgcolor="#C8D8F8" align="left">
					<input type="radio" name="mode" value="upload"/> upload image
					<input type="radio" name="mode" value="replace" checked="checked"/> upload and replace any existing image or URL
				</td>
			</tr>
			<tr>
				<td colspan="3" bgcolor="#C8D8F8" align="center">
					<p>
					<input type="submit" name="submitMode" class="yellowbutton" value="Upload Selected Image"/>
					<input type="reset" name="reset" class="plainbutton" value="Reset" onclick="document.refreshForm.submit();"/>
					</p>
				</td>
			</tr>
	</table>
	</form>
	</td>
</tr>
<tr><td colspan="7">
	</td>
</tr>
</table>
<form action="uploadimages.jsp" name="refreshForm" >
	<input type="hidden" name="entityUri" value="${individual.URI}" />
</form>
