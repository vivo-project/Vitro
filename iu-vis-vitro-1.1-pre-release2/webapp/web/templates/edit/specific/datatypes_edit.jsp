<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%
final int DEFAULT_PORTAL_ID=1; //Vivo
String portalIdStr=(portalIdStr=(String)request.getAttribute("home"))==null ?
    ((portalIdStr=request.getParameter("home"))==null?String.valueOf(DEFAULT_PORTAL_ID):portalIdStr):portalIdStr;
int incomingPortalId=Integer.parseInt(portalIdStr);
%>

<div class="editingForm">

<jsp:include page="/templates/edit/fetch/vertical.jsp"/>

<hr/>
<p/>
<div align=center>
<table class="form-background" border="0" cellpadding="2" cellspacing="2">
<tr valign="bottom" align="center">
	<td>
		<form action="fetch" method="post">
			<input type="hidden" name="home" value="<%=portalIdStr%>" />
			<input type="submit" class="form-button" value="See All Datatypes"/>
			<input type="hidden" name="queryspec" value="private_datatypes"/>
			<input type="hidden" name="header" value="titleonly"/>
		</form>
	</td>
	<td valign="bottom" align="center">
		<form action="datatype_retry" method="get">
			<input type="hidden" name="home" value="<%=portalIdStr%>" />
			<input type="hidden" name="id" value="<%=request.getAttribute("firstvalue")%>"/>
			<input type="submit" class="form-button" value="Edit This Datatype"/>
		</form>
	</td>
	<td valign="bottom">
		<form action="datatype_retry" method="get">
			<input type="hidden" name="home" value="<%=portalIdStr%>" />
			<input type="submit" class="form-button" value="Add New Datatype"/>
		</form>
	</td>
</tr>
</table>
</div>
</div>
