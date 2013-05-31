<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration advanced data tools panel -->

<#if dataTools?has_content>
    <section class="pageBodyGroup" role="region">
        <h3>${i18n().advanced_data_tools}</h3>
        
        <ul role="navigation">
            <#if dataTools.rdfData?has_content>
                <li role="listitem"><a href="${dataTools.rdfData}" title="${i18n().add_remove_rdf}">${i18n().add_remove_rdf}</a></li>
            </#if>     
            <#if dataTools.ingest?has_content>
                <li role="listitem"><a href="${dataTools.ingest}" title="${i18n().ingest_tools}">${i18n().ingest_tools}</a></li>
            </#if>     
            <#if dataTools.rdfExport?has_content>
                <li role="listitem"><a href="${dataTools.rdfExport}" title="${i18n().rdf_export}">${i18n().rdf_export}</a></li>
            </#if>     
            <#if dataTools.sparqlQuery?has_content>
                <li role="listitem"><a href="${dataTools.sparqlQuery}" title="${i18n().sparql_query}">${i18n().sparql_query}</a></li>
            </#if>     
            <#if dataTools.sparqlQueryBuilder?has_content>
                <li role="listitem"><a href="${dataTools.sparqlQueryBuilder}" title="${i18n().sparql_query_builder}">${i18n().sparql_query_builder}</a></li>
            </#if>     
        </ul>
    </section>
</#if>