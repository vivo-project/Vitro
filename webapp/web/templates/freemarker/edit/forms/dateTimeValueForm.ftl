<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for adding/editing time values -->

<#if editConfig.object?has_content>
    <#assign editMode = "edit">
<#else>
    <#assign editMode = "add">
</#if>

<#if editMode == "edit">        
        <#assign titleVerb="Edit">        
        <#assign submitButtonText="Edit Date/Time Value">
        <#assign disabledVal="disabled">
<#else>
        <#assign titleVerb="Create">        
        <#assign submitButtonText="Create Date/Time Value">
        <#assign disabledVal=""/>
</#if>

<h2>${editMode} date time value for ${individualName}</h2>

<form class="customForm" action ="${submitUrl}" class="customForm">
    <fieldset class="dateTime" role="group"> 
        <label for="dateTimeField-year">Year</label>
        <input class="text-field" name="dateTimeField-year" id="dateTimeField-year" type="text" value="" size="4" maxlength="4" role="input"/>

        <label for="dateTimeField-month">Month</label>
        <select name="dateTimeField-month" id="dateTimeField-month" role="select">
            <option value=""  role="option" <#if dateTimeField-month="">selected</#if>month</option>
            <#list dateTimeField-months as dateTimeField-month>
            <option value="dateTimeField-month" <#if dateTimeField-month = dateTimeField-month.uri>selected</#if> >${dateTimeField-month.label}</option>
            </#list>
        </select>

        <label for="dateTimeField-day">Day</label>
        <select name="dateTimeField-day" id="dateTimeField-day" role="select">
            <option value="" role="option"><#if dateTimeField-day="">selected</#if>day</option>
            <#list dateTimeField-day as dateTimeField-day>
            <option value="dateTimeField-day" role="option" <#if dateTimeField-day=dateTimeField-day.uri>selected</#if> >${dateTimeField-day.label}</option>
            </#list>
        </select>
    </fieldset>
    <fieldset class="dateTime" role="group">  
        <label for="dateTimeField-hour">Hour</label>
        <select name="dateTimeField-hour" id="dateTimeField-hour" role="select">
            <option value="" role="option"><#if dateTimeField-hour="">selected</#if>hour</option>
            <#list dateTimeField-hours as dateTimeField-hour>
            <option value="dateTimeField-hour" role="option" <#if dateTimeField-hour=dateTimeField-hour.uri>selected</#if> >${dateTimeField-hour.label}</option>
            </#list>
        </select>

        <label for="dateTimeField-minute">Minutes</label>
        <select name="dateTimeField-hour" id="dateTimeField-hour" role="select">
            <option value="" role="option"><#if dateTimeField-minute="">selected</#if>minutes</option>
            <#list dateTimeField-minutes as dateTimeField-minute>
            <option value="dateTimeField-minute" role="option" <#if dateTimeField-minute=dateTimeField-minute.uri>selected</#if> >${dateTimeField-minute.label}</option>
            </#list>
        </select>    
        
        <label for="dateTimeField-second">Seconds</label>
        <select name="dateTimeField-second" id="dateTimeField-second" role="select">
            <option value="" role="option"><#if dateTimeField-second="">selected</#if>seconds</option>
            <#list dateTimeField-seconds as dateTimeField-second>
            <option value="dateTimeField-second" role="option" <#if dateTimeField-second=dateTimeField-second.uri>selected</#if> >${dateTimeField-second.label}</option>
            </#list>
        </select>
    </fieldset>

    <p class="submit">
        <input type="hidden" name="editKey" value="${keyValue}" />
        <input type="submit" id="submit" value="${editConfiguration.submitLabel}" role="button" />
    
        <span class="or"> or </span>
    
        <a class="Cancel" href="${editConfiguration.cancelUrl}" title="Cancel">Cancel</a>
    </p>
</form>

<#assign acUrl="/autocomplete?tokenize=true&stem=true" >

<script type="text/javascript">
var customFormData  = {
    acUrl: '${acUrl?url}',
    editMode: '${editMode}',
    submitButtonTextType: 'compound',
    defaultTypeName: 'organization'
};
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />',
                  '<link rel="stylesheet" href="${urls.base}/edit/forms/css/personHasEducationalTraining.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/utils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/edit/forms/js/customFormWithAutocomplete.js"></script>')}