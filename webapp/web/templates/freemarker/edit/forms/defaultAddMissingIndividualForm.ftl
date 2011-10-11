<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#assign formTitle>
 "${editConfiguration.propertyPublicDomainTitle}" entry for ${editConfiguration.subjectName}
</#assign>
<#if editConfiguration.objectUri?has_content>
	<#assign formTitle>Edit ${formTitle} </#assign>
<#else>
 	<#assign formTitle>Create ${formTitle} </#assign>
</#if>

<h2>${formTitle}</h2>

<form class="editForm" action = "${submitUrl}">
	<input type="hidden" name="editKey" id="editKey" value="${editKey}" />
	<input type="text" name="name" id="name" label="name (required)" size="30"/>
	<br/>
	
	<div style="margin-top: 0.2em">
		<input type="submit" id="submit" value="${editConfiguration.submitLabel}"
		<span class="or"> or </span>
		<a title="Cancel" href="${editConfiguration.cancelUrl}">Cancel</a>
	</div>     
	
</form>



