<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for the main Site Administration page -->

<#-- <#if indexRebuildCache?has_content> -->
    <section class="pageBodyGroup indexCacheRebuild">
        <h3>Refresh Content</h3>
        
        <ul>

            <#if indexCacheRebuild.urls.rebuildSearchIndex??>
                <li><a href="${indexCacheRebuild.urls.rebuildSearchIndex }">Rebuild search index</a></li>
            </#if>
            
            <#if indexCacheRebuild.urls.rebuildClassGroupCache??>
                <li><a href="${indexCacheRebuild.urls.rebuildClassGroupCache}">Rebuild class group cache</a></li>
            </#if>
            
            <#if indexCacheRebuild.urls.rebuildVisCache??>
                <li><a href="indexCacheRebuild.urls.rebuildVisCache">Rebuild visualization cache</a></li>
            </#if>
            
            <#if indexCacheRebuild.urls.recomputeInferences??>
                <li><a href="${indexCacheRebuild.urls.recomputeInferences}">Recompute inferences</a></li>
            </#if>
            
        </ul>
    </section>
<#-- </#if> -->