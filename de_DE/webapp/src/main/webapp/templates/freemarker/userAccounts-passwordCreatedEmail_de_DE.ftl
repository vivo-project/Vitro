<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an password has been created. -->

<#assign subject = "Ihr Passwort für ${siteName} wurde erfolgreich erstellt." />

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
            <strong>Passwort erfolgreich erstellt.</strong>
        </p>

        <p>
          Ihr Passwort für den Account ${userAccount.emailAddress} wurde erstellt.
        </p>

        <p>
            Vielen Dank.
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Passwort erfolgreich erstellt.

Ihr Passwort für den Account ${userAccount.emailAddress}
wurde erstellt.

Vielen Dank.
</#assign>

<@email subject=subject html=html text=text />
