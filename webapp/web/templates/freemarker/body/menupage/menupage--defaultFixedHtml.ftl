<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#--Save to variable indicated in generator -->

<#assign htmlExists = false/>
<#if variableName?has_content>
	<#assign htmlExists = true />
</#if>
<#if htmlExists>
	${variableName}
<#else>
	No HTML specified.  
</#if>


