<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<%@page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>

<%

   VitroRequest vreq = new VitroRequest(request);
   String vclassName = vreq.getParameter("VClassName");

%>


<tr>
  <td>
    <strong>Move instances of <%=vclassName%> to:</strong>
  </td>
</tr>

<tr class="editformcell">
  <td>
	<strong>Subject class</strong><br/>
	<select name="NewVClassURI">
		<form:option name="NewVClassURI"/>
	</select>
  </td>
</tr>
