<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration site configuration panel -->

<#if siteConfig?has_content>
    <div class="pageBodyGroup">
        
        <h3>Site Configuration</h3>
        
        <ul>

            <#if siteConfig.urls.siteInfo??>
                <li><a href="${siteConfig.urls.siteInfo}">Site information</a></li>
            </#if>
            
            <#if siteConfig.urls.menuN3Editor??>
                <#-- once new menu management is ready to go, we'll want to remove menu.n3 editor -->
                <li><a href="${siteConfig.urls.menuN3Editor}">menu.n3 editor</a></li>
            </#if>
            
            <#if siteConfig.urls.menuManagement??>
                <li><a href="${siteConfig.urls.menuManagement}">Menu Management</a> (in development)</li>
            </#if>
            
            <#if siteConfig.urls.userList??>
                <li><a href="${siteConfig.urls.userList}">User accounts</a></li>
            </#if>
            
        </ul>
    </div>
</#if>
