<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that a password has been reset. -->

<html>
    <head>
        <title>${subjectLine}</title>
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