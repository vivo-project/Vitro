<?xml version="1.0" encoding="UTF-8"?>

<!-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
		  xmlns:c="http://java.sun.com/jsp/jstl/core"
		  xmlns:form="http://vitro.mannlib.cornell.edu/edit/tags"
		  version="2.0">
	<jsp:directive.page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers"/>


	<div class="editingForm">
		<style>
			.editingForm .form-button {
				white-space: normal;
			}
		</style>

		<jsp:include page="/templates/edit/fetch/vertical.jsp"/>

		<div align="center">
			<table class="form-background" border="0" cellpadding="2" cellspacing="2">
				<tr valign="bottom" align="center">
					<td width="46%">
						<form action="listOntologies" method="get">
							<input type="submit" class="form-button" value="All Ontologies"/>
						</form>
						<form action="showClassHierarchy" method="get">
							<input type="submit" class="form-button" value="Hierarchy of Classes Defined in This Namespace" />
							<input type="hidden" name="ontologyUri" value="${Ontology.URI}" />
						</form>
						<form action="listPropertyWebapps" method="get">
							<input type="submit" class="form-button" value="Object Properties Defined in This Namespace" />
							<input type="hidden" name="ontologyUri" value="${Ontology.URI}" />
						</form>
						<form action="listDatatypeProperties" method="get">
							<input type="submit" class="form-button" value="Datatype Properties Defined in This Namespace" />
							<input type="hidden" name="ontologyUri" value="${Ontology.URI}" />
						</form>
					</td>
					<td valign="bottom" align="center" width="27%">
						<form action="editForm" method="get">
							<input type="submit" class="form-button" value="Edit ${Ontology.name}"/>
							<input name="uri" type = "hidden" value="${Ontology.URI}" />
							<input type="hidden" name="controller" value="Ontology"/>
						</form>
					</td>
					<td valign="bottom" width="27%">
						<form action="editForm" method="get">
							<input type="submit" class="form-button" value="Add New Ontology"/>
							<input type="hidden" name="controller" value="Ontology"/>
						</form>
						<form action="editForm" method="get">
							<input type="submit" class="form-button" value="Change URI"/>
							<input type="hidden" name="oldURI" value="${realURI}"/>
							<input type="hidden" name="mode" value="renameResource"/>
							<input type="hidden" name="controller" value="Refactor"/>
						</form>

					</td>
				</tr>
				<tr><td colspan="3"><hr></hr></td></tr>
			</table>
			<table class="form-background" border="0" cellpadding="2" cellspacing="2">
				<tr valign="bottom" align="center">
					<td>
						<div style="margin-left:-0.5em;margin-top:0.5em;padding:0.5em;border-style:solid;border-width:1px;float:right">
							<form action="${exportURL}" method="get">
								<input type="hidden" name="subgraph" value="tbox"/>
								<input type="hidden" name="assertedOrInferred" value="asserted"/>
								<input type="hidden" name="ontologyURI" value="${Ontology.URI}"/>
								<input type="submit" class="form-button" name="submit" value="Export Ontology Entities Defined in This Namespace to RDF"/>
								<div style="padding:0;margin-top:0.3em;white-space:nowrap;">
									<input type="radio" name="format" value="RDF/XML-ABBREV" checked="checked" selected="selected"/> RDF/XML abbreviated
									<input type="radio" name="format" value="RDF/XML"/> RDF/XML
									<input type="radio" name="format" value="N3"/> N3
									<input type="radio" name="format" value="N-TRIPLE"/> N-Triples
									<input type="radio" name="format" value="TURTLE"/> Turtle
								</div>
							</form>
						</div>


					</td>
				</tr>
			</table>
		</div>
	</div>

</jsp:root>
