<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for login using internal vitro account (even when external auth is enabled). Accessible at /admin/login -->

<section id="internalLogin" role="region">
    <h2>${i18n().internal_login}</h2>

    <#if errorNoEmail??>
        <#assign errorMessage = "${i18n().no_email_supplied}" />
    </#if>
    
    <#if errorNoPassword??>
        <#assign errorMessage = "${i18n().no_password_supplied}" />
    </#if>
    
    <#if errorLoginDisabled??>
        <#assign errorMessage = "${i18n().logins_temporarily_disabled}" />
    </#if>
    
    <#if errorLoginFailed??>
        <#assign errorMessage = "${i18n().incorrect_email_password}" />
    </#if>
    
    <#if errorNewPasswordWrongLength??>
        <#assign errorMessage = "${i18n().password_length(minPasswordLength, maxPasswordLength)}" />
    </#if>
    
    <#if errorNewPasswordsDontMatch??>
        <#assign errorMessage = "${i18n().password_mismatch}" />
    </#if>
    
    <#if errorNewPasswordMatchesOld??>
        <#assign errorMessage = "${i18n().new_pwd_matches_existing}" />
    </#if>
    
    <#if errorMessage?has_content>
        <section id="error-alert" role="alert">
            <img src="${urls.images}/iconAlert.png" width="24" height="24" alt="${i18n().error_alert_icon}"/>
            <p>${errorMessage}</p>
        </section>
    </#if>
    
    <#if !newPasswordRequired??>
        <p>${i18n().enter_email_password}</p>
    <#else>
        <p>${i18n().change_password}</p>
    </#if>

	<form method="post" action="${controllerUrl}">
        <#if newPasswordRequired??>
            <label for="newPassword">${i18n().new_password}</label>
            <input name="newPassword" id="newPassword" class="text-field" type="password" required autofocus />
            
            <p class="password-note">${i18n().minimum_password_length(minPasswordLength)}</p>
            
            <label for="confirmPassword">${i18n().confirm_password}</label>
            <input id="confirmPassword" name="confirmPassword" class="text-field" type="password" required />
            
            <input id="email" name="email" type="hidden" value="${email!}" />
            <input id="password" name="password" type="hidden" value="${password!}" />
        <#else>
            <label for="email">${i18n().email_capitalized}</label>
            <input id="email" name="email" class="text-field focus" type="text" value="${email!}" required autofocus />

        	<label for="password">${i18n().password_capitalized}</label>
            <input id="password" name="password" class="text-field" type="password" required />
        </#if>

		<p class="submit"><input name="loginForm" type="submit" class="green button" value="${i18n().login_button}"/></p>
	</form>
</section>