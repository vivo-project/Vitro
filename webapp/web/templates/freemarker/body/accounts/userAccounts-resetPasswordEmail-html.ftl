<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Notification that your password has been reset. -->

<html>
    <head>
        <title>${subjectLine}</title>
    </head>
    <body>
        <p>
            ${userAccount.firstName} ${userAccount.lastName}
        </p>
        
        <p>
            We received a request to reset the password for your account (${userAccount.emailAddress}). 
            Please follow the instructions below to proceed with your password reset.
        </p>
        
        <p>
            If you did not request this new account you can safely ignore this email. 
            This request will expire if not acted upon for 30 days.
        </p>
        
        <p>
            Click the link below to reset your password using our secure server.
        </p>
        
        <p>
            <a href="${passwordLink}">${passwordLink}</a>
        </p>
        
        <p>
            If the link above doesn't work, you can copy and paste the link directly into your browser's address bar.
        </p>
        
        <p>
            Thank you!
        </p>
    </body>
</html>