<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#assign actionText = "${i18n().add_new}" />
<#if editConfiguration.dataPropertyStatement?has_content>
    <#assign actionText = "${i18n().edit_capitalized}"/>
</#if>
<#assign submitLabel>${actionText} ${i18n().label}</#assign>

<h2>${actionText} <em>${i18n().label}</em> for ${editConfiguration.subjectName}</h2>

<#assign literalValues = "${editConfiguration.dataLiteralValuesAsString}" />

<form class="editForm" action = "${submitUrl}" method="post">
    <input type="text" name="${editConfiguration.varNameForObject}" id="label" size="70" value="${literalValues?html}" role="input"/>
    <input type="hidden" name="editKey" id="editKey" value="${editKey}" role="input"/>
    <input type="hidden" name="vitroNsProp" value="true" role="input"/>
    
    <p class="submit">
        <input type="submit" id="submit" value="${submitLabel}" role="input"/>
        or <a href="${cancelUrl}" class="cancel" title="${i18n().cancel_title}">${i18n().cancel_link}</a>
    </p>
    
</form>

<#--The original jsp included a delete form for deleting rdfs label.  
If required, deletion can be supported but it does not appear that is required currently. 
-->