<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#if submissionErrors?has_content >
    <section id="error-alert" role="alert">
        <img src="${urls.images}/iconAlert.png" width="24" height="24" alt="${i18n().error_alert_icon}" />
        <p>
        <#list submissionErrors?keys as errorFieldName>
        	<#if  errorFieldName == "label">
        	    ${i18n().enter_value_name_field}
    	    </#if>
    	    <br />
    	</#list>
        </p>
    </section>
</#if>