<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<div class="editingForm">

<jsp:include page="/templates/edit/fetch/vertical.jsp"/>

<hr/>
<p/>
<div align=center>
<table class="form-background" border="0" cellpadding="2" cellspacing="2">
<tr valign="bottom" align="center">
	<td>
		<form action="fetch" method="get">
			<input type="submit" class="form-button" value="See All Namespaces"/>
			<input type="hidden" name="queryspec" value="private_namespaces"/>
			<input type="hidden" name="header" value="titleonly"/>
		</form>
	</td>
	<td valign="bottom" align="center">
		<form action="editForm" method="get">
			<input type="submit" class="form-button" value="Edit Namespace <%=request.getAttribute("firstvalue")%>"/>
			<input name="id" type = "hidden" value="<%=request.getAttribute("firstvalue")%>" />
			<input type="hidden" name="controller" value="Namespace"/>
		</form>
	</td>
	<td valign="bottom">
		<form action="editForm" method="get">
			<input type="submit" class="form-button" value="Add New Namespace"/>
			<input type="hidden" name="controller" value="Namespace"/>
		</form>
	</td>
</tr>
</table>
</div>
</div>

