<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>

   <input type="hidden" value="equivalentClass" name="opMode"/>
   <input type="hidden" value="add" name="operation"/>

    <tr class="editformcell">
        <td valign="top">
			<select name="SuperclassURI">
				<form:option name="SuperclassURI"/>
			</select>
			<span class="warning"><form:error name="SuperclassURI"/></span>
        </td>
    </tr>
    <tr class="editformcell">
        <td><p><strong>equivalent to</strong></p></td>
    </tr>
    <tr class="editformcell">
        <td valign="top">
            <select name="SubclassURI" >
            	<form:option name="SubclassURI"/>
            </select>
            <span class="warning"><form:error name="SubclassURI"/></span>
        </td>
    </tr>
