<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tr>
  <td>
    <strong>Move statements matching the following:</strong>
  </td>
</tr>

<tr class="editformcell">
  <td>
	<strong>Subject class</strong><br/>
	<select name="SubjectClassURI">
		<form:option name="SubjectClassURI"/>
	</select>
  </td>
</tr>
<tr>
  <td>
	${epo.attributeMap['propertyURI']}
  </td>
</tr>
<c:if test="${epo.attributeMap['propertyType'] == 'ObjectProperty' }">
	<tr class="editformcell">
	  <td>
		<strong>Object class</strong><br/>
		<select name="ObjectClassURI">
			<form:option name="ObjectClassURI"/>
		</select>
	  </td>
	</tr>
</c:if>
<tr class="editformcell">
  <td>
	<strong>to use property:</strong><br/>
	<select name="NewPropertyURI">
		<form:option name="NewPropertyURI"/>
	</select>
  </td>
</tr>
