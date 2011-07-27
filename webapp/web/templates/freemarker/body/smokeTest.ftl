<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for the body of the SmokeTest page -->
<#-- TODO: This is an initial implementation and will continue to evolve. -->


<#if results??>
	<#list results as x>
		<#if x??>
			<h2>${x.result}</h2>
		</#if>
	</#list>
</#if>