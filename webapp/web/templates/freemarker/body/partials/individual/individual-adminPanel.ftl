<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for admin panel on individual profile page -->

<#if individual.showAdminPanel>
    <section id="admin">
        <h3>Admin Panel</h3><a class="edit-individual" href="${individual.editUrl}">Edit this individual</a>
        
        <#if verbosePropertyForm??>
            <#assign formId = "verbosePropertyForm">
            <form id="${formId}" action="${verbosePropertyForm.action}#${formId}" method="get">
                <input type="hidden" name="verbose" value="${verbosePropertyForm.verboseFieldValue}" />
                <span>Verbose property display for this session is <b>${verbosePropertyForm.currentValue}</b></span>
                <input type="submit" id="submit" value="Turn ${verbosePropertyForm.newValue}" />
            </form>  
        </#if>
    
        <p class="uri-link">Resource URI: <a href="${individual.uri}" target="_blank">${individual.uri}</a></p>
    </section>
</#if>