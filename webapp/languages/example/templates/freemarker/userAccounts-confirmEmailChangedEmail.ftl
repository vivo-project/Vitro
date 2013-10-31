<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that the user has changed his email account. -->

<#assign subject = "Su cuenta de correo electrónico ${siteName} ha cambiado." />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p>
            Hola, ${userAccount.firstName} ${userAccount.lastName}
        </p>

        <p>
            Ha cambiado recientemente la dirección de correo electrónico asociada a
            ${userAccount.firstName} ${userAccount.lastName}
        </p>

        <p>
            Gracias.
        </p>
    </body>
</html>
</#assign>

<#assign text>
Hola, ${userAccount.firstName} ${userAccount.lastName}

Ha cambiado recientemente la dirección de correo electrónico asociada a
${userAccount.firstName} ${userAccount.lastName}

Gracias.
</#assign>

<@email subject=subject html=html text=text />
