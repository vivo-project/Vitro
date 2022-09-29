<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that a password has been reset. -->

<#assign subject = "Seu ${siteName}, a senha foi alterada." />

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
            <strong>Senha alterada com sucesso.</strong>
        </p>

        <p>
            Sua nova senha associada com ${userAccount.emailAddress} foi alterado.
        </p>

        <p>
            Obrigado.
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Senha alterada com sucesso.

Sua nova senha associada com ${userAccount.emailAddress} foi alterado.

Obrigado.
</#assign>

<@email subject=subject html=html text=text />
