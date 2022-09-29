<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an account has been created for an externally-authenticated user. -->

<#assign subject = "Ваша учётная запись для ${siteName} создана." />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p>${userAccount.firstName} ${userAccount.lastName}</p>

        <p><strong>Поздравляем!</strong></p>

        <p>Мы создали Вашу новую учётную запись VIVO, связанную с почтовым адресом ${userAccount.emailAddress}.</p>

        <p>Спасибо!</p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Поздравляем!

Мы создали новую учётную запись VIVO, связанную с адресом
${userAccount.emailAddress}.

Спасибо!
</#assign>

<@email subject=subject html=html text=text />
