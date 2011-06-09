<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for login using internal vitro account (even when external auth is enabled). Accessible at /admin/login -->

<section id="internalLogin" role="region">
    <h2>Internal Login</h2>

    <#if errorNoEmail??>
        <#assign errorMessage = "No email supplied." />
    </#if>
    
    <#if errorNoPassword??>
        <#assign errorMessage = "No password supplied." />
    </#if>
    
    <#if errorLoginFailed??>
        <#assign errorMessage = "Email or Password was incorrect." />
    </#if>
    
    <#if errorNewPasswordWrongLength??>
        <#assign errorMessage = "Password must be between 6 and 12 characters." />
    </#if>
    
    <#if errorNewPasswordsDontMatch??>
        <#assign errorMessage = "Passwords do not match." />
    </#if>
    
    <#if errorNewPasswordMatchesOld??>
        <#assign errorMessage = "Your new password must be different from your existing password." />
    </#if>
    
    <#if errorMessage?has_content>
        <section id="error-alert" role="alert">
            <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon"/>
            <p>${errorMessage}</p>
        </section>
    </#if>
    
    <#if !newPasswordRequired??>
        <p>Enter the email address and password for your internal Vitro account.</p>
    <#else>
        <p>You must change your password to log in.</p>
    </#if>

	<form method="post" action="${controllerUrl}">
        <#if newPasswordRequired??>
            <label for="newPassword">New Password</label>
            <input name="newPassword" id="newPassword" class="text-field" type="password" required autofocus />
            
            <p class="password-note">Minimum of 6 characters in length.</p>
            
            <label for="confirmPassword">Confirm Password</label>
            <input id="confirmPassword" name="confirmPassword" class="text-field" type="password" required />
            
            <input id="email" name="email" type="hidden" value="${email!}" />
            <input id="password" name="password" type="hidden" value="${password!}" />
        <#else>
            <label for="email">Email</label>
            <input id="email" name="email" class="text-field focus" type="text" value="${email!}" required autofocus />

        	<label for="password">Password</label>
            <input id="password" name="password" class="text-field" type="password" required />
        </#if>

		<p class="submit"><input name="loginForm" type="submit" class="green button" value="Log in"/></p>
	</form>
</section>