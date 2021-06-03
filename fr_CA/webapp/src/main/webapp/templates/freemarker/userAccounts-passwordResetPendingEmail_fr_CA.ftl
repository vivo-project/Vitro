<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation email for user account password reset -->

<#assign subject = "${siteName} reset password request" />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p>
            Dear ${userAccount.firstName} ${userAccount.lastName}:
        </p>
        
        <p>
            We have received a request to reset the password for your ${siteName} account (${userAccount.emailAddress}). 
        </p>
        
        <p>
            Please follow the instructions below to proceed with your password reset.
        </p>
        
        <p>
            If you did not request this new account you can safely ignore this email. 
            This request will expire if not acted upon within 30 days.
        </p>
        
        <p>
            Click on the link below or paste it into your browser's address bar to reset your password 
            using our secure server.
        </p>
        
        <p><a href="${passwordLink}" title="password">${passwordLink}</a> </p>
        
        <p>Thank you!</p>
    </body>
</html>
</#assign>

<#assign text>
Dear ${userAccount.firstName} ${userAccount.lastName}:
        
We have received a request to reset the password for your ${siteName} account 
(${userAccount.emailAddress}). 

Please follow the instructions below to proceed with your password reset.

If you did not request this new account you can safely ignore this email. 
This request will expire if not acted upon within 30 days.

Paste the link below into your browser's address bar to reset your password 
using our secure server.

${passwordLink}
        
Thank you!
</#assign>

<@email subject=subject html=html text=text />
