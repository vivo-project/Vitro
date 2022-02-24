<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an account has been created for an externally-authenticated user. -->

<#assign subject = "Seu ${siteName}, a conta foi criada." />

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
            <strong>Parabéns!</strong>
        </p>

        <p>
           Nós criamos sua conta nova VIVO associado com ${userAccount.emailAddress}.
        </p>

        <p>
            Obrigado!
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Parabéns!

Nós criamos sua conta nova VIVO associado com
${userAccount.emailAddress}.

Obrigado!
</#assign>

<@email subject=subject html=html text=text />
