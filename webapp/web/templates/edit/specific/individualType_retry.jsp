<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>

   <input type="hidden" value="add" name="operation"/>
   <input type="hidden" value="${individual.URI}" name="individualURI"/>

    <tr class="editformcell">
        <td valign="top">
			${individual.name}
        </td>
    </tr>
    <tr class="editformcell">
        <td><p><strong>has type</strong></p></td>
    </tr>
    <tr class="editformcell">
        <td valign="top">
            <select name="TypeURI" >
            	<form:option name="types"/>
            </select>
            <span class="warning"><form:error name="TypeURI"/></span>
        </td>
    </tr>
