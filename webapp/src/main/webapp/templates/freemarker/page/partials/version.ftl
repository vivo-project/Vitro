<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template for version/revision information -->

<#-- Only show version info if user has access -->
<#if user.hasRevisionInfoAccess>
    <div id="revision">
        ${i18n().version} <a href="${version.moreInfoUrl}" title="${i18n().version}">${version.label}</a>
    </div>
</#if>
