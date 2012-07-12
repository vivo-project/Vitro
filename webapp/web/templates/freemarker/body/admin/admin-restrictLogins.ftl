<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for restricting (or opening) access to logins. -->

<h2>Restrict Logins</h2>
    <#if messageAlreadyRestricted??>
        <#assign errorMessage = "Logins are already restricted." />
    </#if>
    
    <#if messageAlreadyOpen??>
        <#assign errorMessage = "Logins are already not restricted." />
    </#if>
    
    <#if errorMessage?has_content>
        <section id="error-alert" role="alert">
            <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon" />
            <p>${errorMessage}</p>
        </section>
    </#if>

    <#if messageRestricting??>
        <#assign successMessage = "Logins are now restricted." />
    </#if>
    
    <#if messageOpening??>
        <#assign successMessage = "Logins are no longer restricted." />
    </#if>

    <#if successMessage?has_content>
        <section class="success">
            <p>${successMessage}</p>
        </section>
    </#if>


<section id="restrict-login" role="region">
    <#if restricted == true>
        <h4>Logins are restricted</h4>
        <p><a href="${openUrl}" title="Remove Restrictions">Remove Restrictions</a></p>
    <#else>
        <h4>Logins are open to all</h4>
        <p><a href="${restrictUrl}" title="Restrict Logins">Restrict Logins</a></p>
    </#if>
</section>
