<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils" %>
<html>

<head>
<title>Authroization Test</title>
<link rel="stylesheet" type="text/css" href="css/edit.css">
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>

<body>

<table width="90%" cellpadding="1" cellspacing="1" border="0" align="center">

<tr><td colspan="4" height="1"><img src="site_icons/transparent.gif" width="100%" height="1" border="0"></td></tr>

    <%

    out.println("here");
java.util.Map params = request.getParameterMap();
java.util.Iterator keys = params.keySet().iterator();

%> <tr><td><b>params</b></td></tr> <%
while (keys.hasNext()){
    String name = (String) keys.next();
    String val = (String) params.get(name);
    out.println("<tr><td>"+ name + "</td><td>"+ val +"</td></tr>");
}
%> <tr><td><b>headers</b></td></tr> <%
    java.util.Enumeration hnames = request.getHeaderNames();
while( hnames.hasMoreElements() ){
    String name = (String) hnames.nextElement();
    String val  = request.getHeader(name);
    out.println("<tr><td>"+ name + "</td><td>"+ val +"</td></tr>");
}%>
</table>

<%= MiscWebUtils.getReqInfo(request) %>

</body>
</html>
