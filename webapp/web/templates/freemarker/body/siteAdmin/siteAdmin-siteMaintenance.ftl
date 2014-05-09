<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for the main Site Administration page -->

<#if siteMaintenance?has_content>
    <section class="pageBodyGroup" role="region">
        <h3>${i18n().site_maintenance}</h3>
        
        <ul role="navigation">                        
            <#if siteMaintenance.rebuildSearchIndex?has_content>
                <li role="listitem"><a href="${siteMaintenance.rebuildSearchIndex }" title="${i18n().rebuild_search_index}">${i18n().rebuild_search_index}</a></li>
            </#if>
            
            <#if siteMaintenance.rebuildVisCache?has_content>
                <li role="listitem"><a href="${siteMaintenance.rebuildVisCache}" title="${i18n().rebuild_vis_cache}">${i18n().rebuild_vis_cache}</a></li>
            </#if>
            
            <#if siteMaintenance.recomputeInferences?has_content>
                <li role="listitem"><a href="${siteMaintenance.recomputeInferences}" title="${i18n().recompute_inferences}">${i18n().recompute_inferences_mixed_caps}</a></li>
            </#if>
            
            <#if siteMaintenance.startupStatus?has_content>
                <li role="listitem">
                    <a href="${siteMaintenance.startupStatus}" title="${i18n().startup_status}">${i18n().startup_status}</a>
                    <#if siteMaintenance.startupStatusAlert>
                        <img id="alertIcon" src="${urls.images}/iconAlert.png" width="20" height="20" alt="${i18n().error_alert_icon}" />
                    </#if>
                </li>
            </#if>   
            
            <#if siteMaintenance.restrictLogins?has_content>
                <li role="listitem"><a href="${siteMaintenance.restrictLogins}" title="${i18n().restrict_logins}">${i18n().restrict_logins_mixed_caps}</a></li>
            </#if>
            
            <#if siteMaintenance.activateDeveloperPanel?has_content>
                <li role="listitem"><a href="${siteMaintenance.activateDeveloperPanel}" title="${i18n().activate_developer_panel}">${i18n().activate_developer_panel_mixed_caps}</a></li>
            </#if>
        </ul>
    </section>
</#if>