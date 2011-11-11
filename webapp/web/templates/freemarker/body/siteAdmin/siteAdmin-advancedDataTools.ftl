<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration advanced data tools panel -->

<#if dataTools?has_content>
    <section class="pageBodyGroup" role="region">
        <h3>Advanced Data Tools</h3>
        
        <ul role="navigation">
            <li role="listitem"><a href="${dataTools.rdfData}">Add/Remove RDF data</a></li>
            <li role="listitem"><a href="${dataTools.ingest}">Ingest tools</a></li>
            <li role="listitem"><a href="${dataTools.rdfExport}">RDF export</a></li>
            <li role="listitem"><a href="${dataTools.sparqlQuery}">SPARQL query</a></li>
            <li role="listitem"><a href="${dataTools.sparqlQueryBuilder}">SPARQL query builder</a></li>
        </ul>
    </section>
</#if>