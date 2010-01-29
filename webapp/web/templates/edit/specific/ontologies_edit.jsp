<?xml version="1.0" encoding="UTF-8"?>

<!-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" 
          xmlns:c="http://java.sun.com/jstl/core" 
          xmlns:form="http://vitro.mannlib.cornell.edu/edit/tags"
          version="2.0">


<div class="editingForm">

<jsp:include page="/templates/edit/fetch/vertical.jsp"/> 

<div align="center">
<table class="form-background" border="0" cellpadding="2" cellspacing="2">
<tr valign="bottom" align="center">
	<td>
		<form action="listOntologies" method="get">
			<input type="hidden" name="home" value="${portalBean.portalId}" />
			<input type="submit" class="form-button" value="Show All Ontologies"/>
		</form>
		<form action="showClassHierarchy" method="get">
                        <input type="hidden" name="home" value="${portalBean.portalId}" />
                        <input type="submit" class="form-button" value="Show This Ontology's Class Hierarchy" />
                        <input type="hidden" name="ontologyUri" value="${Ontology.URI}" />
                </form>
		<form action="listPropertyWebapps" method="get">
                        <input type="hidden" name="home" value="${portalBean.portalId}" />
                        <input type="submit" class="form-button" value="Show This Ontology's Object Properties" />
                        <input type="hidden" name="ontologyUri" value="${Ontology.URI}" />
                </form>
		<form action="listDatatypeProperties" method="get">
                        <input type="hidden" name="home" value="${portalBean.portalId}" />
                        <input type="submit" class="form-button" value="Show This Ontology's Datatype Properties" />
                        <input type="hidden" name="ontologyUri" value="${Ontology.URI}" />
                </form>
	</td>
	<td valign="bottom" align="center">
		<form action="editForm" method="get">
			<input type="hidden" name="home" value="${portalBean.portalId}" />
			<input type="submit" class="form-button" value="Edit ${Ontology.name}"/>
			<input name="uri" type = "hidden" value="${Ontology.URI}" />
			<input type="hidden" name="controller" value="Ontology"/>
		</form>
	</td>
	<td valign="bottom">
		<form action="editForm" method="get">
			<input type="hidden" name="home" value="${portalBean.portalId}" />
			<input type="submit" class="form-button" value="Add New Ontology"/>
			<input type="hidden" name="controller" value="Ontology"/>
		</form>
        <form action="editForm" method="get">
                <input type="submit" class="form-button" value="Change URI"/>
                <input type="hidden" name="home" value="${portalBean.portalId}" />
                <input type="hidden" name="oldURI" value="${realURI}"/>
                <input type="hidden" name="mode" value="renameResource"/>
                <input type="hidden" name="controller" value="Refactor"/>
        </form>
	</td>
</tr>
</table>
</div>
</div>

</jsp:root>
