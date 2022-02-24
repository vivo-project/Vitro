<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that the user has changed his email account. -->

<#assign subject = "Seu ${siteName}, a conta de e-mail foi alterada." />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p>
            Olá, ${userAccount.firstName} ${userAccount.lastName}
        </p>

        <p>
           Você recentemente mudou o endereço de email associado
            ${userAccount.firstName} ${userAccount.lastName}
        </p>

        <p>
            Obrigado.
        </p>
    </body>
</html>
</#assign>

<#assign text>
Olá, ${userAccount.firstName} ${userAccount.lastName}

Você recentemente mudou o endereço de email associado
${userAccount.firstName} ${userAccount.lastName}

Obrigado.
</#assign>

<@email subject=subject html=html text=text />
