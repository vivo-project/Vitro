<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that a password has been reset. -->

<#assign subject = "El ${siteName} contraseña cambiada." />

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
            <strong>Contraseña cambiada con éxito.</strong>
        </p>

        <p>
            Su nueva contraseña asociada con ${userAccount.emailAddress} ha sido cambiado.
        </p>

        <p>
            Gracias.
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Contraseña cambiada con éxito.

Su nueva contraseña asociada con ${userAccount.emailAddress} 
ha sido cambiado.

Gracias.
</#assign>

<@email subject=subject html=html text=text />
