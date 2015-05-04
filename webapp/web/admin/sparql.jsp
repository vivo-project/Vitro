<!-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<%@page import="com.hp.hpl.jena.vocabulary.OWL"%>
<%@page import="com.hp.hpl.jena.rdf.model.ModelMaker"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission" %>
<% request.setAttribute("requestedActions", SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION); %>
<vitro:confirmAuthorization />

<script type="text/javascript" src="../js/sparql/prototype.js">
</script>
<script type="text/javascript" src="../js/sparql/sparql.js">
</script>
<style type="text/css">
td {
	vertical-align: top;
	border: 1px solid #b2b2b2;
}
</style>
<BODY onload="init()">
	<div id="content" class="sparqlform"
		style="width: 900px; margin: 0 auto;">
		<h2>
			SPARQL Query Builder
		</h2>
		<table id="builder" width="100%">
			<tr>
				<td width="33%">
					Subject
				</td>
				<td width="33%">
					Predicate
				</td>
				<td width="34%">
					Object
				</td>
			</tr>
			<tr id="clazz(0)">
				<td id="subject(0)" width="33%">
					<select id="subject(0,0)">
						<option value="<%=OWL.Thing.getURI()%>">
							Thing
						</option>
					</select>
				</td>
				<td id="predicate(0)">

				</td>
				<td id="object(0)" width="34%">

				</td>
			</tr>
		</table>
		<div>
			<input type="button" class="submit" value="Generate Query" onclick="genQuery();" />
		</div>
		<div id="sparqlquery" style="visibility: hidden;">
			<form action="sparqlquery">
				<div>
					<textarea id="query" name="query" rows="20" cols="111" class="maxWidth">
							
						</textarea>
				</div>
				<p>
					<h3>
						Format for SELECT query results:
					</h3>
					<input id='RS_XML_BUTTON' type='radio' name='resultFormat'
						value='application/sparql-results+xml'>
					<label for='RS_XML_BUTTON'>
						RS_XML
					</label>
					<input id='RS_TEXT_BUTTON' type='radio' name='resultFormat'
						value='text/plain' checked='checked'>
					<label for='RS_TEXT_BUTTON'>
						RS_TEXT
					</label>
					<input id='RS_CSV_BUTTON' type='radio' name='resultFormat'
						value='text/csv'>
					<label for='RS_CSV_BUTTON'>
						CSV
					</label>
					<input id='RS_JSON_BUTTON' type='radio' name='resultFormat'
						value='application/sparql-results+json'>
					<label for='RS_JSON_BUTTON'>
						RS_JSON
					</label>
				</p>

				<p>
					<h3>
						Format for CONSTRUCT and DESCRIBE query results:
					</h3>
					<input id='RR_RDFXML_BUTTON' type='radio' name='rdfResultFormat'
						value='application/rdf+xml'>
					<label for='RR_RDFXML_BUTTON'>
						RDF/XML
					</label>
					<input id='RR_N3_BUTTON' type='radio' name='rdfResultFormat'
						value='text/n3'>
					<label for='RR_N3_BUTTON'>
						N3
					</label>
					<input id='RR_NTRIPLE_BUTTON' type='radio' name='rdfResultFormat'
						value='text/plain'>
					<label for='RR_NTRIPLE_BUTTON'>
						N-Triples
					</label>
					<input id='RR_TURTLE_BUTTON' type='radio' name='rdfResultFormat'
						value='text/turtle'>
					<label for='RR_TURTLE_BUTTON'>
						Turtle
					</label>
				</p>

				<div>

					<ul class="clean">
						<%
							try {
								ModelMaker maker = ModelAccess.on(application).getModelMaker(WhichService.CONFIGURATION);
								for (Iterator it = maker.listModels(); it.hasNext();) {
									String modelName = (String) it.next();
						%>
						<li>
							<input type="checkbox" name="sourceModelName"
								value="<%=modelName%>" /><%=modelName%></li>
						<%
							    }
							} catch (Exception ex) {
						%><li>
							could not find named models in ModelMaker
						</li>
						<%
							}
						%>
					</ul>

				</div>
				<input type="submit" value="Run Query" class="submit">
			</form>
		</div>
	</div>
</BODY>
</HTML>
