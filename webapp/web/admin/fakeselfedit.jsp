<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<%-- doesn't use <vitro:requiresAuthorizationFor> becuase the controller does complex authorization. --%>

<div id="content">

<h2>Configure Self-Edit Testing</h2>
<p>${msg}</p>
<form action="<c:url value="/admin/fakeselfedit"/>" >
    <input type="text" name="netid" value="${netid}"/>
    <input type="hidden" name="force" value="1"/>
    &nbsp;<input type="submit" value="use this netid for testing"/>
</form>

<br />
<form action="<c:url value="/admin/fakeselfedit"/>" >
    <input type="hidden" name="stopfaking" value="1"/>
    <input type="submit" value="stop using netid for testing"/>
</form>

</div> <!-- content -->
