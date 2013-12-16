<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an account has been created. -->

<#assign subject = "Su cuenta ${siteName} ha sido creado." />
 
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
            <strong>Enhorabuena!</strong>
        </p>

        <p>
            Hemos creado la nueva cuenta en ${siteName}, asociada con ${userAccount.emailAddress}.
        </p>

        <p>
            Si no has solicitado esta nueva cuenta puede ignorar este mensaje.
            Esta solicitud caducará si no se hubiere pronunciado sobre durante 30 días.
        </p>

        <p>
            Haga clic en el enlace de abajo para crear la contraseña de su cuenta usando nuestro servidor seguro.
        </p>

        <p>
            <a href="${passwordLink}" title="password">${passwordLink}</a>
        </p>

        <p>
            Si el enlace no funciona, puedes copiar y pegar el enlace directamente en la barra de direcciones de su navegador.
        </p>

        <p>
            ¡Gracias!
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Enhorabuena!

Hemos creado la nueva cuenta en ${siteName}, 
asociada con ${userAccount.emailAddress}.

Si no has solicitado esta nueva cuenta puede ignorar este mensaje.
Esta solicitud caducará si no se hubiere pronunciado sobre durante 30 días.

Pega el siguiente enlace en la barra de direcciones de su navegador para 
crear su contraseña para su nueva cuenta usando nuestro servidor seguro.

${passwordLink}

¡Gracias!
</#assign>

<@email subject=subject html=html text=text />
