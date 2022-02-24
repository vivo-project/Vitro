<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an account has been created. -->

<#assign subject = "Seu $ {siteName} a conta foi criada." />

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
           Criamos a sua nova conta ${siteName}, associado com o email ${userAccount.emailAddress}.
        </p>

        <p>
            Se você não solicitou essa nova conta, você pode simplesmente ignorar este e-mail.
            Este pedido expirará se não for ultilizada nos proximos 30 dias.
        </p>

        <p>
            Clique no link abaixo para criar sua senha para sua nova conta usando o nosso servidor seguro.
        </p>

        <p>
            <a href="${passwordLink}" title="password">${passwordLink}</a>
        </p>

        <p>
            Se o link acima não funcionar, você pode copiar e colar o link diretamente na barra de endereços do seu navegador.
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

Criamos a sua nova conta no ${siteName},
associado com ${userAccount.emailAddress}.

Se você não solicitou essa nova conta, você pode simplesmente ignorar este e-mail.
Este pedido expirará se não for ultilizada nos proximos 30 dias.

Cole o link abaixo na barra de endereços do seu navegador para criar sua senha
para a sua nova conta usando o nosso servidor seguro.

${passwordLink}

Obrigado!
</#assign>

<@email subject=subject html=html text=text />
