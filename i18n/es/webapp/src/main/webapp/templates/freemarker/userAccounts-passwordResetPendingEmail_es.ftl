<#-- $Este archivo esta distribuido bajo los términos de la licencia en /doc/license.txt$ -->

<#-- Mensaje de confirmacion de cambio de contraseña para una cuenta de usuario -->

<#assign subject = "Solicitud para reestablecer contraseña en ${siteName}" />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p>
            Estimado ${userAccount.firstName} ${userAccount.lastName}:
        </p>
        
        <p>
            Hemos recibido una solicitud para restablecer la contraseña de tu cuenta ${siteName} 
            (${userAccount.emailAddress}).
        </p>
        
        <p>
            Por favor, sigue las siguientes instrucciones para proceder con el restablecimiento de tu contraseña.
        </p>
        
        <p>
            Si no has solicitado esta nueva cuenta puedes ignorar este mensaje.
            Esta solicitud caducará si no se activa en un plazo de 30 días.
        </p>
        
        <p>
            Haga clic en el enlace de abajo o pegalo en la barra de direcciones de tu navegador para 
            restablecer tu contraseña usando nuestro servidor seguro.
        </p>
        
        <p><a href="${passwordLink}" title="password">${passwordLink}</a> </p>
        
        <p>¡Gracias!</p>
    </body>
</html>
</#assign>

<#assign text>
Estimado ${userAccount.firstName} ${userAccount.lastName}:
        
Hemos recibido una solicitud para restablecer la contraseña de tu cuenta ${siteName}
(${userAccount.emailAddress}).

Por favor, sigue las siguientes instrucciones para proceder con el restablecimiento de tu contraseña.

Si no has solicitado esta nueva cuenta puedes ignorar este mensaje.
Esta solicitud caducará si no se activa en un plazo de 30 días.

Pega el siguiente enlace en la barra de direcciones de tu navegador para 
restablecer tu contraseña usando nuestro servidor seguro.

${passwordLink}
        
¡Gracias!
</#assign>

<@email subject=subject html=html text=text />
