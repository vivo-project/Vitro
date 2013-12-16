<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#assign toBeDeletedClass = "dataProp" />

<#if editConfiguration.objectProperty = true>
    <#assign toBeDeletedClass = "objProp" />
    <#if editConfiguration.objectStatementDisplay?has_content>
    	<#assign statement = editConfiguration.objectStatementDisplay />
    	<#--Reviewer and editor role list views required object property template model object for property-->
    	<#assign property = editConfiguration.objectPropertyStatementDisplayPropertyModel />
    </#if>
<#else>
	<#assign statement = editConfiguration.dataLiteralValuesAsString />   
</#if>

<#assign deletionTemplateName = editConfiguration.deleteTemplate/>

<form action="${editConfiguration.deleteProcessingUrl}" method="get">
    <h2>${i18n().confirm_entry_deletion_from} <em>${editConfiguration.propertyName}</em>?</h2>
    
    <p class="toBeDeleted ${toBeDeletedClass}">
        <#if editConfiguration.objectProperty = true>
            <#if statement?has_content>
                <#include deletionTemplateName />
            </#if>
        <#else>
            ${statement}  
        </#if>
    </p>
    
    <input type="hidden" name="subjectUri"   value="${editConfiguration.subjectUri}" role="input" />
    <input type="hidden" name="predicateUri" value="${editConfiguration.predicateUri}" role="input" />
    <input type="hidden" name="domainUri" value="${editConfiguration.domainUri!}" role="input" />
    <input type="hidden" name="rangeUri" value="${editConfiguration.rangeUri!}" role="input" />
    <input type="hidden" name="deleteObjectUri" value="${editConfiguration.customDeleteObjectUri!}" />
    <#if editConfiguration.dataProperty = true>
        <input type="hidden" name="datapropKey" value="${editConfiguration.datapropKey}" role="input" />
        <input type="hidden" name="vitroNsProp" value="${editConfiguration.vitroNsProperty}" role="input" />
    <#else>
        <input type="hidden" name="objectUri"    value="${editConfiguration.objectUri}" role="input" />
    </#if>
    
   <br />
    <#if editConfiguration.objectProperty = true>
    <p class="submit">
    </#if>
        <input type="submit" id="submit" value="${i18n().delete_button}" role="button"/>
        or 
        <a class="cancel" title="${i18n().cancel_title}" href="${editConfiguration.cancelUrl}">${i18n().cancel_link}</a>
    <#if editConfiguration.objectProperty = true>
    </p>
    </#if>
</form>
