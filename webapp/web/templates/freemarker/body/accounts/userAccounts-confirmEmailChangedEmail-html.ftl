<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that the user has changed his email account. -->

<html>
    <head>
        <title>${subjectLine}</title>
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