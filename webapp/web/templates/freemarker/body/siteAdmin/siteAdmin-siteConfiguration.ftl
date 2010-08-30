<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration site configuration panel -->

<#if siteConfig??>
    <div class="pageBodyGroup">
        
        <h3>Site Configuration</h3>
        
        <ul>
            <#if siteConfig.listPortalsUrl??>
                <li><a href="${siteConfig.siteInfoUrl}">Current portal information</a></li>
                <li><a href="${siteConfig.listPortalsUrl}">List all portals</a></li>
            <#else>
                <li><a href="${siteConfig.siteInfoUrl}">Site information</a></li>
            </#if>
            
            <li><a href="${siteConfig.tabManagementUrl}">Tab management</a></li>
            
            <#if siteConfig.userManagementUrl??>
                <li><a href="${siteConfig.userManagementUrl}">User accounts</a></li>  
            </#if>
        
        </ul>
    </div>
</#if>