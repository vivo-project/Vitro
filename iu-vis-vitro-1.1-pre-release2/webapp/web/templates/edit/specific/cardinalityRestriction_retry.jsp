<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>

wtffff
${restrictionType}
${requestScope.restrictionType}

<tr class="editformcell">
    <td>
        <c:choose>
        	<c:when test="${restrictionType eq 'minCardinality'}">
            	<strong>minimum cardinality</strong>
                <input type="hidden" name="cardinalityType" value="minCardinality"/>
        	</c:when>
        	<c:otherwise>
        	    <c:choose>
        	        <c:when test="${restrictionType eq 'maxCardinality'}">
        	            <strong>maximum cardinality</strong>
                        <input type="hidden" name="cardinalityType" value="maxCardinality"/>
        	        </c:when>
        	        <c:otherwise>
        	             <c:if test="${restrictionType eq 'cardinality' }">
        	                 <strong>exact cardinality</strong>
                             <input type="hidden" name="cardinalityType" value="cardinality"/>
        	             </c:if>
        	        </c:otherwise>
        	    </c:choose>
            </c:otherwise>
        </c:choose>

        <input name="cardinality"/>

    </td>
  
</tr>
