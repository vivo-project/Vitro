<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for the Fake External Authentication page. -->

<section role="region">
    <h2>Internal Login</h2>

    <#if errorNoUser??>
        <h3>No username supplied.</h3>
    </#if>
    
    <#if errorNoPassword??>
        <h3>No password supplied</h3>
    </#if>
    
    <#if errorLoginFailed??>
        <h3>Username or Password was incorrect.</h3>
    </#if>
    
    <#if newPasswordRequired??>
        <h3>This is your first time logging in. You must supply a new password.</h3>
    </#if>
    
    <p>
      Enter the username and password for your internal VIVO account.
    </p>

	<form action="${controllerUrl}">
    	<div> Username:	<input type="text" name="username" value="${username}"/> </div>
    	<div> Password:	<input type="text" name="password" /> </div>

        <#if newPasswordRequired??>
            <div>New Password:	<input type="text" name="newPassword" /> </div>
        </#if>

		<input type="submit" value="submit" /> 
	</form>
</section>
