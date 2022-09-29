<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that the user has changed his email account. -->

<#assign subject = "Запись Вашего адреса электронной почты на ${siteName} была изменена." />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p>Здравствуйте, ${userAccount.firstName} ${userAccount.lastName}</p>

        <p>Вы недавно изменили адрес электронной почты, связанный с ${userAccount.firstName} ${userAccount.lastName}</p>

        <p>Спасибо.</p>
    </body>
</html>
</#assign>

<#assign text>
Здравствуйте, ${userAccount.firstName} ${userAccount.lastName}

Вы недавно изменили адрес электронной почты, связанный с 
${userAccount.firstName} ${userAccount.lastName}

Спасибо.
</#assign>

<@email subject=subject html=html text=text />
