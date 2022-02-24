<#-- $Este archivo esta distribuido bajo los términos de la licencia en /doc/license.txt$ -->

<#-- Mensaje de confirmación de que una cuenta ha sido creada. -->

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
            Si no has solicitado esta nueva cuenta puedes ignorar este mensaje.
            Esta solicitud caducará si no se activa durante los próximos 30 días.
        </p>

        <p>
            Haga clic en el enlace de abajo para crear la contraseña de tu cuenta usando nuestro servidor seguro.
        </p>

        <p>
            <a href="${passwordLink}" title="password">${passwordLink}</a>
        </p>

        <p>
            Si el enlace no funciona, puedes copiar y pegar el enlace directamente en la barra de direcciones de tu navegador.
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

Si no has solicitado esta nueva cuenta puedes ignorar este mensaje.
Esta solicitud caducará si no se activa durante los próximos 30 días.

Pega el siguiente enlace en la barra de direcciones de tu navegador para 
crear la contraseña de tu nueva cuenta usando nuestro servidor seguro.

${passwordLink}

¡Gracias!
</#assign>

<@email subject=subject html=html text=text />
