<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for email message sent to site administrator when an error occurs on the site. -->

<#assign subject = "An error occurred on the VIVO site" />

<#assign datetime = datetime?string("yyyy-MM-dd HH:mm:ss zzz")>

<#assign html>
<html>
    <head>
        <title>${subject}</title>
    </head>
    <body>
        <p>
            An error occurred on your VIVO site at ${datetime}.
        </p>
        
        <p>
            <strong>Requested url:</strong> ${requestedUrl}.
        </p>
        
        <p>
            <strong>Error message:</strong> ${errorMessage}.
        </p>
        
        <p>
            <strong>Stack trace</strong> (full trace available in the vivo log):
        </p>
        
        <div>${stackTrace}</div>
        
    </body>
</html>
</#assign>

<#assign text>
An error occurred on your VIVO site at ${datetime}.

Requested url: ${requestedUrl}.

Error message: ${errorMessage}.

Stack trace (full trace available in the vivo log):

${stackTrace}        
</#assign>

<@email subject=subject html=html text=text />