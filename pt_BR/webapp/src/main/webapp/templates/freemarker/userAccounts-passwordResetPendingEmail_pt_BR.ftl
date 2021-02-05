<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation email for user account password reset -->

<#assign subject = "${siteName} solicitação de redefinição de senha" />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p>
            Caro ${userAccount.firstName} ${userAccount.lastName}:
        </p>
        
        <p>
            Recebemos um pedido para redefinir a senha para o seu ${siteName} conta
            (${userAccount.emailAddress}). 
        </p>
        
        <p>
            Por favor, siga as instruções abaixo para prosseguir com a sua redefinição de senha.
        </p>
        
        <p>
            Se você não solicitou essa nova conta, você pode simplesmente ignorar este e-mail.
            Este pedido expirará se não for executado no prazo de 30 dias.
        </p>
        
        <p>
            Clique no link abaixo ou cole-o na barra de endereço do seu navegador para 
            redefinir sua senha usando o nosso servidor seguro.
        </p>
        
        <p>${passwordLink}</p>
        
        <p>Obrigado!</p>
    </body>
</html>
</#assign>

<#assign text>
Caro ${userAccount.firstName} ${userAccount.lastName}:
        
Recebemos um pedido para redefinir a senha para o seu ${siteName} conta
(${userAccount.emailAddress}). 

Please follow the instructions below to proceed with your password reset.

Se você não solicitou essa nova conta, você pode simplesmente ignorar este e-mail.
Este pedido expirará se não for executado no prazo de 30 dias.

Clique no link abaixo ou cole-o na barra de endereço do seu navegador para 
redefinir sua senha usando o nosso servidor seguro.

${passwordLink}
        
Obrigado!
</#assign>

<@email subject=subject html=html text=text />
