<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation email for user account password reset -->

<#assign subject = "${siteName} restablecer solicitud de contraseña" />

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
            Hemos recibido una solicitud para restablecer la contraseña de su cuenta ${siteName} 
            (${userAccount.emailAddress}).
        </p>
        
        <p>
            Por favor, siga las siguientes instrucciones para proceder con su restablecimiento de contraseña.
        </p>
        
        <p>
            Si no has solicitado esta nueva cuenta puede ignorar este mensaje.
            Esta solicitud caducará si no se hubiere pronunciado en un plazo de 30 días.
        </p>
        
        <p>
            Haga clic en el enlace de abajo o pegarlo en la barra de direcciones de su navegador para 
            restablecer su contraseña usando nuestro servidor seguro.
        </p>
        
        <p>${passwordLink}</p>
        
        <p>¡Gracias!</p>
    </body>
</html>
</#assign>

<#assign text>
Estimado ${userAccount.firstName} ${userAccount.lastName}:
        
Hemos recibido una solicitud para restablecer la contraseña de su cuenta ${siteName}
(${userAccount.emailAddress}).

Por favor, siga las siguientes instrucciones para proceder con su restablecimiento de contraseña.

Si no has solicitado esta nueva cuenta puede ignorar este mensaje.
Esta solicitud caducará si no se hubiere pronunciado en un plazo de 30 días.

Pega el siguiente enlace en la barra de direcciones de su navegador para 
restablecer su contraseña usando nuestro servidor seguro.

${passwordLink}
        
¡Gracias!
</#assign>

<@email subject=subject html=html text=text />
