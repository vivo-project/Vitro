<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation email for user account password reset -->

<#assign subject = "Passwort zurücksetzen für ${siteName}" />

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p>
            Sehr geehrte/r ${userAccount.firstName} ${userAccount.lastName}:
        </p>
        
        <p>
            Wir haben eine Anfrage erhalten, Ihr Passwort für Ihren Account (${userAccount.emailAddress}) auf ${siteName} zurückzusetzen.
        </p>
        
        <p>
            Bitte folgen Sie den Anweisungen unten, um mit dem Zurücksetzen Ihres Passworts fortzufahren.
        </p>
        
        <p>
            If you did not request this new account you can safely ignore this email. 
            This request will expire if not acted upon within 30 days.
        </p>
        
        <p>
            
			Fügen Sie den untenstehenden Link in die Adressleiste Ihres Browsers ein,
			um Ihr Passwort über unseren Server zurückzusetzen. 
        </p>
        
        <p>${passwordLink}</p>
        
        <p>Vielen Dank!</p>
    </body>
</html>
</#assign>

<#assign text>
Sehr geehrte/r ${userAccount.firstName} ${userAccount.lastName}:
        
Wir haben eine Anfrage erhalten, Ihr Passwort für Ihren Account
(${userAccount.emailAddress}) auf ${siteName} zurückzusetzen. 

Bitte folgen Sie den Anweisungen unten, um mit dem Zurücksetzen Ihres Passworts fortzufahren.

Wenn Sie dies nicht beantragt haben, können Sie diese E-Mail ignorieren. 
Diese Anfrage erlischt innerhalb von 30 Tagen, wenn sie nicht beantwortet wird.

Fügen Sie den untenstehenden Link in die Adressleiste Ihres Browsers ein,
um Ihr Passwort über unseren Server zurückzusetzen. 

${passwordLink}
        
Vielen Dank!
</#assign>

<@email subject=subject html=html text=text />
