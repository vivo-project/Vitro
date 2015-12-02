<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for adding a user account -->

<#assign strings = i18n() />

<h3><a class="account-menu" href="accountsAdmin" title="${strings.user_accounts_title}">${strings.user_accounts_link}</a> > ${strings.add_new_account}</h3>

    <#if errorEmailIsEmpty??>
        <#assign errorMessage = strings.error_no_email />
    <#elseif errorEmailInUse??>
        <#assign errorMessage = strings.error_email_already_exists />
    <#elseif errorEmailInvalidFormat??>
        <#assign errorMessage = strings.error_invalid_email(emailAddress) />
    <#elseif errorExternalAuthIdInUse??>
        <#assign errorMessage = strings.error_external_auth_already_exists />
    <#elseif errorFirstNameIsEmpty??>
        <#assign errorMessage = strings.error_no_first_name />
    <#elseif errorLastNameIsEmpty??>
        <#assign errorMessage = strings.error_no_last_name />
    <#elseif errorNoRoleSelected??>
        <#assign errorMessage = strings.error_no_role />
    <#elseif errorPasswordIsEmpty??>
        <#assign errorMessage = strings.error_no_password />
    <#elseif errorPasswordIsWrongLength??>
        <#assign errorMessage = strings.error_password_length(minimumLength, maximumLength) />
    <#elseif errorPasswordsDontMatch??>
        <#assign errorMessage = strings.error_password_mismatch />
    </#if>

    <#if errorMessage?has_content>
        <section id="error-alert" role="alert">
            <img src="${urls.images}/iconAlert.png" width="24" height="24" alt="${strings.alt_error_alert}" />
            <p>${errorMessage}</p>
        </section>
    </#if>

<section id="add-account" role="region">
    <form method="POST" action="${formUrls.add}" class="customForm" role="add new account">
        <label for="email-address">${strings.email_address}<span class="requiredHint"> *</span></label>
        <input type="text" name="emailAddress" value="${emailAddress}" id="email-address" role="input" />

        <label for="first-name">${strings.first_name}<span class="requiredHint"> *</span></label> 
        <input type="text" name="firstName" value="${firstName}" id="first-name" role="input" />

        <label for="last-name">${strings.last_name}<span class="requiredHint"> *</span></label> 
        <input type="text" name="lastName" value="${lastName}" id="last-name" role="input" />

        <#include "userAccounts-associateProfilePanel.ftl">

        <p><input id="externalAuthChkBox" type="checkbox" name="externalAuthOnly" <#if externalAuthOnly?? >checked</#if> />${strings.external_auth_only}</p>
        <p>${strings.roles}<span class="requiredHint"> *</span></p>
        <#list roles as role>
            <input type="radio" name="role" value="${role.uri}" role="radio" ${selectedRoles?seq_contains(role.uri)?string("checked", "")} />
            <label class="inline" for="${role.label}"> ${role.label}</label>
            <br />
        </#list>

        <#if emailIsEnabled??>
            <p class="note">${strings.new_account_note}</p>
        <#else>
            <section id="passwordContainer" role="region">
                <label for="initial-password">${strings.initial_password}<span class="requiredHint"> *</span></label>
                <input type="password" name="initialPassword" value="${initialPassword}" id="initial-password" role="input" />
                <p class="note">${strings.minimum_password_length(minimumLength, maximumLength)}</p>
                
                <label for="confirm-password">${strings.confirm_initial_password}<span class="requiredHint"> *</span></label> 
                <input type="password" name="confirmPassword" value="${confirmPassword}" id="confirm-password" role="input" />
            </section>
        </#if>
        
        <p><input type="submit" name="submitAdd" value="${strings.submit_add_new_account}" class="submit" /> ${strings.or} <a class="cancel" href="${formUrls.list}" title="${strings.cancel_title}">${strings.cancel_link}</a></p>

        <p class="requiredHint">* ${strings.required_fields}</p>
    </form>
</section>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/account/account.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/account/accountExternalAuthFlag.js"></script>')}