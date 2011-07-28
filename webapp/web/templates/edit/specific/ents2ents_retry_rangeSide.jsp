<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>

    <tr class="editformcell">
        <td valign="top">
            <b>Subject Individual<sup>*</sup></b><br/>
			<select name="RangeId">
				<form:option name="RangeId"/>
			</select>
			<span class="warning"><form:error name="RangeId"/></span>
        </td>
    </tr>
    <tr class="editformcell">
        <td valign="top" colspan="3">
            <b><form:value name="Prop"/><sup>*</sup></b><br/>
            <b>Subject Individual<sup>*</sup></b><br/>
			<select name="DomainId">
				<form:option name="DomainId"/>
			</select>            <span class="warning"><form:error name="DomainId"/></span>
        </td>
    </tr>
