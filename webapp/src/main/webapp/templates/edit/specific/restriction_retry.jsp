<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>

<tr class="editformcell">
<td>
<b>Condition type</b><br/>
<select name="conditionType">
    <option value="necessary">necessary</option>
    <option value="necessaryAndSufficient">necessary and sufficient</option>
</select>
</td>
</tr>

<tr class="editformcell">
<td>
<b>Property to restrict</b><br/>
<select name="onProperty">
    <form:option name="onProperty"/>
</select>
</td>
</tr>

<jsp:include page="${specificRestrictionForm}"/>
