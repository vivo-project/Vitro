<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for adding a user account -->

<h1>Add new account</h1>

    <#if errorEmailIsEmpty??>
        <#assign errorMessage = "You must supply an email address." />
    </#if>
    
    <#if errorEmailInUse??>
        <#assign errorMessage = "An account with that email address already exists." />
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
        <#assign errorMessage = "Password must be between 6 and 12 characters." />
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

<form method="POST" action="${formUrls.add}">
    Email address * 
    <br/>
    <input type="text" name="emailAddress" value="${emailAddress}" />
    <br/> 
    First name * 
    <br/>
    <input type="text" name="firstName" value="${firstName}" />
    <br/> 
    Last name * 
    <br/>
    <input type="text" name="lastName" value="${lastName}" />
    <br/> 
    <br/> 
    Roles * 
    <br/>
    <#list roles as role>
        <input type="radio" name="role" value="${role.uri}" <#if selectedRole = role.uri>checked</#if> />${role.label}
        <br>
    </#list>
    <br/>
    
    <#if !emailIsEnabled??>
        Initial password *
        <input type="password" name="initialPassword" value="${initialPassword}" />
        <br/>
        Confirm initial password *
        <input type="password" name="confirmPassword" value="${confirmPassword}" />
        <br/>
    </#if>
    
    Associate a profile with this account
    <br/> 
    <input type="radio" name="associate" value="yes" <#if associate??>checked</#if> />Yes
    <br/> 
    <input type="radio" name="associate" value="no" <#if !associate??>checked</#if> />No
    <br/> 
   
    <#if emailIsEnabled??>
       <p>
           Note: An email will be sent to the address entered above 
           notifying that an account has been created. 
           It will include instructions for activating the account and creating a password.
       </p>
    </#if>
   
   <input type="submit" name="submitAdd" value="Add new account" /> 
   or <a href="${formUrls.list}">Cancel</a>
</form>
