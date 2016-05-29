<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Login widget -->

<#macro assets>
    <#-- RY This test should be replaced by login widget controller logic which displays different assets macros depending
         on login status, but currently there's no widget-specific doAssets() method. See NIHVIVO-1357. The test doesn't work
         because we don't have the user in the template data model when we generate the assets. This can also be fixed by 
         NIHVIVO-1357.     
    <#if ! user.loggedIn> -->
        ${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/login.css" />')}
        <#-- ${scripts.add("")} -->
        ${headScripts.add('<script type="text/javascript" src="${urls.base}/js/login/loginUtils.js"></script>')}
    <#-- </#if> -->
</#macro>

<#macro loginForm>
    <#assign infoClassHide = ''/>
    <#assign infoClassShow = ''/>

    <#-- Don't display the JavaScript required to edit message on the home page even if JavaScript is unavailable -->
    <#if currentServlet != 'home'>
        <noscript>
            <section id="error-alert">
                <img src="${urls.images}/iconAlertBig.png" alt="${i18n().alert_icon}"/>
                <p>${i18n().javascript_require_to_edit} <a href="http://www.enable-javascript.com" title="${i18n().javascript_instructions}">${i18n().to_enable_javascript}</a>.</p>
            </section>
        </noscript>
    </#if>

    <section id="login" class="hidden">
        <h2>${i18n().login_button}</h2>
    
        <#if infoMessage??>
            <h3>${infoMessage}</h3>
        </#if>
       
        <#if errorMessage??>
            <#assign infoClassShow = ' id="vivoAccountError"'/>
        
            <section id="error-alert" role="alert"><img src="${urls.images}/iconAlert.png" alt="${i18n().error_alert_icon}" />
                <p class="login-alert">${errorMessage}</p>
            </section>
        </#if>
       
        <form role="form" id="login-form" action="${formAction}" method="post" name="login-form" />
            <#if externalAuthUrl??>
                <#assign infoClassHide = 'class="vivoAccount"'/>
            
                <p class="external-auth"><a class="blue button" href="${externalAuthUrl}" title="${i18n().external_auth_name}">${i18n().external_login_text}</a></p>
                <!--<p class="or-auth">or</p>-->
                <h3 class="internal-auth"><!--Log in using your--> <b>${i18n().or}</b> ${siteName} ${i18n().account}</h3>
                
            </#if>
            
            <div ${infoClassHide} ${infoClassShow}>
            
                <label for="loginName">${i18n().email_capitalized}</label>
                <input id="loginName" name="loginName" class="text-field focus" type="text" value="${loginName!}" autocapitalize="off" required autofocus />

                <label for="loginPassword">${i18n().password_capitalized}</label>
                <input id="loginPassword" name="loginPassword" class="text-field" type="password" required />
          
                <p class="submit"><input name="loginForm" class="green button" type="submit" value="${i18n().login_button}"/></p>
          
                <#-- NC: remember me won't be ready for r1.2
                <input class="checkbox-remember-me" name="remember-me" type="checkbox" value="" />  
                <label class="label-remember-me" for="remember-me">Remember me</label> -->
                <#-- mb863: forgot password and request an account won't be part of VIVO r1.2
                <p class="forgot-password"><a href="#" title="forgot password">Forgot your password?</a></p>
                <p class="request-account"><a class="blue button" href="#" title="request an account">Request an account</a> </p>
                -->
            </div>
        </form>

    </section><!-- #log-in -->
</#macro> 

<#macro forcePasswordChange>
    <section id="login">
        <h2>${i18n().change_password_to_login}</h2>
           
            <#if errorMessage??>
                <div id="error-alert" role="alert"><img src="${urls.images}/iconAlert.png" width="24" height="24" alt="${i18n().error_alert_icon}"/>
                    <p>${errorMessage}</p>
                </div>
            </#if>
           
            <form role="form" id="login-form" action="${formAction}" method="post" name="login-form" />
                <label for="newPassword">${i18n().new_password_capitalized}</label>
                <input id="newPassword" name="newPassword" class="text-field focus" type="password" required autofocus/>
                
                <p class="password-note">${i18n().minimum_password_length(minimumPasswordLength, maximumPasswordLength)}</p>
                
                <label for="confirmPassword">${i18n().confirm_password_capitalized}</label>
                <input id="confirmPassword" name="confirmPassword" class="text-field" type="password" required />
                
                 <p class="submit-password"><input name="passwordChangeForm" class="green button" type="submit" value="${i18n().login_button}"/> <span class="or">or <a class="cancel" href="${cancelUrl}" title="${i18n().cancel_title}">${i18n().cancel_link}</a></span></p>
            </form>
    </section>
</#macro>

<#macro alreadyLoggedIn>
    <h2>${i18n().login_button}</h2>
    <p>${i18n().already_logged_in}</p>
</#macro>

<#macro error>
    <p>${i18n().we_have_an_error}</p>
</#macro>