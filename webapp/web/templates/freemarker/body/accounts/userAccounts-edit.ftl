<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for editing a user account -->

<h3><a class="account-menu" href="accountsAdmin" title="edit account">User accounts</a> > Edit account</h3>

    <#if errorEmailIsEmpty??>
        <#assign errorMessage = "You must supply an email address." />
    </#if>
    
    <#if errorEmailInUse??>
        <#assign errorMessage = "An account with that email address already exists." />
    </#if>
    
    <#if errorEmailInvalidFormat??>
        <#assign errorMessage = "'${emailAddress}' is not a valid email address." />
    </#if>
    
    <#if errorExternalAuthIdInUse??>
        <#assign errorMessage = "An account with that external authorization ID already exists." />
    </#if>
    
    <#if errorFirstNameIsEmpty??>
        <#assign errorMessage = "You must supply a first name." />
    </#if>
    
    <#if errorLastNameIsEmpty??>
        <#assign errorMessage = "You must supply a last name." />
    </#if>
    
    <#if errorNoRoleSelected??>
        <#assign errorMessage = "You must select a role." />
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
            <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon" />
            <p>${errorMessage}</p>
        </section>
    </#if>

<section id="edit-account" role="region">
    <form method="POST" action="${formUrls.edit}" id="userAccountForm" class="customForm" role="edit account">
        <label for="email-address">Email address<span class="requiredHint"> *</span></label>
        <input type="text" name="emailAddress" value="${emailAddress}" id="email-address" role="input" />

        <label for="first-name">First name<span class="requiredHint"> *</span></label> 
        <input type="text" name="firstName" value="${firstName}" id="first-name" role="input" />

        <label for="last-name">Last name<span class="requiredHint"> *</span></label> 
        <input type="text" name="lastName" value="${lastName}" id="last-name" role="input" />

        <#include "userAccounts-associateProfilePanel.ftl">

        <p><input id="externalAuthChkBox" type="checkbox" name="externalAuthOnly"  <#if externalAuthOnly?? >checked</#if>  />Externally Authenticated Only</p>
        <#if roles?has_content>
            <p>Roles<span class="requiredHint"> *</span></p>
            <#list roles as role>
                <input type="radio" name="role" value="${role.uri}" role="radio" <#if selectedRole = role.uri>checked</#if> />
                <label class="inline" for="${role.label}"> ${role.label}</label>
                <br />
            </#list>
        </#if>

        <#if emailIsEnabled??>
            <section id="pwdResetContainer" <#if externalAuthOnly?? >class="hidden"</#if> role="region">
                <input type="checkbox" name="resetPassword" value="" id="reset-password" role="checkbox" <#if resetPassword??>checked</#if> />
                <label  class="inline" for="reset-password"> Reset password</label>

                <p class="note">
                    Note: Instructions for resetting the password will 
                    be emailed to the address entered above. The password will not 
                    be reset until the user follows the link provided in this email.
                </p>
            </section>
        <#else>
            <section id="passwordContainer" <#if externalAuthOnly?? >class="hidden"</#if> role="region">
                <label for="new-password">New password</label>
                <input type="password" name="newPassword" value="${newPassword}" id="new-password" role="input" />
                <p class="note">Minimum of ${minimumLength} characters in length.<br />
                Leaving this blank means that the password will not be changed.</p>
                
                <label for="confirm-password">Confirm new password</label> 
                <input type="password" name="confirmPassword" value="${confirmPassword}" id="confirm-password" role="input" />
            </section>
        </#if>

        <p><input type="submit" id="submitMyAccount" name="submitEdit" value="Save changes" class="submit" disabled /> or <a class="cancel" href="${formUrls.list}" title="cancel">Cancel</a></p>

        <p class="requiredHint">* required fields</p>
    </form>
</section>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/account/account.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/account/accountExternalAuthFlag.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/account/accountListenerSetup.js"></script>')}
