<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration site configuration panel -->

<#if siteConfig?has_content>
    <section class="pageBodyGroup" role="region">
        <h3>Site Configuration</h3>
        
        <ul role="navigation">
            <#if siteConfig.internalClass?has_content>
                <li role="listitem"><a href="${siteConfig.internalClass}">Institutional internal class</a></li>
            </#if>     
            
            <#if siteConfig.manageProxies?has_content>
                <li role="listitem"><a href="${siteConfig.manageProxies}">Manage profile editing</a></li>
            </#if>     
            
            <#if siteConfig.menuManagement?has_content>
                <li role="listitem"><a href="${siteConfig.menuManagement}">Menu management</a></li>
            </#if>      
            
            <#if siteConfig.siteInfo?has_content>
                <li role="listitem"><a href="${siteConfig.siteInfo}">Site information</a></li>
            </#if>
            
            <#if siteConfig.startupStatus?has_content>
                <li role="listitem">
                    <a href="${siteConfig.startupStatus}">Startup status</a>
                    <#if siteConfig.startupStatusAlert>
                        <img id="alertIcon" src="${urls.images}/iconAlert.png" width="20" height="20" alert="Error alert icon" />
                    </#if>
                </li>
            </#if>   
            
             <#if siteConfig.userAccounts?has_content>
                <li role="listitem"><a href="${siteConfig.userAccounts}">User accounts</a></li>
             </#if>        
        </ul>
    </section>
</#if>
