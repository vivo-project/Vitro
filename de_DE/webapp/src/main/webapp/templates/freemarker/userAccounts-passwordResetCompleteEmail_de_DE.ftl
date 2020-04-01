<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that a password has been reset. -->

<#assign subject = "Ihr Passwort für ${siteName} wurde verändert." />

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
            <strong>Ihr Passwort wurde geändert.</strong>
        </p>

        <p>
            Das mit dem Account ${userAccount.emailAddress} verknüpfte Passwort wurde geändert.
        </p>

        <p>
            Vielen Dank.
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Ihr Passwort wurde geändert.

Das mit dem Account ${userAccount.emailAddress} verknüpfte Passwort wurde geändert.

Vielen Dank.
</#assign>

<@email subject=subject html=html text=text />
