<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for restricting (or opening) access to logins. -->

<h2>${i18n().restrict_logins}</h2>
    <#if messageAlreadyRestricted??>
        <#assign errorMessage = "${i18n().logins_already_restricted}" />
    </#if>
    
    <#if messageAlreadyOpen??>
        <#assign errorMessage = "${i18n().logins_not_already_restricted}" />
    </#if>
    
    <#if errorMessage?has_content>
        <section id="error-alert" role="alert">
            <img src="${urls.images}/iconAlert.png" width="24" height="24" alt="${i18n().error_alert_icon}" />
            <p>${errorMessage}</p>
        </section>
    </#if>

    <#if messageRestricting??>
        <#assign successMessage = "${i18n().logins_restricted}" />
    </#if>
    
    <#if messageOpening??>
        <#assign successMessage = "${i18n().logins_not_restricted}" />
    </#if>

    <#if successMessage?has_content>
        <section class="success">
            <p>${successMessage}</p>
        </section>
    </#if>


<section id="restrict-login" role="region">
    <#if restricted == true>
        <h4>${i18n().logins_are_restricted}</h4>
        <p><a href="${openUrl}" title="${i18n().remove_restrictions}">${i18n().remove_restrictions}</a></p>
    <#else>
        <h4>${i18n().logins_are_open}</h4>
        <p><a href="${restrictUrl}" title="${i18n().restrict_logins}">${i18n().restrict_logins}</a></p>
    </#if>
</section>
