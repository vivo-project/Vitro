<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

    <tr class="editformcell" id="entityNameTr">
        <td valign="bottom" id="entityNameTd" colspan="2">
			<b>Individual Name *</b><br/>
            <input style="width:80%;" type="text" name="Name" value="<form:value name="Name"/>" />
           <c:set var="NameError"><form:error name="Name"/></c:set>
            <c:if test="${!empty NameError}">
                <span class="notice"><c:out value="${NameError}"/></span>
            </c:if>
        </td>
    </tr>

    <!-- begin datatype properties section -->
    
    <tr class="editformcell" style="border-collapse:collapse;">
        <td colspan="2">
            <ul style="margin-left:0;padding-left:0;list-style-type:none">
	        <form:dynamicFields type="dataprops" usePage="entity_retry_dataprops.jsp"/>
            </ul>
	</td>
    </tr>

    <!-- end datatype properties section -->    



