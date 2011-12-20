<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration site configuration panel -->

<#if siteConfig?has_content>
    <section class="pageBodyGroup" role="region">
        <h3>Site Configuration</h3>
        
        <ul role="navigation">
            <#if siteConfig.internalClass?has_content>
                <li role="listitem"><a href="${siteConfig.internalClass}" title="Institutional internal class">Institutional internal class</a></li>
            </#if>     
            
            <#if siteConfig.manageProxies?has_content>
                <li role="listitem"><a href="${siteConfig.manageProxies}" title="Manage profile editing">Manage profile editing</a></li>
            </#if>     
            
            <#if siteConfig.menuManagement?has_content>
                <li role="listitem"><a href="${siteConfig.menuManagement}" title="Menu management">Menu management</a></li>
            </#if>      
            
            <#if siteConfig.siteInfo?has_content>
                <li role="listitem"><a href="${siteConfig.siteInfo}" title="Site information">Site information</a></li>
            </#if>
            
            <#if siteConfig.startupStatus?has_content>
                <li role="listitem">
                    <a href="${siteConfig.startupStatus}" title="Startup status">Startup status</a>
                    <#if siteConfig.startupStatusAlert>
                        <img id="alertIcon" src="${urls.images}/iconAlert.png" width="20" height="20" alert="Error alert icon" />
                    </#if>
                </li>
            </#if>   
            
             <#if siteConfig.userAccounts?has_content>
                <li role="listitem"><a href="${siteConfig.userAccounts}" title="User accounts">User accounts</a></li>
             </#if>        
        </ul>
    </section>
</#if>
