<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>

    <tr class="editformcell">
        <td valign="top">
            <b>Individual<sup>*</sup></b><br/>
			<select name="_" disabled="disabled">
				<form:option name="IndividualURI"/>
			</select>
        </td>
    </tr>
    <tr class="editformcell">
        <td valign="top" colspan="3">
            <b><form:value name="Dataprop"/><sup>*</sup></b><br/>
            <textarea name="Data" rows="6" cols="64"><form:value name="Data"/></textarea>
            <span class="warning"><form:error name="Data"/></span>
        </td>
    </tr>
