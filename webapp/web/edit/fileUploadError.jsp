<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>


<jsp:include page="/edit/formPrefix.jsp" >
    <jsp:param name="useTinyMCE" value="false"/>
    <jsp:param name="useAutoComplete" value="false"/>
</jsp:include>

<h2>There was a problem uploading your file.</h2>
<div>
${errors}
</div>

<jsp:include page="/edit/formSuffix.jsp" />
