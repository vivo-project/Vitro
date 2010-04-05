<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary" %>
<%@ taglib uri="http://vitro.mannlib.cornell.edu/vitro/tags/PropertyEditLink" prefix="edLnk" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<c:if test="${showEdits || !empty entity.citation}">
	
		<c:if test="${not empty entity.citation }">                          	 
			<c:set var="editLinksForExisting"><edLnk:editLinks item="<%= VitroVocabulary.CITATION %>" data="${entity.citation}" icons="false"/></c:set>
		</c:if>
		<c:set var="editLinksForNew"><edLnk:editLinks item="<%= VitroVocabulary.CITATION %>" icons="false"/></c:set>            	              	 
		<c:set var="mayEditCitation" value="${showEdits and (( empty entity.citation and !empty editLinksForNew) or ( ! empty entity.citation and !empty editLinksForExisting)) }"/>
		
		<c:if test="${ !empty entity.citation || mayEditCitation }">
			<div id="dprop-vitro-citation" class="propsItem ${editingClass}">		                           
			<h3 class="propertyName">citation</h3> ${editLinksForNew}
		</c:if>                 
		
		<c:if test="${!empty entity.citation}">
			<div class="datatypeProperties">
				<div class="datatypePropertyValue">
					<div class="statementWrap">
						<p:process>${entity.citation}</p:process>                        
						<c:if test="${showEdits && !empty editLinksForExisting}">
							<span class="editLinks">${editLinksForExisting}</span>
						</c:if> 
					</div>
				</div>
			</div>
		</c:if>     
		<c:if test="${ !empty entity.citation || mayEditCitation }">
			</div>
		</c:if>
</c:if>