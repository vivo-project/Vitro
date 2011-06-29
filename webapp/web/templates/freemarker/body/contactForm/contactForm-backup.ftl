<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Backup of contact mail email -->

<p>${datetime}</p>

<#if spamReason??>
    <section id="error-alert" role="alert">
        <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon" />
        
        <p>REJECTED - SPAM</p>
        <p>${spamReason}</p>
    </section>
</#if>

${msgText}

<hr />