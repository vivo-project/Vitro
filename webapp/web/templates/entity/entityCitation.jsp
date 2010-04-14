<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
	
<c:if test="${!empty entity.citation}">
	<div class="datatypeProperties">
		<div class="datatypePropertyValue">
			<div class="statementWrap">
				<p:process><div class="citation">${entity.citation}</div></p:process>                        
			</div>
		</div>
	</div>
</c:if>     
