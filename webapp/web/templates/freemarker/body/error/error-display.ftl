<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for general system error. -->

<p>
    ${i18n().we_have_an_error} 
    <#if sentEmail>
        ${i18n().error_was_reported} 
    </#if>
</p>  
    
<#if adminErrorData??> <#-- view for site administrators -->
   <#if adminErrorData.errorMessage?has_content>
        <p><strong>${i18n().error_message}:</strong> ${adminErrorData.errorMessage?html}</p>
    </#if>
    <#if adminErrorData.stackTrace?has_content>
        <p>
            <strong>${i18n().stack_trace}</strong> (${i18n().trace_available(siteName!)}): ${adminErrorData.stackTrace?html}
        </p>
                   
        <#if adminErrorData.cause?has_content>
            <p><strong>${i18n().caused_by}:</strong> ${adminErrorData.cause?html}</p>            
        </#if>
    </#if>  

<#elseif ! errorOnHomePage> <#-- view for other users -->
    <p>${i18n().return_to_the} <a href="${urls.home}" title="${i18n().home_page}">${i18n().home_page}</a></p> 
</#if>

