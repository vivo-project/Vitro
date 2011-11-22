<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@page import="com.hp.hpl.jena.rdf.model.ModelMaker"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManagePortals" %>
<% request.setAttribute("requestedActions", new ManagePortals()); %>
<vitro:confirmAuthorization />

<body>
<div id="content" class="sparqlform">
<h2>SPARQL Query</h2>
<form action='sparqlquery' method="get">
<h3>Query:</h3>
<div>
<textarea name='query' rows ='30' cols='100' class="span-23 maxWidth">
PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>
PREFIX owl:   <http://www.w3.org/2002/07/owl#>
PREFIX swrl:  <http://www.w3.org/2003/11/swrl#>
PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#>
PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#><%List prefixes = (List)request.getAttribute("prefixList");
if(prefixes != null){
	Iterator prefixItr = prefixes.iterator();
	Integer count = 0;
	while (prefixItr.hasNext()){
		String prefixText = (String) prefixItr.next();
		if(prefixText.equals("(not yet specified)")){
			count++;
			prefixText = "p." + count.toString();		
		}
		String urlText = (String) prefixItr.next();%>
PREFIX <%=prefixText%>: <<%=urlText%>><%}}%>

#
# This example query gets 20 geographic locations
# and (if available) their labels
#
SELECT ?geoLocation ?label
WHERE
{
      ?geoLocation rdf:type vivo:GeographicLocation
      OPTIONAL { ?geoLocation rdfs:label ?label } 
}
LIMIT 20
</textarea>
</div>

<div>
	 <h3>Format for SELECT query results:</h3>
	 <input id='RS_XML_BUTTON' type='radio' name='resultFormat' value='RS_XML'> <label for='RS_XML_BUTTON'>RS_XML</label>
	 <input id='RS_TEXT_BUTTON' type='radio' name='resultFormat' value='RS_TEXT' checked='checked'> <label for='RS_TEXT_BUTTON'>RS_TEXT</label>
	 <input id='RS_CSV_BUTTON' type='radio' name='resultFormat' value='vitro:csv'> <label for='RS_CSV_BUTTON'>CSV</label>
	 <input id='RS_JSON_BUTTON' type='radio' name='resultFormat' value='RS_JSON'> <label for='RS_JSON_BUTTON'>RS_JSON</label>
	 <input id='RS_RDF_BUTTON' type='radio' name='resultFormat' value='RS_RDF'> <label for='RS_RDF_BUTTON'>RS_RDF</label>
</div>

<div>
	 <h3>Format for CONSTRUCT and DESCRIBE query results:</h3>
	 <input id='RR_RDFXML_BUTTON' type='radio' name='rdfResultFormat' value='RDF/XML'> <label for='RR_RDFXML_BUTTON'>RDF/XML</label>
	 <input id='RR_RDFXMLABBREV_BUTTON' type='radio' name='rdfResultFormat' value='RDF/XML-ABBREV' checked='checked'> <label for='RR_RDFXMLABBREV_BUTTON'>RDF/XML-ABBREV</label>
	 <input id='RR_N3_BUTTON' type='radio' name='rdfResultFormat' value='N3'> <label for='RR_N3_BUTTON'>N3</label>
	 <input id='RR_NTRIPLE_BUTTON' type='radio' name='rdfResultFormat' value='N-TRIPLE'> <label for='RR_NTRIPLE_BUTTON'>N-Triples</label>
	 <input id='RR_TURTLE_BUTTON' type='radio' name='rdfResultFormat' value='TTL'> <label for='RR_TURTLE_BUTTON'>Turtle</label>
</div>

<input class="submit" type="submit" value="Run Query" />
</form>
<%--
<h4>Notes</h4>
<p>CONSTRUCT and DESCRIBE queries always return RDF XML</p>
<p>The parameter 'resultFormat' must not be null or zero length</p>
<p>The parameter 'resultFormat' must be one of the following: <ul>
    <li>RS_XML</li>
    <li>RS_TEXT</li>
    <li>RS_RDF/N3</li>
    <li>RS_JSON</li>
    <li>RS_RDF</li>
    </ul>
</p>
--%>
</div><!-- content -->
</body></html>

