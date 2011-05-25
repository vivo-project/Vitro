<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for adding a user account -->

<h3>Add new account</h3>

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

<section id="add-account" role="region">
    <fieldset>
        <legend>Add new account</legend>

        <form method="POST" action="${formUrls.createPassword}" class="customForm" role="create password">
            <input type="hidden" name="user" value="${userAccount.emailAddress}" />
            <input type="hidden" name="key"  value="${userAccount.passwordLinkExpiresHash}" />
        
            <label for="password">Password<span class="requiredHint"> *</span></label>
            <input type="password" name="password" value="${password}" id="password" role="input "/>
            
            <p>Minimum of ${minimumLength} characters in length.</p>

            <label for="confirm-password">Confirm Password<span class="requiredHint"> *</span></label>
            <input type="password" name="confirmPassword" value="${confirmPassword}" id="confirm-password" role="input "/>

            <input type="submit" name="submitCreatePassword" value="Save changes" class="submit"/>

            <p class="requiredHint">* required fields</p>
        </form>
    </fieldset>
</section>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}