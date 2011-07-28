<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages" %>
<% request.setAttribute("requestedActions", new UseAdvancedDataToolsPages()); %>
<vitro:confirmAuthorization />

<h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Rename Resource</h2>

<%String result = (String) request.getAttribute("result");%>
<p><b><%=result%></b></p>