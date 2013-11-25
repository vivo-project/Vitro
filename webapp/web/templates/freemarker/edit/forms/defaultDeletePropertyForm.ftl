<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<form class="deleteForm" action="${editConfiguration.mainEditUrl}" method="get"> 
    <h3 class="delete-entry">${i18n().delete_entry_capitalized}</h3>
          
    <label for="delete"></label>
    
    <input type="hidden" name="subjectUri"   value="${editConfiguration.subjectUri}"/>
    <input type="hidden" name="predicateUri" value="${editConfiguration.predicateUri}"/>
    <input type="hidden" name="domainUri" value="${editConfiguration.domainUri!}"/>
    <input type="hidden" name="rangeUri" value="${editConfiguration.rangeUri!}"/>
    <input type="hidden" name="cmd"          value="delete"/>
    <input type="hidden" name="editKey" value="${editConfiguration.editKey}"/>
    <#if editConfiguration.dataProperty = true>
        <input type="hidden" name="datapropKey" value="${editConfiguration.datapropKey}" />
        <input type="submit" id="delete" value="${i18n().delete_button}" role="button "/>
    </#if>
    
    <#--The original jsp included vinput tag with cancel=empty string for case where both select from existing
    and offer create new option are true below so leaving as Cancel for first option but not second below-->
    <#if editConfiguration.objectProperty = true> 
        <input type="hidden" name="objectUri" value="${editConfiguration.objectUri}"/>    
    
        <#if editConfiguration.propertySelectFromExisting = false && editConfiguration.propertyOfferCreateNewOption = false>
            <p>
                <input type="submit" id="delete" value="${i18n().delete_button}" role="button "/>
                <span class="or"> ${i18n().or} </span>
                <a title="${i18n().cancel_title}" href="${editConfiguration.cancelUrl}">${i18n().cancel_link}</a>
            </p> 
        </#if>
        
        <#if editConfiguration.propertySelectFromExisting = true || editConfiguration.propertyOfferCreateNewOption = true>
            <p>
                <input type="submit" id="delete" value="${i18n().delete_button}" role="button "/>
            </p>      
        </#if>
    </#if>
</form>
