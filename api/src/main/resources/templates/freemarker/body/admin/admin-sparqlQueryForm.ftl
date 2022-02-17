<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template that presents the SPARQL query form. -->

<div id="content" class="sparqlform">
    <h2>${i18n().sparql_query_title}</h2>
    <form action='${submitUrl}?posted' method="post">
        <h3>${i18n().sparql_query_header}:</h3>

        <textarea name='query' rows='30' cols='100' class="span-23 maxWidth" id="query-area">${sampleQuery}</textarea>

        <div class="options">
			 <h3>${i18n().sparql_query_select_ask_results}:</h3>
        	 <label><input type='radio' name='resultFormat' value='text/plain' checked>RS_TEXT</label>
        	 <label><input type='radio' name='resultFormat' value='text/csv'>CSV</label>
        	 <label><input type='radio' name='resultFormat' value='text/tab-separated-values'>TSV</label>
        	 <label><input type='radio' name='resultFormat' value='application/sparql-results+xml'>RS_XML</label>
        	 <label><input type='radio' name='resultFormat' value='application/sparql-results+json'>RS_JSON</label>
        </div>

        <div class="options">
			 <h3>${i18n().sparql_query_construct_describe_results}:</h3>
        	 <label><input type='radio' name='rdfResultFormat' value='text/plain'>N-Triples</label>
        	 <label><input type='radio' name='rdfResultFormat' value='application/rdf+xml' checked>RDF/XML</label>
        	 <label><input type='radio' name='rdfResultFormat' value='text/n3'>N3</label>
        	 <label><input type='radio' name='rdfResultFormat' value='text/turtle'>Turtle</label>
        	 <label><input type='radio' name='rdfResultFormat' value='application/json'>JSON-LD</label>
        </div>

		<div class="options">
			<input type="checkbox" id="download" name="download" value="true">
			<label for="download"> ${i18n().sparql_query_save_results}</label><br>
		</div>

        <input class="submit" type="submit" value="${i18n().sparql_query_run_query}" />
    </form>
</div><!-- content -->

${stylesheets.add('<link rel="stylesheet" href="//cdn.jsdelivr.net/npm/yasgui-yasqe@2.11.22/dist/yasqe.min.css" />')}
${scripts.add('<script type="text/javascript" src="//cdn.jsdelivr.net/npm/yasgui-yasqe@2.11.22/dist/yasqe.bundled.min.js"></script>',
'<script type="text/javascript" src="${urls.base}/js/sparql/init-yasqe.js"></script>')}
