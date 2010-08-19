<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>

    <tr class="editformcell">
        <td valign="top">
            <b>Superclass<sup>*</sup></b><br/>
			<select name="SuperclassURI">
				<form:option name="SuperclassURI"/>
			</select>
			<span class="warning"><form:error name="SuperclassURI"/></span>
        </td>
    </tr>
    <tr class="editformcell">
        <td valign="top">
            <b>Subclass<sup>*</sup></b><br/>
            <select name="SubclassURI" >
            	<form:option name="SubclassURI"/>
            </select>
            <span class="warning"><form:error name="SubclassURI"/></span>
        </td>
    </tr>
