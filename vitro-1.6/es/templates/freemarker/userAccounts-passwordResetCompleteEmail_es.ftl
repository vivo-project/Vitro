<#-- $Este archivo esta distrubuido bajo los términos de la licencia en /doc/license.txt$ -->

<#-- Mensaje de confirmación que la contraseña ha sido reestablecida. -->

<#assign subject = "La contraseña ha sido cambiada en ${siteName} ." />

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
            Su nueva contraseña asociada con ${userAccount.emailAddress} ha sido cambiada.
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
ha sido cambiada.

Gracias.
</#assign>

<@email subject=subject html=html text=text />
