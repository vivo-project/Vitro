<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">	
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Portal" %>

<%
VitroRequest vreq = new VitroRequest(request);
String errorString=request.getParameter("ERR");
if (errorString == null || errorString.equals("")) {
    errorString = (String)request.getAttribute("ERR");
}
%>

<table>
<tr>
    <td align="center" colspan="3">
		<img src="site_icons/bomb.gif" alt="failed email"/><br/>
<%      if ( errorString != null && !errorString.equals("")) {%>
			<p class="normal">We report the following error in processing your request:<br/>
		    <b><%=errorString%></b>
			</p>
<%		} %>
		<p class="normal">Return to the <a href="index.jsp">home page</a>.</p>
	</td>
</tr>
</table>
