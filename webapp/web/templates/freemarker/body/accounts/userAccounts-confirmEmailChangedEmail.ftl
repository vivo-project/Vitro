<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that the user has changed his email account. -->

<#assign subject = "Your ${siteName} email account has been changed." />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p>
            Hi, ${userAccount.firstName} ${userAccount.lastName}
        </p>
        
        <p>
            You recently changed the email address associated with 
            ${userAccount.firstName} ${userAccount.lastName}
        </p>
        
        <p>
            Thank you.
        </p>
    </body>
</html>
</#assign>

<#assign text>
Hi, ${userAccount.firstName} ${userAccount.lastName}

You recently changed the email address associated with 
${userAccount.firstName} ${userAccount.lastName}

Thank you.
</#assign>

<@email subject=subject html=html text=text />