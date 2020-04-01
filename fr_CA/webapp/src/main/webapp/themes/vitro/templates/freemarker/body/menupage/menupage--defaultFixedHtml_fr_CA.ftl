<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#--Save to variable indicated in generator -->

<#assign htmlExists = false/>
<#if variableName?has_content>
	<#assign htmlExists = true />
</#if>
<#if htmlExists>
	${.globals[variableName]}
<#else>
	${i18n().no_html_specified}
</#if>


