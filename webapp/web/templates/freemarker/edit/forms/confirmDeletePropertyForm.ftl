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
	<#assign statement = editConfiguration.dataStatementDisplay />
</#if>

<#assign deletionTemplateName = editConfiguration.deleteTemplate/>

<form action="${editConfiguration.deleteProcessingUrl}" method="get">
    <h2>Are you sure you want to delete the following entry from <em>${editConfiguration.propertyName}</em>?</h2>
    
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
        <input type="submit" id="submit" value="Delete" role="button"/>
        or 
        <a class="cancel" title="Cancel" href="${editConfiguration.cancelUrl}">Cancel</a>
    <#if editConfiguration.objectProperty = true>
    </p>
    </#if>
</form>