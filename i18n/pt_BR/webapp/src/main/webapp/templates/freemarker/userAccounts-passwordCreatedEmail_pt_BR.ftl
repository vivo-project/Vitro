<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an password has been created. -->

<#assign subject = "Seu ${siteName}, a senha foi criada com sucesso." />

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
            <strong>Senha criado com sucesso.</strong>
        </p>

        <p>
            Sua nova senha foi criada e associada ${userAccount.emailAddress} .
        </p>

        <p>
            Obrigado.
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Senha criado com sucesso.

Sua nova senha foi criada e associada ${userAccount.emailAddress} .

Obrigado.
</#assign>

<@email subject=subject html=html text=text />
