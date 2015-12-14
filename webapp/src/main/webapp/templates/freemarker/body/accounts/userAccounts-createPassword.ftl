<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for adding a user account -->

<#assign strings = i18n() />

<h3>${strings.create_your_password}</h3>

<#if errorPasswordIsEmpty??>
    <#assign errorMessage = strings.error_no_password />
<#elseif errorPasswordIsWrongLength??>
    <#assign errorMessage = strings.error_password_length(minimumLength, maximumLength) />
<#elseif errorPasswordsDontMatch??>
    <#assign errorMessage = strings.error_password_mismatch />
</#if>

<#if errorMessage?has_content>
    <section id="error-alert" role="alert">
        <img src="${urls.images}/iconAlert.png" width="24" height="24" alt="${strings.alt_error_alert}"/>
        <p>${errorMessage}</p>
    </section>
</#if>

<section id="create-password" role="region">
    <p>${strings.enter_new_password(userAccount.emailAddress)}</p>

    <form method="POST" action="${formUrls.createPassword}" class="customForm" role="create password">
        <input type="hidden" name="user" value="${userAccount.emailAddress}" role="input" />
        <input type="hidden" name="key"  value="${userAccount.passwordLinkExpiresHash}" role="input" />
    
        <label for="new-password">${strings.new_password}<span class="requiredHint"> *</span></label>
        <input type="password" name="newPassword" value="${newPassword}" id="new-password" role="input" />
        
        <p class="note">${strings.minimum_password_length(minimumLength, maximumLength)}</p>

        <label for="confirm-password">${strings.confirm_password}<span class="requiredHint"> *</span></label>
        <input type="password" name="confirmPassword" value="${confirmPassword}" id="confirm-password" role="input" />

        <p><input type="submit" name="submit" value="${strings.save_changes}" class="submit"/></p>

        <p class="requiredHint">* ${strings.required_fields}</p>
    </form>
</section>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/account/account.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}