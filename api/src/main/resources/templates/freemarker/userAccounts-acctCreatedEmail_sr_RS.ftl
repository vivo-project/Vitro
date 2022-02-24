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
            Kreirali smo vaš novi nalog za ${siteName}, povezan sa ${userAccount.emailAddress} email adresom.
        </p>

        <p>
            Ako niste zatražili kreiranje novog naloga možete ignorisati ovaj email. 
            Zahtev će isteći za 30 dana.
        </p>

        <p>
            Kliknite link ispod kako bi ste kreirali lozinku za Vaš novi nalog koristeći naše zaštićene servere.
        </p>

        <p>
            <a href="${passwordLink}" title="password">${passwordLink}</a>
        </p>

        <p>
            Ako link iznad ne funkcioniše, možete ga kopirati i nalepiti direktno unutar Vašeg pretraživača.
        </p>

        <p>
            Hvala Vam!
        </p>
    </body>
</html>
</#assign>

<#assign text>
${userAccount.firstName} ${userAccount.lastName}

Česttitamo!

Kreirali smo vaš novi nalog za ${siteName}, 
povezan sa ${userAccount.emailAddress} email adresom.

Ako niste zatražili kreiranje novog naloga možete ignorisati ovaj email.
Zahtev će isteći za 30 dana.

Nalepite link koji se nalazi ispred u navigacioni bar Vašeg pretraživača kako bi ste 
kreirali lozinku za Vaš nov nalog koristeći naše zaštićene servere.

${passwordLink}

Hvala Vam!
</#assign>

<@email subject=subject html=html text=text />
