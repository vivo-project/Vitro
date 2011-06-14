<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation email for user account password reset -->

<#assign subject = "Reset password request" />

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
        
We received a request to reset the password for your account 
(${userAccount.emailAddress}). 
Please follow the instructions below to proceed with your password reset.

If you did not request this new account you can safely ignore this email. 
This request will expire if not acted upon for 30 days.

Paste the link below into your browser's address bar to reset your password 
using our secure server.

${passwordLink}
        
Thank you!
</#assign>

<@email subject=subject html=html text=text />