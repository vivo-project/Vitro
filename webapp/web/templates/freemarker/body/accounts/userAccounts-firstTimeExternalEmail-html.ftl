<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an account has been created for an externally-authenticated user. -->

<html>
    <head>
        <title>${subjectLine}</title>
    </head>
    <body>
        <p>
            ${userAccount.firstName} ${userAccount.lastName}
        </p>
        
        <p>
            <strong>Congratulations!</strong>
        </p>
        
        <p>
            We have created your new VIVO account associated with ${userAccount.emailAddress}.
        </p>
        
        <p>
            Thanks!
        </p>
    </body>
</html>