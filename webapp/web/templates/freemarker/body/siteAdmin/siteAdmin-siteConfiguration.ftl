<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration site configuration panel -->

<#if siteConfig?has_content>
    <section class="pageBodyGroup" role="region">
        <h3>${i18n().site_config}</h3>
        
        <ul role="navigation">
            <#if siteConfig.internalClass?has_content>
                <li role="listitem"><a href="${siteConfig.internalClass}" title="${i18n().internal_class}">${i18n().internal_class_i_capped}</a></li>
            </#if>     
            
            <#if siteConfig.manageProxies?has_content>
                <li role="listitem"><a href="${siteConfig.manageProxies}" title="${i18n().manage_profile_editing}">${i18n().manage_profile_editing}</a></li>
            </#if>  
            
            <#if siteConfig.pageManagement?has_content>
                <li role="listitem"><a href="${siteConfig.pageManagement}" title="${i18n().page_management}">${i18n().page_management}</a></li>
            </#if>        
            
            <#if siteConfig.menuManagement?has_content>
                <li role="listitem"><a href="${siteConfig.menuManagement}" title="${i18n().menu_ordering}">${i18n().menu_ordering_mixed_caps}</a></li>
            </#if>      
            
            <#if siteConfig.siteInfo?has_content>
                <li role="listitem"><a href="${siteConfig.siteInfo}" title="${i18n().site_information}">${i18n().site_information}</a></li>
            </#if>
            
             <#if siteConfig.userAccounts?has_content>
                <li role="listitem"><a href="${siteConfig.userAccounts}" title="${i18n().user_accounts}">${i18n().user_accounts}</a></li>
             </#if>        
        </ul>
    </section>
</#if>
