<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration advanced data tools panel -->

<#if dataTools?has_content>
    <section class="pageBodyGroup" role="region">
        <h3>Advanced Data Tools</h3>
        
        <ul role="navigation">
            <li role="listitem"><a href="${dataTools.rdfData}" title="Add/Remove RDF data">Add/Remove RDF data</a></li>
            <li role="listitem"><a href="${dataTools.ingest}" title="Ingest tools">Ingest tools</a></li>
            <li role="listitem"><a href="${dataTools.rdfExport}" title="RDF export">RDF export</a></li>
            <li role="listitem"><a href="${dataTools.sparqlQuery}" title="SPARQL query">SPARQL query</a></li>
            <li role="listitem"><a href="${dataTools.sparqlQueryBuilder}" title="SPARQL query builder">SPARQL query builder</a></li>
        </ul>
    </section>
</#if>