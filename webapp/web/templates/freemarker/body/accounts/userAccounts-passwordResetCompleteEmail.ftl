<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that a password has been reset. -->

<#assign subject = "Your ${siteName} password changed." />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p>
            ${userAccount.firstName} ${userAccount.lastName}
        </p>
        
        <p>
            <strong>Password successfully changed.</strong>
        </p>
        
        <p>
            Your new password associated with ${userAccount.emailAddress} has been changed.
        </p>
        
        <p>
            Thank you.
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Password successfully changed.

Your new password associated with ${userAccount.emailAddress} 
has been changed.

Thank you.
</#assign>

<@email subject=subject html=html text=text />