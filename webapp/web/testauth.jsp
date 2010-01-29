<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vedit.beans.LoginFormBean" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle" %>
<%@ page
import="edu.cornell.mannlib.vitro.webapp.auth.identifier.NetIdIdentifierFactory"
%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="java.util.Enumeration" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<%
    if(  request.getParameter("force") != null ){
        VitroRequestPrep.forceToSelfEditing(request);
    }
    if( request.getParameter("stopfaking") != null){
        VitroRequestPrep.forceOutOfSelfEditing(request);
    }
%>


<html>

<body>
<h1>auth info</h1>

<form action="<c:url value="testauth.jsp"/>" >
    <input type="hidden" name="force" value="1"/>
    <input type="submit" value="use fake netid for testing"/>
</form>

<p/>

<form action="<c:url value="testauth.jsp"/>" >
    <input type="hidden" name="stopfaking" value="1"/>
    <input type="submit" value="stop usng netid for testing"/>
</form>


<%
    out.println("<table>");

    Enumeration fj = request.getHeaderNames();
    while( fj.hasMoreElements()){
        String name = (String)fj.nextElement();
        out.print("\n<tr><td>" + name + "</td>");
        out.print("<td>" + request.getHeader(name) + "</td></tr>");
    }

    out.println("</table>");
    %>
</body>
</html>
