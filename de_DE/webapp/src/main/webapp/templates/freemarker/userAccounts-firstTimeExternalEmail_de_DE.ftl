<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an account has been created for an externally-authenticated user. -->

<#assign subject = "Ihr Account für ${siteName} wurde angelegt." />

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
            <strong>Herzlichen Glückwunsch!</strong>
        </p>

        <p>
            Wir haben einen Account für Sie angelegt. Ihr Account ist mit der E-Mail-Adresse ${userAccount.emailAddress} verknüpft.
        </p>

        <p>
            Vielen Dank!
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Herzlichen Glückwunsch!

Wir haben einen Account für Sie angelegt. Ihr Account ist
mit der E-Mail-Adresse ${userAccount.emailAddress} verknüpft.

Vielen Dank!
</#assign>

<@email subject=subject html=html text=text />
