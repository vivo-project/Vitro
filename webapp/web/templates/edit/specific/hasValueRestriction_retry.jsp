<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tr class="editformcell">
    <td>

        <b>has value</b><br/>
        <c:choose>    
            <c:when test="${requestScope.propertyType eq 'object'}">    
                <p><input name="ValueIndividual"/> <em>enter complete URI</em> </p> <!-- TODO: make nice way of dealing with picklists -->
            </c:when>
            <c:otherwise>
                <p>
                    <input name="ValueLexicalForm"/>
                    datatype <select name="ValueDatatype"> 
                        <form:option name="ValueDatatype"/>
                    </select> 
                </p>
            </c:otherwise>
        </c:choose>
    </td>
</tr>
