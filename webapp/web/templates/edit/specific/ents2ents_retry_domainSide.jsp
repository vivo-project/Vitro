<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>

    <tr class="editformcell">
        <td valign="top">
            <b>Subject Individual<sup>*</sup></b><br/>
			<select disabled="disabled" name="SubjectEntURI">
				<form:option name="SubjectEntURI"/>
			</select>
			<span class="warning"><form:error name="SubjectEntURI"/></span>
        </td>
    </tr>
    <tr class="editformcell">
        <td valign="top" colspan="3">
            <b><form:value name="Prop"/><sup>*</sup></b><br/>
            <b>Object Individual<sup>*</sup></b><br/>
			         <select name="ObjectEntURI">
                                   <form:option name="ObjectEntURI"/>
                                 </select>
			            <span class="warning"><form:error name="ObjectEntURI"/></span>
        </td>
    </tr>
