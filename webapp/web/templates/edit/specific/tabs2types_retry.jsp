<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %> 
    <tr class="editformcell">
        <td valign="top">
            <b>Tab</b><br/>
                <select name="TabId" >
			<form:option name="TabId"/>
                </select>
                <font size="2" color="red"><form:error name="TabId"/></font>
        </td>
    </tr>
    <tr class="editformcell">
        <td valign="top">
            <b>Vclass</b><br/>
                <select name="VClassURI" >
			<form:option name="VClassId"/>
                </select>
                <font size="2" color="red"><form:error name="VClassId"/></font>
        </td>
    </tr>
