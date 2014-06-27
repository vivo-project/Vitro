<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template that presents the SPARQL query form. -->

<div id="content" class="sparqlform">
    <h2>SPARQL Query</h2>
    <form action='${submitUrl}' method="get">
        <h3>Query:</h3>
        <div>
            <textarea name='query' rows ='30' cols='100' class="span-23 maxWidth">
${sampleQuery}
            </textarea>
        </div>

        <div>
        	 <h3>Format for SELECT and ASK query results:</h3>
        	 <label><input type='radio' name='resultFormat' value='text/plain' checked>RS_TEXT</label>
        	 <label><input type='radio' name='resultFormat' value='text/csv'>CSV</label>
        	 <label><input type='radio' name='resultFormat' value='text/tab-separated-values'>TSV</label>
        	 <label><input type='radio' name='resultFormat' value='application/sparql-results+xml'>RS_XML</label>
        	 <label><input type='radio' name='resultFormat' value='application/sparql-results+json'>RS_JSON</label>
        </div>

        <div>
        	 <h3>Format for CONSTRUCT and DESCRIBE query results:</h3>
        	 <label><input type='radio' name='rdfResultFormat' value='text/plain'>N-Triples</label>
        	 <label><input type='radio' name='rdfResultFormat' value='application/rdf+xml' checked>RDF/XML</label>
        	 <label><input type='radio' name='rdfResultFormat' value='text/n3'>N3</label>
        	 <label><input type='radio' name='rdfResultFormat' value='text/turtle'>Turtle</label>
        	 <label><input type='radio' name='rdfResultFormat' value='application/json'>JSON-LD</label>
        </div>

        <input class="submit" type="submit" value="Run Query" />
    </form>
</div><!-- content -->
