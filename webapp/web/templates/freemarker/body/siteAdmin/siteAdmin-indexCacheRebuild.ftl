<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for the main Site Administration page -->

<#if indexCacheRebuild?has_content>
    <section class="pageBodyGroup indexCacheRebuild" role="region">
        <h3>Refresh Content</h3>
        
        <ul role="navigation">
            <#if indexCacheRebuild.rebuildClassGroupCache?has_content>
                <li role="listitem"><a href="${indexCacheRebuild.rebuildClassGroupCache}" title="Rebuild class group cache">Rebuild class group cache</a></li>
            </#if>
            
            <#if indexCacheRebuild.rebuildSearchIndex?has_content>
                <li role="listitem"><a href="${indexCacheRebuild.rebuildSearchIndex }" title="Rebuild search index">Rebuild search index</a></li>
            </#if>
            
            <#if indexCacheRebuild.rebuildVisCache?has_content>
                <li role="listitem"><a href="${indexCacheRebuild.rebuildVisCache}" title="Rebuild visualization cache">Rebuild visualization cache</a></li>
            </#if>
            
            <#if indexCacheRebuild.recomputeInferences?has_content>
                <li role="listitem"><a href="${indexCacheRebuild.recomputeInferences}" title="Recompute inferences">Recompute inferences</a></li>
            </#if>
        </ul>
    </section>
</#if>