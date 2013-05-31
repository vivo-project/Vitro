<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Contact form email response -->

<#-- Only inline styles seem to work in email. Can't get styles for margin to work, though. -->

<html>
    <head>
        <title>${subject}</title>
    </head>
    
    <body>
        <h3>${subject}</h3>
        
        <p><strong>${i18n().from_capitalized}:</strong> ${name}</p>

        <p><strong>${i18n().email_address}:</strong> ${emailAddress}</p>
            
        <p>
            <strong>${i18n().ip_address}:</strong> ${ip}<br />
            <#if referrer??>
                <em>${i18n().viewing_page}: ${referrer}</em>
            </#if>
        </p>  
        
        <p><strong>${i18n().comments}:</strong> ${comments}</p>
    </body>
</html>