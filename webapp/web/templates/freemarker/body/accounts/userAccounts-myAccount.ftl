<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for editing a user account -->

<#assign strings = i18n() />

<h3>${strings.myAccount_heading}</h3>

    <#if errorEmailIsEmpty??>
        <#assign errorMessage = strings.error_no_email />
    <#elseif errorEmailInUse??>
        <#assign errorMessage = strings.error_email_already_exists />
    <#elseif errorEmailInvalidFormat??>
        <#assign errorMessage = strings.error_invalid_email(emailAddress) />
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

    <#if confirmChange??>
        <#assign confirmMessage = strings.myAccount_confirm_changes />
    </#if>
    
    <#if confirmEmailSent??>
        <#assign confirmMessage = strings.myAccount_confirm_changes_plus_note(emailAddress) />
    </#if>
    
    <#if confirmMessage?has_content>
        <section  class="account-feedback" role="alert">
            <p><img class="middle" src="${urls.images}/iconConfirmation.png" alt="${strings.alt_confirmation}"/> ${confirmMessage}</p>
        </section>
    </#if>

<section id="my-account" role="region">
    <form id="main-form" method="POST" action="${formUrls.myAccount}" class="customForm" role="my account">
        <#if showProxyPanel?? >
            <#include "userAccounts-myProxiesPanel.ftl">
        </#if>

        <label for="email-address">${strings.email_address}<span class="requiredHint"> *</span></label>
        <input type="text" name="emailAddress" value="${emailAddress}" id="email-address" role="input" />

        <p class="note">${strings.email_change_will_be_confirmed}</p>
        
        <label for="first-name">${strings.first_name}<span class="requiredHint"> *</span></label> 
        <input type="text" name="firstName" value="${firstName}" id="first-name" role="input" />

        <label for="last-name">${strings.last_name}<span class="requiredHint"> *</span></label> 
        <input type="text" name="lastName" value="${lastName}" id="last-name" role="input" />

        <#if !externalAuth??>
            <label for="new-password">${strings.new_password}</label>
            <input type="password" name="newPassword" value="${newPassword}" id="new-password" role="input" />

            <p class="note">${strings.minimum_password_length(minimumLength, maximumLength)}<br />${strings.leave_password_unchanged}</p>

            <label for="confirm-password">${strings.confirm_password}</label> 
            <input type="password" name="confirmPassword" value="${confirmPassword}" id="confirm-password" role="input" />
        </#if>

        <p>
            <input type="submit" id="submitMyAccount" name="submitMyAccount" value="${strings.save_changes}" class="submit" disabled /> 
            ${strings.or} 
            <a class="cancel" href="${urls.referringPage}" title="${strings.cancel_title}">${strings.cancel_link}</a>
        </p>

        <p class="requiredHint">* ${strings.required_fields}</p>
    </form>
</section>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/account/account.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/account/accountListenerSetup.js"></script>')}