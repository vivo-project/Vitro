<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vedit.beans.LoginFormBean" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.FakeSelfEditingIdentifierFactory" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<%
    if(session == null || !LoginFormBean.loggedIn(request, LoginFormBean.CURATOR)) {
        %><c:redirect url="<%= Controllers.LOGIN %>" /><%
    }

    if(  request.getParameter("force") != null ){        
        VitroRequestPrep.forceToSelfEditing(request);
        String netid = request.getParameter("netid");
        FakeSelfEditingIdentifierFactory.clearFakeIdInSession( session );
        FakeSelfEditingIdentifierFactory.putFakeIdInSession( netid , session );%>
        <c:redirect url="/entity">
            <c:param name="netid" value="<%=netid%>" />
        </c:redirect>
<%    }
    if( request.getParameter("stopfaking") != null){
        VitroRequestPrep.forceOutOfSelfEditing(request);
        FakeSelfEditingIdentifierFactory.clearFakeIdInSession( session );
    }    
    String netid = (String)session.getAttribute(FakeSelfEditingIdentifierFactory.FAKE_SELF_EDIT_NETID);
    String msg = "You have not configured a netid for testing self-editing. ";
    if( netid != null ) {
        msg = "You have are testing self-editing as '" + netid + "'.";%>
        <c:redirect url="/entity">
        	<c:param name="netid" value="<%=netid%>"/>
       	</c:redirect>
<%  } else {
        netid = "";
    }
        
%>


<html>
<title>Test Self-Edit</title>
<body>
<h2>Configure Self-Edit Testing</h2>
<p><%=msg %></p>
<form action="<c:url value="fakeselfedit.jsp"/>" >
    <input type="text" name="netid" value="<%= netid %>"/>
    <input type="hidden" name="force" value="1"/>
    <input type="submit" value="use a netid for testing"/>
</form>

<p/>

<form action="<c:url value="fakeselfedit.jsp"/>" >
    <input type="hidden" name="stopfaking" value="1"/>
    <input type="submit" value="stop usng netid for testing"/>
</form>

</body>
</html>
