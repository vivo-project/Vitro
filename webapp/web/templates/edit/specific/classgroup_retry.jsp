<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<tr class="editformcell">
		<td valign="top">
			<b>Class group name*</b> (max 120 characters)<br />
				<input type="text" name="PublicName" value="<form:value name="PublicName"/>" size="50" maxlength="120" />
				<c:set var="PublicNameError"><form:error name="PublicName"/></c:set>
                <c:if test="${!empty PublicNameError}">
                    <span class="notice"><c:out value="${PublicNameError}"/></span>
                </c:if>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="top">
			<b>Display rank </b> (lower number displays first)<br />
				<input type="text" name="DisplayRank" value="<form:value name="DisplayRank"/>" size="3" maxlength="11" />
				<c:set var="DisplayRankError"><form:error name="DisplayRank"/></c:set>
                <c:if test="${!empty DisplayRankError}">
                    <span class="notice"><c:out value="${DisplayRankError}"/></span>
                </c:if>
		</td>
	</tr>
