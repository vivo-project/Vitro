<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an password has been created. -->

<#assign subject = "Ваш пароль для сайта ${siteName} успешно создан." />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p> ${userAccount.firstName} ${userAccount.lastName}</p>

        <p> <strong>Пароль успешно создан.</strong></p>

        <p>Ваш пароль, связанный с адресом ${userAccount.emailAddress} создан.</p>

        <p>Спасибо.</p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Пароль успешно создан.

Ваш пароль, связанный с адресом ${userAccount.emailAddress}
создан.


Спасибо.
</#assign>

<@email subject=subject html=html text=text />
