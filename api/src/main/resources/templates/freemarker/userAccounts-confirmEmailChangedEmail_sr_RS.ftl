<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Potvrda da je korisnik promenio email adresu sa kojom je povezan nalog. -->

<#assign subject = "Your ${siteName} email account has been changed." />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p>
            Pozdrav, ${userAccount.firstName} ${userAccount.lastName}
        </p>

        <p>
            Nedavno ste promenili email adresu povezanu sa nalogom za korisnika
            ${userAccount.firstName} ${userAccount.lastName}
        </p>

        <p>
           Hvala Vam.
        </p>
    </body>
</html>
</#assign>

<#assign text>
Pozdrav, ${userAccount.firstName} ${userAccount.lastName}

Nedavno ste promenili email adresu povezanu sa nalogom za korisnika
${userAccount.firstName} ${userAccount.lastName}

Hvala Vam.
</#assign>

<@email subject=subject html=html text=text />
