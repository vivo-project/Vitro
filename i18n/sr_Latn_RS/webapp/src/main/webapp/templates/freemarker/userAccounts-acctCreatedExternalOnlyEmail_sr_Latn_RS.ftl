<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Potvrda da je nalog kreiran. -->

<#assign subject = "Vaš ${siteName} nalog je kreiran." />

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
            <strong>Čestitamo!</strong>
        </p>

        <p>
            Kreirali smo novi VIVO nalog povezan sa ${userAccount.emailAddress} email adresom.
        </p>

        <p>
            Hvala!
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Čestitamo!

Kreirali smo novi VIVO nalog povezan sa ${userAccount.emailAddress} email adresom.

Hvala!
</#assign>

<@email subject=subject html=html text=text />
