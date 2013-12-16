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
<#--If edit submission exists, then retrieve validation errors if they exist-->
<#if editSubmission?has_content && editSubmission.submissionExists = true && editSubmission.validationErrors?has_content>
	<#assign submissionErrors = editSubmission.validationErrors/>
</#if>


<h2>${titleVerb} ${i18n().date_time_value_for} ${editConfiguration.subjectName}</h2>

<#--Display error messages if any-->
<#if submissionErrors?has_content>
    <section id="error-alert" role="alert">
        <img src="${urls.images}/iconAlert.png" width="24" height="24" alt="${i18n().error_alert_icon}" />
        <p>
        <#--below shows examples of both printing out all error messages and checking the error message for a specific field-->
        <#list submissionErrors?keys as errorFieldName>
        	<#if errorFieldName == "startField">
        	    <#if submissionErrors[errorFieldName]?contains("before")>
        	        ${i18n().start_interval_must_precede_end_earlier}
        	    <#else>
        	        ${submissionErrors[errorFieldName]}
        	    </#if>
        	    <br />
        	<#elseif errorFieldName == "endField">
    	        <#if submissionErrors[errorFieldName]?contains("after")>
    	            ${i18n().end_interval_must_follow_start_interval}
    	        <#else>
    	            ${submissionErrors[errorFieldName]}
    	        </#if>
	        </#if>
        </#list>
        </p>
    </section>
</#if>
<form class="customForm" action ="${submitUrl}" class="customForm">
<p></p>
<#--Need to draw edit elements for dates here-->
 <#if htmlForElements?keys?seq_contains("startField")>
	${i18n().start_capitalized}&nbsp; ${htmlForElements["startField"]}
 </#if>
 <br /><br />
 <#if htmlForElements?keys?seq_contains("endField")>
	${i18n().end_capitalized}&nbsp; ${htmlForElements["endField"]}
 </#if>
    <span class="requiredHint"> *${i18n().datetime_year_required}</span>
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