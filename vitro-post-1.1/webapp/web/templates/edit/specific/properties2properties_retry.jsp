<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

    <tr class="editformcell">
        <td valign="top">
            <c:choose>
                <c:when test="${opMode eq 'equivalentProperty'}">
                    <b>Property<sup>*</sup></b><br/>
                </c:when>
                <c:otherwise>
                    <b>Superproperty<sup>*</sup></b><br/>
                </c:otherwise>
            </c:choose>
            <select name="SuperpropertyURI">
                <form:option name="SuperpropertyURI"/>
            </select>
            <span class="warning"><form:error name="SuperpropertyURI"/></span>
        </td>
    </tr>
    <tr class="editformcell">
        <td valign="top">
            <c:choose>
                <c:when test="${opMode eq 'equivalentProperty'}">
                    <b>Equivalent Property<sup>*</sup></b><br/>
                </c:when>
                <c:otherwise>
                    <b>Subproperty<sup>*</sup></b><br/>
                </c:otherwise>
            </c:choose>
            <select name="SubpropertyURI" >
                <form:option name="SubpropertyURI"/>
            </select>
            <span class="warning"><form:error name="SubpropertyURI"/></span>
        </td>
    </tr>
    
    <input type="hidden" name="operation" value="${operation}" />
    <input type="hidden" name="opMode" value="${opMode}" />