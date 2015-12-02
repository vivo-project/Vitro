<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an account has been created. -->

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
