<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template for the body of the About page -->

<h1>${siteName!}</h1>

<#if aboutText?has_content>
    <div class="pageGroupBody" id="aboutText">${aboutText}</div>
</#if>

<#if acknowledgeText?has_content>
    <div class="pageGroupBody" id="acknowledgementText">${acknowledgeText}</div>
</#if>
