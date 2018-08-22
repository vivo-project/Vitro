<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<section id="audit" role="region">
    <h2>${i18n().audit_title(username)}</h2>

    <table class="history">
        <tbody>
            <tr>
                <th>${i18n().audit_count}</th>
                <th>${i18n().audit_date}</th>
                <th>${i18n().audit_dataset}</th>
            </tr>
            <#assign pos = results.offset>
            <#list results.datasets as dataset>
                <#assign pos = pos + 1>
                <tr>
                    <td>${pos}</td>
                    <td>${dataset.requestTime?datetime}</td>
                    <td>
                        <#list dataset.graphUris as graphUri>
                            <#assign added = listAddedStatements(dataset, graphUri)>
                            <#assign removed = listRemovedStatements(dataset, graphUri)>

                            <b>${i18n().audit_graph}</b>: ${graphUri}<br />
                            <#if added??>
                                <br /><b>${i18n().audit_added}</b>:<br />
                                <div><pre style="font-size: 0.8em;">${added?html}</pre></div>
                            </#if>
                            <#if removed??>
                                <br /><b>${i18n().audit_removed}</b>:<br />
                                <div><pre style="font-size: 0.8em;">${removed?html}</pre></div>
                            </#if>
                        </#list>
                    </td>
                </tr>
            </#list>
        </tbody>
    </table>
    <#if prevPage??><a class="prev" href="${prevPage}" title="${i18n().previous}">${i18n().previous}</a></#if>
    <#if nextPage??><a class="next" href="${nextPage}" title="${i18n().next_capitalized}">${i18n().next_capitalized}</a></#if>
</section>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/audit/audit.css" />')}
