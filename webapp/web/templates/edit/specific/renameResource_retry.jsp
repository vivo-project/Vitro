<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<tr class="editformcell">
<td><c:out value='<%=request.getParameter("oldURI")%>'/></td>
</tr>

<tr class="editformcell">
    <td>
        <br/>
        <b>New URI</b>&nbsp;<span class="note"> (must begin with http:// or https://)</span>
        <br/>
        <span class="warning"><strong>${epo.attributeMap['errorMsg']}</strong></span>
        <input type="text" size="95%" name="newURI" value='<%=request.getParameter("oldURI")%>'/>
    </td>
</tr>
<script  type="text/javascript">
    $('form#editForm').submit(function() {
        var str = $('input[name=newURI]').val();
        if ( str.indexOf('http://') >= 0 || str.indexOf('https://') >= 0 ) {
            return true;
        }
        else {
            alert('The New URI must begin with either http:// \n\n or https://');
            $('input[name=newURI]').focus();
            return false;
        }
    });
</script>
