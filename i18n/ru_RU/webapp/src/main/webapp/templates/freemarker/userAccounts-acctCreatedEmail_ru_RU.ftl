<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an account has been created. -->

<#assign subject = "Ваша учётная запись для ${siteName} создана." />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p> ${userAccount.firstName} ${userAccount.lastName}</p>

        <p><strong>Поздравляем!</strong></p>

        <p>Мы создали новую учётную запись на сайте ${siteName}, связанную с адресом ${userAccount.emailAddress}.</p>

        <p>Если Вы не запрашивали создания учётной записи, просто проигнорируйте это электронное письмо. Если не предпринимать никаких действий через 30 дней срок действия этого запроса закончится.</p>

        <p>Нажмите на ссылку ниже, чтобы создать пароль для новой учетной записи с помощью нашего защищенного сервера.</p>

        <p> <a href="${passwordLink}" title="password">${passwordLink}</a></p>

        <p>Если приведенная выше ссылка не работает, вы можете скопировать и вставить ссылку непосредственно в адресную строку вашего браузера.</p>

        <p>Спасибо!</p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Поздравляем!

Мы создали новую учётную запись на сайте ${siteName},
связанную с адресом ${userAccount.emailAddress}.

Если Вы не запрашивали создания учётной записи, просто проигнорируйте это электронное письмо.
Если не предпринимать никаких действий через 30 дней срок действия этого запроса закончится.

Чтобы создать пароль для Вашей новой учетной записи при помощи нашего защищенного сервера,
скопируйте и вставьте приведённую ниже ссылку в адресную строку вашего браузера.

${passwordLink}

Спасибо!
</#assign>

<@email subject=subject html=html text=text />
