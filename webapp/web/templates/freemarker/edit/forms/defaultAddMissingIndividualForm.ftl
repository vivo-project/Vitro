<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#assign formTitle>
 "${editConfiguration.propertyPublicDomainTitle}" entry for ${editConfiguration.subjectName}
</#assign>
<#if editConfiguration.objectUri?has_content>
    <#assign formTitle>Edit ${formTitle} </#assign>
    <#assign submitLabel>Save changes</#assign>
<#else>
    <#assign formTitle>Create ${formTitle} </#assign>
    <#assign submitLabel>Create "${editConfiguration.propertyPublicDomainTitle}" entry</#assign>
</#if>

<h2>${formTitle}</h2>

<form class="editForm" action="${submitUrl}">
    <input type="hidden" name="editKey" id="editKey" value="${editKey}" role="input" />
    <input type="text" name="name" id="name" label="name (required)" size="30" role="input" />
    
    <p class="submit">
        <input type="submit" id="submit" value="${submitLabel}" role="submit" />
        <span class="or"> or </span>
        <a title="Cancel" href="${editConfiguration.cancelUrl}">Cancel</a>
    </p>     
</form>