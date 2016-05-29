<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for adding/editing time values -->

<#--Retrieve certain edit configuration information-->
<#assign editMode = editConfiguration.pageData.editMode />
<#assign htmlForElements = editConfiguration.pageData.htmlForElements />

<#if editMode == "edit">        
        <#assign titleVerb="${i18n().edit_capitalized}">        
        <#assign submitButtonText="${i18n().edit_date_time_value}">
        <#assign disabledVal="disabled">
<#else>
        <#assign titleVerb="${i18n().create_capitalized}">        
        <#assign submitButtonText="${i18n().create_date_time_value}">
        <#assign disabledVal=""/>
</#if>

<h2>${titleVerb} ${i18n().date_time_value_for} ${editConfiguration.subjectName}</h2>

<form class="customForm" action ="${submitUrl}" class="customForm">
<#--Need to draw edit elements for dates here-->
 <#if htmlForElements?keys?seq_contains("dateTimeField")>
		${htmlForElements["dateTimeField"]}
 </#if>

    <p class="submit">
        <input type="hidden" name="editKey" value="${editKey}" />
        <input type="submit" id="submit" value="${submitButtonText}" role="button" />
    
        <span class="or"> ${i18n().or} </span>
    
        <a class="cancel" href="${editConfiguration.cancelUrl}" title="${i18n().cancel_title}">${i18n().cancel_link}</a>
    </p>
</form>
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/utils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>')}