<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#--Save to variable indicated in generator -->

<#assign htmlExists = false/>
<#if variableName?has_content>
	<#assign htmlExists = true />
</#if>
<#if htmlExists>
    <#-- Promote a stored page-title h2 to h1 when it is the first element. -->
    <#assign html = .globals[variableName]!"">
    <#if html?trim?starts_with("<h2>")>
        ${html?replace("<h2>", "<h1>", "f")?replace("</h2>", "</h1>", "f")}
    <#else>
        ${html}
    </#if>
<#else>
	${i18n().no_html_specified}
</#if>


