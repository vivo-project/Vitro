<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<div class="editingForm">

<jsp:include page="/templates/edit/fetch/vertical.jsp"/>

<%@ page import="java.util.*,formbeans.ApplicationBean"%>
<%
/**
 *
 * @version 1.00
 * @author Jon Corson-Rikert
 *
 * UPDATES:
 * JCR 2005-11-06 : removed flag2 and flag3 values fields (text) -- using only numeric, and even their use has been shifted to tabs instead
 * JCR 2005-06-14 : added field23 for imageThumbWidth
 */
 
ApplicationBean appBean=ApplicationBean.getAppBean();
%>
<%@ taglib uri="/WEB-INF/tlds/database.tld" prefix="database"%>
<%
final int PRIMARY_TAB_TABTYPE=28;
final int SUBCOLLECTION_CATEGORY_TABTYPE=18;
final int DEFAULT_PORTAL_ID=1;
String portalIdStr=(portalIdStr=(String)request.getAttribute("home"))==null ?
	((portalIdStr=request.getParameter("home"))==null?String.valueOf(DEFAULT_PORTAL_ID):portalIdStr):portalIdStr;
String appName=null;
%>
<hr/>
<p/>
<div align="center">
<table class="form-background" border="0" cellpadding="2" cellspacing="2">
<tr valign="bottom" align="center">
	<td>
		<form action="fetch" method="post">
			<input type="submit" class="form-button" value="display this portal's record"/>
			<input type="hidden" name="home" value="<%=portalIdStr%>" />
			<input type="hidden" name="queryspec" value="private_portal"/>
			<input type="hidden" name="header" value="titleonly"/>
			<input type="hidden" name="linkwhere" value="portals.id=<%=request.getAttribute("firstvalue")%>"/>
		</form>
<%		if (appBean.isFlag1Active()) {%>
		<form action="fetch" method="post">
			<input type="submit" class="form-button" value="See All Portals"/>
			<input type="hidden" name="home" value="<%=portalIdStr%>" />
			<input type="hidden" name="queryspec" value="private_portals"/>
			<input type="hidden" name="header" value="titleonly"/>
		</form>
<%		}%>
	</td>
	<td valign="bottom" align="center">
		<form action="editForm" method="get">
			<input type="hidden" name="controller" value="Portal"/>
			<input name="id" type = "hidden" value="<%=request.getAttribute("firstvalue")%>" />
			<input type="submit" class="form-button" value="Edit Portal <%=request.getAttribute("firstvalue")%>"/>
		</form>
	</td>
<%	if (appBean.isFlag1Active()){%>
	<td valign="bottom">
		<form action="editForm" method="get">
			<input type="hidden" name="home" value="<%=portalIdStr%>" />
			<input type="hidden" name="controller" value="Portal"/>
			<input type="submit" class="form-button" value="Add New Portal"/>
		</form>
	</td>
<%	}%>
</tr>
<tr><td colspan="3"><hr/></td></tr>
</table>
</div>
</div>
