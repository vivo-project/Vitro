<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#if title?? && title?has_content>
    <title>${title?html}</title>
<#else>
    <title>${siteName}</title>
</#if>
