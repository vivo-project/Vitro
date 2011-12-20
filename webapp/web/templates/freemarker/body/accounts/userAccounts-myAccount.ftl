<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for editing a user account -->

<h3>My account</h3>

    <#if errorEmailIsEmpty??>
        <#assign errorMessage = "You must supply an email address." />
    </#if>
    
    <#if errorEmailInUse??>
        <#assign errorMessage = "An account with that email address already exists." />
    </#if>
    
    <#if errorEmailInvalidFormat??>
        <#assign errorMessage = "'${emailAddress}' is not a valid email address." />
    </#if>
    
    <#if errorFirstNameIsEmpty??>
        <#assign errorMessage = "You must supply a first name." />
    </#if>
    
    <#if errorLastNameIsEmpty??>
        <#assign errorMessage = "You must supply a last name." />
    </#if>
    
    <#if errorPasswordIsEmpty??>
        <#assign errorMessage = "No password supplied." />
    </#if>
    
    <#if errorPasswordIsWrongLength??>
        <#assign errorMessage = "Password must be between ${minimumLength} and ${maximumLength} characters." />
    </#if>
    
    <#if errorPasswordsDontMatch??>
        <#assign errorMessage = "Passwords do not match." />
    </#if>
    
    <#if errorMessage?has_content>
        <section id="error-alert" role="alert">
            <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon"/>
            <p>${errorMessage}</p>
        </section>
    </#if>

    <#if confirmChange??>
        <#assign confirmMessage = "Your changes have been saved." />
    </#if>
    
    <#if confirmEmailSent??>
        <#assign confirmMessage = "Your changes have been saved. A confirmation email has been sent to ${emailAddress}." />
    </#if>
    
    <#if confirmMessage?has_content>
        <section  class="account-feedback" role="alert">
            <p><img class="middle" src="${urls.images}/iconConfirmation.png" alert="Confirmation icon"/> ${confirmMessage}</p>
        </section>
    </#if>

<section id="my-account" role="region">
    <form id="main-form" method="POST" action="${formUrls.myAccount}" class="customForm" role="my account">
        <#if showProxyPanel?? >
            <#include "userAccounts-myProxiesPanel.ftl">
        </#if>

        <label for="email-address">Email address<span class="requiredHint"> *</span></label>
        <input type="text" name="emailAddress" value="${emailAddress}" id="email-address" role="input" />

        <p class="note">Note: if email changes, a confirmation email will<br />be sent to the new email address entered above.</p>
        
        <label for="first-name">First name<span class="requiredHint"> *</span></label> 
        <input type="text" name="firstName" value="${firstName}" id="first-name" role="input" />

        <label for="last-name">Last name<span class="requiredHint"> *</span></label> 
        <input type="text" name="lastName" value="${lastName}" id="last-name" role="input" />

        <#if !externalAuth??>
            <label for="new-password">New password</label>
            <input type="password" name="newPassword" value="${newPassword}" id="new-password" role="input" />

            <p class="note">Minimum of ${minimumLength} characters in length.<br />If left blank, the password will not be changed.</p>

            <label for="confirm-password">Confirm new password</label> 
            <input type="password" name="confirmPassword" value="${confirmPassword}" id="confirm-password" role="input" />
        </#if>

        <p><input type="submit" id="submitMyAccount" name="submitMyAccount" value="Save changes" class="submit" disabled /> or <a class="cancel" href="${urls.referringPage}" title="cancel">Cancel</a></p>

        <p class="requiredHint">* required fields</p>
    </form>
</section>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/account/account.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/account/accountListenerSetup.js"></script>')}