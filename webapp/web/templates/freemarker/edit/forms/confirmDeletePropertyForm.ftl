<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#assign toBeDeletedClass = "dataProp" />

<#if editConfiguration.objectProperty = true>
    <#assign toBeDeletedClass = "objProp" />
</#if>

<#assign statement = editConfiguration.statementDisplay />

<form action="${editConfiguration.deleteProcessingUrl}" method="get">
    <h2>Are you sure you want to delete the following entry from <em>${editConfiguration.propertyName}</em>?</h2>
    
    <p class="toBeDeleted ${toBeDeletedClass}">
        <#if editConfiguration.objectProperty = true>
            <#if statement.object?has_content>
                <#include "propStatement-default.ftl" />
            </#if>
        <#else>
            ${statement.dataValue}
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