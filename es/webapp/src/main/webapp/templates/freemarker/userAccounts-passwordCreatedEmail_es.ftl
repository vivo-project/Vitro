<#-- $Este archivo esta distribuido bajo los términos de la licencia en /doc/license.txt$ -->

<#-- Mensaje de confirmación que la contraseña ha sido creada. -->

<#assign subject = "La contraseña en ${siteName} ha sido creado con éxito." />

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
            <strong>Contraseña creado con éxito.</strong>
        </p>

        <p>
            Su nueva contraseña asociada con ${userAccount.emailAddress} se ha creado.
        </p>

        <p>
            Gracias.
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Contraseña creado con éxito.

Su nueva contraseña asociada con ${userAccount.emailAddress} 
se ha creado.

Gracias.
</#assign>

<@email subject=subject html=html text=text />
