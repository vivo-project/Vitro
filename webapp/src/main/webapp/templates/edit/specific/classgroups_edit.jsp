<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<div class="editingForm"/>

<jsp:include page="/templates/edit/fetch/vertical.jsp"/>

<hr/>
<p/>
<div align="center">
<table class="form-background" border="0" cellpadding="2" cellspacing="2">
<tr valign="bottom" align="center">
	<td>
		<form action="fetch" method="get">
			<input type="submit" class="form-button" value="display this class group's record"/>
			<input type="hidden" name="queryspec" value="private_classgroup"/>
			<input type="hidden" name="header" value="titleonly"/>
			<input type="hidden" name="linkwhere" value="classgroups.id=<%=request.getAttribute("firstvalue")%>"/>
		</form>
		<form action="fetch" method="get">
			<input type="submit" class="form-button" value="see all class groups"/>
			<input type="hidden" name="queryspec" value="private_classgroups"/>
			<input type="hidden" name="header" value="titleonly"/>
		</form>
	</td>
	<td valign="bottom" align="center">
		<form action="editForm" method="get">
			<input name="id" type = "hidden" value="<%=request.getAttribute("firstvalue")%>" />
			<input type="submit" class="form-button" value="edit class group <%=request.getAttribute("firstvalue")%>"/>
			<input type="hidden" name="controller" value="Classgroup"/>
		</form>
	</td>
	<td valign="bottom">
		<form action="editForm" method="get">
			<input type="hidden" name="controller" value="Classgroup"/>
			<input type="submit" class="form-button" value="add new class group"/>
		</form>
		<form action="editForm" method="get">
			<input type="hidden" name="controller" value="Vclass"/>
			<input type="hidden" name="GroupId" value="<%=request.getAttribute("firstvalue")%>"/>
			<input type="submit" class="form-button" value="add new class to this group"/>
		</form>
	</td>
</tr>
<tr><td colspan="3"><hr/></td></tr>
</table>
</div>
</div>
