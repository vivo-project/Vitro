<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Potvrda da je kreirana lozinka. -->

<#assign subject = "Vaša ${siteName} lozinka je uspešno kreirana." />

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
            <strong>Lozinka je uspešno kreirana.</strong>
        </p>

        <p>
            Vaša nova lozinka povezana sa nalogom ${userAccount.emailAddress} je kreirana.
        </p>

        <p>
            Hvala Vam.
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Lozinka je uspešno kreirana.

Vaša nova lozinka povezana sa nalogom ${userAccount.emailAddress} je kreirana.

Hvala Vam.
</#assign>

<@email subject=subject html=html text=text />
