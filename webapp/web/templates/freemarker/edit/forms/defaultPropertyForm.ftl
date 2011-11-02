<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<h2>${editConfiguration.formTitle}</h2>

<#if editConfiguration.propertySelectFromExisting = true>
    <#if editConfiguration.rangeOptionsExist  = true >
        <#assign rangeOptionKeys = editConfiguration.rangeOptions?keys />
        <form class="editForm" action = "${submitUrl}">
            <input type="hidden" name="editKey" id="editKey" value="${editKey}" role="input" />
            <#if editConfiguration.propertyPublicDescription?has_content>
                <p>${editConfiguration.propertyPublicDescription}</p>
                
                <select id="objectVar" name="objectVar" role="select">
                    <#list rangeOptionKeys as key>
                     <option value="${key}" <#if editConfiguration.objectUri?has_content && editConfiguration.objectUri = key>selected</#if> role="option">${editConfiguration.rangeOptions[key]}</option>
                    </#list>
                </select>
                
                <p>
                    <input type="submit" id="submit" value="${editConfiguration.submitLabel}" role="button "/>
                    <span class="or"> or </span>
                    <a title="Cancel" href="${editConfiguration.cancelUrl}">Cancel</a>
                </p>
            </#if>  
        </form>
    <#else>
        <p> There are no entries in the system from which to select.  </p>  
    </#if>
</#if>

<#if editConfiguration.propertyOfferCreateNewOption = true>
<#include "defaultOfferCreateNewOptionForm.ftl">

</#if>

<#if editConfiguration.propertySelectFromExisting = false && editConfiguration.propertyOfferCreateNewOption = false>
<p>This property is currently configured to prohibit editing. </p>
</#if>


<#if editConfiguration.includeDeletionForm = true>
<#include "defaultDeletePropertyForm.ftl">
</#if>

