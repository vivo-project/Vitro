<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for admin panel on individual profile page -->

<#import "lib-form.ftl" as form>

<#if individual.showAdminPanel>
    <section id="admin">
        <h3 id="adminPanel">${i18n().admin_panel}</h3><a class="edit-individual" href="${individual.controlPanelUrl()}" title="${i18n().edit_this_individual}">${i18n().edit_this_individual}</a>
        
        <section id = "verbose-mode">
        <#if verbosePropertySwitch?has_content>
            <#assign anchorId = "verbosePropertySwitch">
            <#assign currentValue = verbosePropertySwitch.currentValue?string("${i18n().verbose_status_on}", "${i18n().verbose_status_off}")>
            <#assign newValue = verbosePropertySwitch.currentValue?string("${i18n().verbose_status_off}", "${i18n().verbose_status_on}")>
            <span>${i18n().verbose_property_status} <b>${currentValue}</b> | </span>
            <a id="${anchorId}" class="verbose-toggle small" href="${verbosePropertySwitch.url}" title="${i18n().verbose_control}"><#if verbosePropertySwitch.currentValue!>${i18n().verbose_turn_off}<#else>${i18n().verbose_turn_on}</#if></a>
        </#if> 
        </section>

        <p class="uri-link">${i18n().resource_uri}: <a href="${individual.uri}" target="_blank" title="${i18n().resource_uri}">${individual.uri}</a></p>
    </section>
</#if>