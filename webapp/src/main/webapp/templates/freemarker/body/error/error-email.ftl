<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for email message sent to site administrator when an error occurs on the site. -->

<#assign subject = "${i18n().error_occurred(siteName!)}" />

<#assign datetime = datetime?string("yyyy-MM-dd HH:mm:ss zzz")>

<#assign html>
<html>
    <head>
        <title>${subject!}</title>
    </head>
    <body>
        <p>
            ${i18n().error_occurred_at(siteName!,datetime!)}
        </p>
        
        <p>
            <strong>${i18n().requested_url}:</strong> ${requestedUrl!}
        </p>
        
        <p>
        <#if errorMessage?has_content>
            <strong>${i18n().error_message}:</strong> ${errorMessage!}
        </#if>
        </p>
        
        <p>
            <strong>${i18n().stack_trace}</strong> (${i18n().trace_available(siteName!)}): 
            <pre>${stackTrace!}</pre>
        </p>
        
        <#if cause?has_content>
            <p><strong>${i18n().caused_by}:</strong> 
                <pre>${cause!}</pre>
            </p>
        </#if>
        
    </body>
</html>
</#assign>

<#assign text>
${i18n().error_occurred_at(siteName!,datetime!)}

${i18n().requested_url}: ${requestedUrl!}

<#if errorMessage?has_content>
    ${i18n().error_message}: ${errorMessage!}
</#if>

${i18n().stack_trace} (${i18n().trace_available(siteName!)}): 
${stackTrace!}

<#if cause?has_content>
${i18n().caused_by}: 
${cause!}
</#if>       
</#assign>

<@email subject=subject html=html text=text />