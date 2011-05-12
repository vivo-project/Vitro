<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for login using internal vitro account (even when external auth is enabled). Accessible at /admin/login -->

<section id="internalLogin" role="region">
    <h2>Internal Login</h2>

    <#if errorNoUser??>
        <#assign errorMessage = "No email supplied." />
    </#if>
    
    <#if errorNoPassword??>
        <#assign errorMessage = "No password supplied." />
    </#if>
    
    <#if errorLoginFailed??>
        <#assign errorMessage = "Email or Password was incorrect." />
    </#if>
    
    <#if (errorNoUser?? || errorNoPassword?? || errorLoginFailed?? )>
        <section id="error-alert" role="alert">
            <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon"/>
            <p>${errorMessage}</p>
        </section>
    </#if>
    
    <#if ( !newPasswordRequired?? )>
        <p>Enter the email address and password for your internal Vitro account.</p>
    <#else>
        <p>You must change your password to log in.</p>
    </#if>

	<form method="post" action="${controllerUrl}">
        <#if newPasswordRequired??>
            <label for="newPassword">New Password</label>
            <input name="password" id="password" class="text-field" type="password" required autofocus />
            
            <p class="password-note">Minimum of 6 characters in length.</p>
            
            <label for="confirmPassword">Confirm Password</label>
            <input id="confirmPassword" name="confirmPassword" class="text-field" type="password" required />
            
            <input id="username" name="username" type="hidden" value="${username!}" />
            <input id="password" name="password" type="hidden" value="${password!}" />
        <#else>
            <label for="username">Email</label>
            <input id="username" name="username" class="text-field focus" type="text" value="${username!}" required autofocus />

        	<label for="password">Password</label>
            <input id="password" name="password" class="text-field" type="password" required />
        </#if>

		<p class="submit"><input name="loginForm" type="submit" class="green button" value="Log in"/></p>
	</form>
</section>