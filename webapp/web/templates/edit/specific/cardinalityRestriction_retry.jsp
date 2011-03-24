<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>

<tr class="editformcell">
    <td>
        <c:choose>
        	<c:when test="${restrictionType eq 'minCardinality'}">
            	minimum cardinality
        	</c:when>
        	<c:otherwise>
        	    <c:choose>
        	        <c:when test="${restrictionType eq 'maxCardinality'}">
        	            maximum cardinality
        	        </c:when>
        	        <c:otherwise>
        	             <c:if test="${restrictionType eq 'cardinality' }">
        	                 exact cardinality
        	             </c:if>
        	        </c:otherwise>
        	    </c:choose>
        </c:choose>
        
        <select name="cardinalityType">
            <option value="minCardinality">minimum cardinality</option>
            <option value="maxCardinaltiy">maximum cardinatlity</option>
            <option value="cardinality">cardinality (exact)</option>
        </select>
    </td>
    <td>
        <input name="cardinality"/>
    </td>
</tr>
