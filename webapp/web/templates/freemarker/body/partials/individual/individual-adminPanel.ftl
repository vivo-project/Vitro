<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for admin panel on individual profile page -->

<#if individual.showAdminPanel>
    <section id="admin">
        <h3>Admin Panel</h3><a class="edit-individual" href="${individual.editUrl}">Edit this individual</a>
        
        <#if verbosePropertyForm??>
            <#assign formId = "verbosePropertyForm">
            <#assign currentValue = verbosePropertyForm.currentValue?string("on", "off")>
            <#assign newValue = verbosePropertyForm.currentValue?string("off", "on")>
            <form id="${formId}" action="${verbosePropertyForm.action}#${formId}" method="${verbosePropertyForm.method}">
                <input type="hidden" name="verbose" value="${verbosePropertyForm.newValue}" />
                <span>Verbose property display is <b>${currentValue}</b> | </span>
                <input type="submit" id="submit" class="small" value="Turn ${newValue}" />
            </form>  
        </#if> 

        <p class="uri-link">Resource URI: <a href="${individual.uri}" target="_blank">${individual.uri}</a></p>
    </section>
</#if>