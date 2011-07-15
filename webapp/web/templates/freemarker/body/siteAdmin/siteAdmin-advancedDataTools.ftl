<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration advanced data tools panel -->

<#if dataTools??>
    <section class="pageBodyGroup">
    
        <h3>Advanced Data Tools</h3>
        
        <ul>
            <li><a href="${dataTools.urls.ingest}">Ingest tools</a></li>   
            <li><a href="${dataTools.urls.rdfData}">Add/Remove RDF data</a></li>
            <li><a href="${dataTools.urls.rdfExport}">RDF export</a></li>
            <li><a href="${dataTools.urls.sparqlQuery}">SPARQL query</a></li>
            <li><a href="${dataTools.urls.sparqlQueryBuilder}">SPARQL query builder</a></li>         
        </ul>
    </section>
</#if>
