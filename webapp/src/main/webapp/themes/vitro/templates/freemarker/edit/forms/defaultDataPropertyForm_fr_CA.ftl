<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#--If edit submission exists, then retrieve validation errors if they exist-->
<#if editSubmission?has_content && editSubmission.submissionExists = true && editSubmission.validationErrors?has_content>
    <#assign submissionErrors = editSubmission.validationErrors/>
</#if>


<h2>${editConfiguration.formTitle}</h2>

<#--Display error messages if any-->
<#if submissionErrors?has_content>
    <section id="error-alert" role="alert">
        <img src="${urls.images}/iconAlert.png" width="24" height="24" alt="${i18n().error_alert_icon}" />
        <p>

        <#list submissionErrors?keys as errorFieldName>
            ${submissionErrors[errorFieldName]}
        </#list>

        </p>
    </section>
</#if>

<#assign literalValues = "${editConfiguration.dataLiteralValuesAsString}" />
<#assign datatype = editConfiguration.dataPredicateProperty.rangeDatatypeURI!"none" />

<form class="editForm" action = "${submitUrl}" method="post">
    <input type="hidden" name="editKey" id="editKey" value="${editKey}" role="input" />
    <#if editConfiguration.dataPredicatePublicDescription?has_content>
       <label for="${editConfiguration.dataLiteral}"><p class="propEntryHelpText">${editConfiguration.dataPredicatePublicDescription}</p></label>
    </#if>

	<#if "HTML" == editConfiguration.dataPredicateProperty.editing!>
		<textarea rows="2" id="literal" name="literal" class="useTinyMce" role="textarea">${literalValues}</textarea>

	<#elseif datatype = "http://www.w3.org/2001/XMLSchema#integer" || datatype = "http://www.w3.org/2001/XMLSchema#int">
    	<input type="text" id="literal" name="literal" value="${literalValues}" placeholder="123456" />

    <#elseif datatype = "http://www.w3.org/2001/XMLSchema#float">
        <input type="text" id="literal" name="literal" value="${literalValues}" placeholder="12.345" />

    <#elseif datatype = "http://www.w3.org/2001/XMLSchema#boolean">
        <select id="literal" name="literal">
            <#if literalValues = "true">
                <option value="true" selected="true">true</option>
                <option value="false">false</option>
            <#else>
                <option value="true">true</option>
                <option value="false" selected="true">false</option>
            </#if>
        </select>

    <#elseif datatype = "http://www.w3.org/2001/XMLSchema#anyURI">
        <input type="text" id="literal" name="literal" value="${literalValues}" placeholder="http://..." />

    <#elseif datatype = "http://www.w3.org/2001/XMLSchema#dateTime" ||
        		datatype = "http://www.w3.org/2001/XMLSchema#date" ||
        		datatype = "http://www.w3.org/2001/XMLSchema#time" ||
        		datatype = "http://www.w3.org/2001/XMLSchema#gYearMonth" ||
        		datatype = "http://www.w3.org/2001/XMLSchema#gYear"	||
        		datatype = "http://www.w3.org/2001/XMLSchema#gMonth" >
        <#include "dateTimeEntryForm.ftl">

    <#else>
        <input type="text" size="70" id="literal" name="literal" value="${literalValues}" />

    </#if>

    <br />
    <#--The submit label should be set within the template itself, right now
    the default label for default data/object property editing is returned from Edit Configuration Template Model,
    but that method may not return the correct result for other custom forms-->
    <input type="submit" id="submit" value="${editConfiguration.submitLabel}" role="button"/>
    <span class="or"> or </span>
    <a title="${i18n().cancel_title}" href="${cancelUrl}">${i18n().cancel_link}</a>

</form>

<#if editConfiguration.includeDeletionForm = true>
<#include "defaultDeletePropertyForm.ftl">
</#if>

<script type="text/javascript">
	var datatype = "${datatype!}";

	var i18nStrings = {
    	four_digit_year: "${i18n().four_digit_year}",
    	year_numeric: "${i18n().year_numeric}",
    	year_month_day: "${i18n().year_month_day}",
    	minimum_ymd: "${i18n().minimum_ymd}",
    	minimum_hour: "${i18n().minimum_hour}",
    	year_month: "${i18n().year_month}",
    	decimal_only: "${i18n().decimal_only}",
    	whole_number: "${i18n().whole_number}"
	};
</script>

<#include "defaultFormScripts.ftl">

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/utils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>')}

