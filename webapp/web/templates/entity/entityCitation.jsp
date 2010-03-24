<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary" %>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<c:if test="${showEdits || !empty entity.citation}">
    <div id="dprop-vitro-citation" class="propsItem ${editingClass}">   
        <c:if test="${showEdits}">                           
            <h3 class="propertyName">citation</h3>
            <edLnk:editLinks item="<%= VitroVocabulary.CITATION %>" icons="false"/>
        </c:if>
        <c:if test="${!empty entity.citation}">
            <div class="datatypeProperties">
                <div class="datatypePropertyValue">
                    <div class="statementWrap">
                        ${entity.citation}
                        <c:if test="${showEdits}">
                            <c:set var="editLinks"><edLnk:editLinks item="<%= VitroVocabulary.CITATION %>" data="${entity.citation}" icons="false"/></c:set>
                            <c:if test="${!empty editLinks}"><span class="editLinks">${editLinks}</span></c:if>                                                                     
                        </c:if> 
                    </div>
                </div>
            </div>
        </c:if>     
    </div>
</c:if>