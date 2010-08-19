<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>


<tr class="editformcell">
<td><c:out value='<%=request.getParameter("oldURI")%>'/></td>
</tr>

<tr class="editformcell">
<td>
<b>New URI</b><br/><span class="warning"><strong>${epo.attributeMap['errorMsg']}</strong></span>
<input type="text" size="95%" name="newURI" value='<%=request.getParameter("oldURI")%>'/>
</td>
</tr>

