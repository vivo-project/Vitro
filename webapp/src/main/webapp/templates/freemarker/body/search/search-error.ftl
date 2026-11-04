<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template for displaying search error message -->

<#if title??>
    <h1>${title?html}</h1>
</#if>

<p>
${message?html}
</p>
<#include "search-help.ftl" >
