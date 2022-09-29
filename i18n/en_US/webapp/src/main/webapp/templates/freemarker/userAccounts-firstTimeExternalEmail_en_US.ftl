<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an account has been created for an externally-authenticated user. -->

<#assign subject = "Your ${siteName} account has been created." />

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
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Congratulations!

We have created your new VIVO account associated with 
${userAccount.emailAddress}.

Thanks!
</#assign>

<@email subject=subject html=html text=text />
