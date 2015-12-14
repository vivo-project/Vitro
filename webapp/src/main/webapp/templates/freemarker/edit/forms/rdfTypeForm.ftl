<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#--Assign variables from editConfig-->
<#assign rangeOptions = editConfiguration.pageData.object />
<#assign rangeOptionsExist = false />
<#if (rangeOptions?keys?size > 0)>
    <#assign rangeOptionsExist = true/>
</#if>

<h2>${editConfiguration.formTitle}</h2>


    <#if rangeOptionsExist  = true >
        <#assign rangeOptionKeys = rangeOptions?keys />
        <form class="editForm" action = "${submitUrl}">
            <input type="hidden" name="editKey" id="editKey" value="${editKey}" role="input" />                
            <select id="object" name="object" role="select">
                <#list rangeOptionKeys as key>
                 <option value="${key}" <#if editConfiguration.objectUri?has_content && editConfiguration.objectUri = key>selected</#if> role="option">${rangeOptions[key]}</option>
                </#list>
            </select>
            
            <p>
                <input type="submit" id="submit" value="${editConfiguration.submitLabel}" role="button "/>
                <span class="or"> ${i18n().or} </span>
                <a title="${i18n().cancel_title}" class="cancel" href="${cancelUrl}">${i18n().cancel_link}</a>
            </p>
        </form>
    <#else>
        <p> ${i18n().no_classes_to_select}  </p>  
    </#if>

<#if editConfiguration.propertyOfferCreateNewOption = true>
<#include "defaultOfferCreateNewOptionForm.ftl">

</#if>

<#if editConfiguration.propertySelectFromExisting = false && editConfiguration.propertyOfferCreateNewOption = false>
<p>${i18n().editing_prohibited} </p>
</#if>

<#if editConfiguration.includeDeletionForm = true>
<#include "defaultDeletePropertyForm.ftl">
</#if>

