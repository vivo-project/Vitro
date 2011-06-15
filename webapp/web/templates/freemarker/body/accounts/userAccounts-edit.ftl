<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for editing a user account -->

<h3><a class="account-menu" href="accountsAdmin">User accounts</a> > Edit account</h3>

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
            <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon"/>
            <p>${errorMessage}</p>
        </section>
    </#if>

<section id="edit-account" role="region">
    <fieldset>
        <legend>Edit new account</legend>

        <form method="POST" action="${formUrls.edit}" class="customForm" role="edit account">
            <label for="email-address">Email address<span class="requiredHint"> *</span></label>
            <input type="text" name="emailAddress" value="${emailAddress}" id="email-address" role="input" />

            <label for="first-name">First name<span class="requiredHint"> *</span></label> 
            <input type="text" name="firstName" value="${firstName}" id="first-name" role="input" />

            <label for="last-name">Last name<span class="requiredHint"> *</span></label> 
            <input type="text" name="lastName" value="${lastName}" id="last-name" role="input" />

            <label for="external-auth-id">External authorization ID (optional)</label> 
            <input type="text" name="externalAuthId" value="${externalAuthId}" id="external-auth-id" role="input "/>

            <p>Roles<span class="requiredHint"> *</span> </p>
            <#list roles as role>
                <input type="radio" name="role" value="${role.uri}" role="radio" <#if selectedRole = role.uri>selected</#if> />
                <label class="inline" for="${role.label}"> ${role.label}</label>
                <br />
            </#list>

            <#if !emailIsEnabled??>
                <label for="new-password">New password<span class="requiredHint"> *</span></label>
                <input type="password" name="newPassword" value="${newPassword}" id="new-password" role="input" />

                <p>Minimum of ${minimumLength} characters in length.</p>
                <p>Leaving this blank means that the password will not be changed.</p>

                <label for="confirm-password">Confirm initial password<span class="requiredHint"> *</span></label> 
                <input type="text" name="confirmPassword" value="${confirmPassword}" id="confirm-password" role="input" />
            </#if>

            <p>Associate a profile with this account</p>
            <input type="radio" name="associate" value="yes" role="radio" <#if associate??>checked</#if> id="associate" />
            <label class="inline" for="associate"> Yes</label>

            <input type="radio" name="associate" value="no"  role="radio" <#if !associate??>checked</#if> id="no-associate" />
            <label class="inline" for="no-associate"> No</label>

            <br />
            <input type="checkbox" name="resetPassword" value="" id="reset-password" role="checkbox" />
            <label  class="inline" for="reset-password"> Reset password</label>

            <#if emailIsEnabled??>
                <p class="note">
                    Note: Instructions for resetting the password will 
                    be emailed to the address entered above. The password will not 
                    be reset until the user follows the link provided in this email.
                </p>
            </#if>
    
            <input type="submit" name="submitEdit" value="Save changes" class="submit" /> or <a class="cancel" href="${formUrls.list}">Cancel</a>

            <p class="requiredHint">* required fields</p>
        </form>
    </fieldset>
</section>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}