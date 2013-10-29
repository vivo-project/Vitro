<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an password has been created. -->

<#assign subject = "El ${siteName} contraseña ha sido creado con éxito." />

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
