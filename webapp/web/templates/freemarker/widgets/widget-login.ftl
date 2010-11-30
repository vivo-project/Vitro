<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Login widget -->

<#macro assets>
    <#-- RY This test should be replaced by widget controller logic which doesn't display any assets if the user is logged in.
    See NIHVIVO-1357. This test does nothing, since user has not been put into the data model.
    <#if ! user.loggedIn> -->
        ${stylesheets.add("/css/login.css")} 
        ${scripts.add("/js/jquery.js", "/js/login/loginUtils.js")}
        <#-- ${headScripts.add("")} -->
    <#-- </#if> -->
</#macro>

<#macro loginForm>

    <section id="login">
        <h2>Log in</h2>

        <noscript>
            <section id="error-alert">
                <img src="${urls.siteIcons}/iconAlertBig.png" alt="Alert Icon"/>
                <p>In order to edit VIVO content, you'll need to enable JavaScript.</p>
            </section>
        </noscript>
    
        <#if infoMessage??>
            <h3>${infoMessage}</h3>
        </#if>
       
        <#if errorMessage??>
            <div id="error-alert"><img src="${urls.siteIcons}/iconAlert.png" alert="Error alert icon"/>
                <p>${errorMessage}</p>
            </div>
        </#if>
       
        <form  role="form" id="login-form" action="${formAction}" method="post" name="login-form" />
            <label for="email">Email</label>
            <input class="text-field" name="loginName" id="loginName" type="text" required />

            <label for="password">Password</label>
            <input class="text-field" name="loginPassword" id="password" type="password" required />
            
            <p class="submit"><input name="loginForm" type="submit" class="green button" value="Log in"/></p>
            <input class="checkbox-remember-me" name="remember-me" type="checkbox" value="" />  
            <label class="label-remember-me" for="remember-me">Remember me</label>
            <p class="forgot-password"><a href="#">Forgot your password?</a></p>    
            <p class="request-account"><a class="blue button" href="#">Request an account</a> </p>    
                            
            <#if externalAuthUrl??>
                <p class="external-auth">
                    <a class="green button" href="${externalAuthUrl}">Log in using ${externalAuthName}</a>
                </p>
            </#if>

        </form>

    </section><!-- #log-in -->
</#macro> 

<#macro forcePasswordChange>
    <section id="login">
        <h2>Log in</h2>
           
            <#if errorMessage??>
                <div id="error-alert" role="alert"><img src="${urls.siteIcons}/iconAlert.png" width="24" height="24" alert="Error alert icon"/>
                    <p>${errorMessage}</p>
                </div>
            </#if>
           
            <form role="form" id="login-form" action="${formAction}" method="post" name="login-form" required />
                <label for="new-password">New Password</label>
                <input id="new-password" class="focus text-field" type="password" name="newPassword"  required />
                
                <p class="password-note">Minimum of 6 characters in length.</p>
                
                <label for="confirm-password">Confirm Password</label>
                <input id="confirm-password" class="text-field" type="password" name="confirmPassword"  />
                
                 <p class="submit-password"><input name="passwordChangeForm" type="submit" class="green button" value="Log in"/> <span class="or">or <a class="cancel" href="${cancelUrl}">Cancel</a></span></p>
            </form>
    </section>
</#macro>

<#macro alreadyLoggedIn>
    <h2>Log in</h2>
    <p>You are already logged in.</p>
</#macro>

<#macro error>
    <p>There was an error in the system.</p>
</#macro>