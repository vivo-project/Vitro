<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that a password has been reset. -->

<#assign subject = "Ваш пароль для сайта ${siteName} изменён." />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>

    <body>
        <p> ${userAccount.firstName} ${userAccount.lastName}</p>

        <p><strong>Пароль успешно изменён.</strong></p>

        <p>Ваш пароль, связанный с адресом ${userAccount.emailAddress} изменён.</p>

        <p>Спасибо.</p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Пароль успешно изменён.

Ваш пароль, связанный с адресом ${userAccount.emailAddress} 
изменён.

Спасибо.
</#assign>

<@email subject=subject html=html text=text />
