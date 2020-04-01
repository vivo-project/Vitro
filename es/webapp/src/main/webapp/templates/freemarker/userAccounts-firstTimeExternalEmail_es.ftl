<#-- $Este archivo esta distribuido bajo los términos de la licencia en /doc/license.txt$ -->

<#-- Mensaje de confirmación de que la cuenta ha sido creada para un usuario externamente autenticado. -->

<#assign subject = "Su cuenta ${siteName} ha sido creada." />

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
            <strong>¡Enhorabuena!</strong>
        </p>

        <p>
            Hemos creado la nueva cuenta VIVO asociado con ${userAccount.emailAddress}.
        </p>

        <p>
            ¡Gracias!
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

¡Enhorabuena!

Hemos creado la nueva cuenta VIVO asociado con
${userAccount.emailAddress}.

¡Gracias!
</#assign>

<@email subject=subject html=html text=text />
