<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Comment form email response -->

<#-- Only inline styles seem to work in email. Can't get styles for margin to work, though. -->

<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <h3>${subject}</h3>
        <p>
            <strong>From:</strong> ${name}<br />
            <strong>Email address:</strong> ${emailAddress}<br />
            <strong>IP address:</strong> ${ip}<br />
            <#if referrer??>
                <em>Likely viewing page: ${referrer}</em><br />
            </#if>
            <strong>Comments:</strong> ${comments}
    </body>
</html>