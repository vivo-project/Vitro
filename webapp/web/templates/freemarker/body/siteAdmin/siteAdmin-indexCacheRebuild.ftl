<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for the main Site Administration page -->

<#if indexCacheRebuild?has_content>
    <section class="pageBodyGroup indexCacheRebuild" role="region">
        <h3>${i18n().refresh_content}</h3>
        
        <ul role="navigation">                        
            <#if indexCacheRebuild.rebuildSearchIndex?has_content>
                <li role="listitem"><a href="${indexCacheRebuild.rebuildSearchIndex }" title="${i18n().rebuild_search_index}">${i18n().rebuild_search_index}</a></li>
            </#if>
            
            <#if indexCacheRebuild.rebuildVisCache?has_content>
                <li role="listitem"><a href="${indexCacheRebuild.rebuildVisCache}" title="${i18n().rebuild_vis_cache}">${i18n().rebuild_vis_cache}</a></li>
            </#if>
            
            <#if indexCacheRebuild.recomputeInferences?has_content>
                <li role="listitem"><a href="${indexCacheRebuild.recomputeInferences}" title="${i18n().recompute_inferences}">${i18n().recompute_inferences}</a></li>
            </#if>
        </ul>
    </section>
</#if>