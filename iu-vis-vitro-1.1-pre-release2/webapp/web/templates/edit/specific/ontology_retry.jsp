<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>Ontology name</b><br/>
				<input type="text" name="Name" value="<form:value name="Name"/>" size="40" maxlength="120" />
				<font size="2" color="red"><form:error name="Name"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
            <b>Namespace URI</b><br/>
             <c:choose>
               <c:when test="${_action eq 'update'}">
                    <i>Change via the "change URI" button on previous screen</i><br/>
                    <input disabled="disabled" type="text" name="URI" value="<form:value name="URI"/>" size="50" maxlength="240" />
                </c:when>
                <c:otherwise>
                    <input type="text" name="URI" value="<form:value name="URI"/>" size="50" maxlength="240" />
                </c:otherwise>
              </c:choose>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>Namespace prefix</b><br/>
				<input type="text" name="Prefix" value="<form:value name="Prefix"/>" size="8" maxlength="25" />
				<font size="2" color="red"><form:error name="Prefix"/></font>
		</td>
	</tr>

