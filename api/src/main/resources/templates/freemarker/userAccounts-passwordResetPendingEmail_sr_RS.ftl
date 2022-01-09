<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Potvrdni mejl za resetovanje korisničke lozinke  -->

<#assign subject = "${siteName} reset password request" />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p>
            Poštovani:
        </p>
        
        <p>
            Dobili smo zahtev za resetovanje šifre za Vaš ${siteName} nalog (${userAccount.emailAddress}). 
        </p>
        
        <p>
            Molimo Vas da pratite dole navedene instrukcije kako bi ste nastavili sa resetovanjem Vaše lozinke.
        </p>
        
        <p>
            Ako niste zahtevali resetovanje lozinke možete ignorisati ovaj mejl. 
            Ovaj zahtev će isteći za 30 dana.
        </p>
        
        <p>
            Kliknite na link ispod ili ga kopirajte u navigacioni bar vašek pretraživača kako bi ste resetovali 
            Vašu lozinku koristeći naše zaštićene servere.
        </p>
        
        <p><a href="${passwordLink}" title="password">${passwordLink}</a> </p>
        
        <p>Hvala Vam!</p>
    </body>
</html>
</#assign>

<#assign text>
Poštovani:

Dobili smo zahtev za resetovanje šifre za Vaš ${siteName} nalog (${userAccount.emailAddress}). 

Molimo Vas da pratite dole navedene instrukcije kako bi ste nastavili sa resetovanjem Vaše lozinke.

Kliknite na link ispod ili ga kopirajte u navigacioni bar vašek pretraživača kako bi ste resetovali 
Vašu lozinku koristeći naše zaštićene servere.

${passwordLink}
        
Hvala Vam!
</#assign>

<@email subject=subject html=html text=text />
