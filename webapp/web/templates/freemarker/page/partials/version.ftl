<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for version/revision information -->

<#-- Only show version info if user has access -->
<#if user.hasRevisionInfoAccess>
    <div id="revision">
        Version <a href="${version.moreInfoUrl}" title="version">${version.label}</a>
    </div>
</#if>