<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Potvrda da je lozinka resetovana. -->

<#assign subject = "Your ${siteName} password changed." />

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
            <strong>Lozinka je uspešno promenjena.</strong>
        </p>

        <p>
            Vaša lozinka povezana sa nalogom ${userAccount.emailAddress} je promenjena.
        </p>

        <p>
            Hvala Vam.
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Lozinka je uspešno promenjena.

Vaša lozinka povezana sa nalogom ${userAccount.emailAddress} je promenjena.

Hvala Vam.
</#assign>

<@email subject=subject html=html text=text />
