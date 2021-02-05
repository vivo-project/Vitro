<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an password has been created. -->

<#assign subject = "Your ${siteName} password has successfully been created." />

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
            <strong>Password successfully created.</strong>
        </p>

        <p>
            Your new password associated with ${userAccount.emailAddress} has been created.
        </p>

        <p>
            Thank you.
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Password successfully created.

Your new password associated with ${userAccount.emailAddress} 
has been created.

Thank you.
</#assign>

<@email subject=subject html=html text=text />
