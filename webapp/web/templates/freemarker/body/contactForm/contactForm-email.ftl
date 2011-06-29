<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Contact form email response -->

<#-- Only inline styles seem to work in email. Can't get styles for margin to work, though. -->

<html>
    <head>
        <title>${subject}</title>
    </head>
    
    <body>
        <h3>${subject}</h3>
        
        <p><strong>From:</strong> ${name}</p>

        <p><strong>Email address:</strong> ${emailAddress}</p>
            
        <p>
            <strong>IP address:</strong> ${ip}<br />
            <#if referrer??>
                <em>Likely viewing page: ${referrer}</em>
            </#if>
        </p>  
        
        <p><strong>Comments:</strong> ${comments}</p>
    </body>
</html>