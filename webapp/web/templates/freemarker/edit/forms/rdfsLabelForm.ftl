<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#assign actionText = "Add new" />
<#if editConfiguration.dataPropertyStatement?has_content>
    <#assign actionText = "Edit"/>
</#if>
<#assign submitLabel>${actionText} label</#assign>

<h2>${actionText} <em>label</em> for ${editConfiguration.subjectName}</h2>

<#assign literalValues = "${editConfiguration.dataLiteralValuesAsString}" />

<form class="editForm" action = "${submitUrl}" method="post">
    <input type="text" name="${editConfiguration.varNameForObject}" id="label" size="30" value="${literalValues}"} role="input"/>
    <input type="hidden" name="editKey" id="editKey" value="${editKey}" role="input"/>
    <input type="hidden" name="vitroNsProp" value="true" role="input"/>
    
    <p class="submit">
        <input type="submit" id="submit" value="${submitLabel}" role="input"/>
        or <a href="${cancelUrl}" class="cancel" title="cancel">Cancel</a>
    </p>
    
</form>

<#--The original jsp included a delete form for deleting rdfs label.  
If required, deletion can be supported but it does not appear that is required currently. 
-->