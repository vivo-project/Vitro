<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Backup of contact mail email -->

<p>${datetime}</p>

<#if spamReason??>
    <div style="color:red;">
        <p>REJECTED - SPAM</p>
        <p>${spamReason}</p>
    </div>
</#if>

${msgText}

<hr />
