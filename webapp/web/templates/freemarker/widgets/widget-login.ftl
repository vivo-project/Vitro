<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Login widget -->

<#macro assets>
    <#-- RY This test should be replaced by controller logic which doesn't display any assets if the user is logged in.
    See NIHVIVO-1357. This test does nothing, since loginName has not been put into the data model.
    <#if ! loginName?has_content> -->
        ${stylesheets.add("/css/login.css")} 
        ${scripts.add("/js/jquery.js", "/js/login/loginUtils.js")}
        <#-- ${headScripts.add("")} -->
    <#-- </#if> -->
</#macro>

<#macro loginForm>

    <section id="log-in">
        <h2>Log in</h2>

        <noscript>
            <section id="javascriptDisableWrapper">
                <section id="javascriptDisableContent">
                    <img src="${urls.siteIcons}/iconAlertBig.png" alt="Alert Icon"/>
                    <p>In order to edit VIVO content, you'll need to enable JavaScript.</p>
                </section>
            </section>
        </noscript>
    
        <#if infoMessage??>
            <h3>${infoMessage}</h3>
        </#if>
       
        <#if errorMessage??>
            <section id="errorAlert"><img src="${urls.siteIcons}/iconAlert.png"  alert="Error alert icon"/>
                <p>${errorMessage}</p>
            </section>
        </#if>
       
        <section id="loginFormAndLinks">
       
            <form id="log-in-form" action="${formAction}" method="post" name="log-in-form" />
                <label for="email">Email</label>
                <input class="text-field" name="loginName" id="loginName" type="text" required />
    
                <label for="password">Password</label>
                <input class="text-field" name="loginPassword" id="password" type="password" required />
                
                <p class="submit"><input name="loginForm" type="submit" class="green button" value="Log in"/></p>
    
                <input class="checkbox-remember-me" name="remember-me" type="checkbox" value="" />  
                <label class="label-remember-me" for="remember-me">Remember me</label>
            </form>

            <p class="forgot-password"><a href="#">Forgot your password?</a></p>
            <p class="request-account"><a class=" blue button" href="#">Request an account</a> </p>
        </section>
    </section><!-- #log-in -->
</#macro> 

<#macro forcePasswordChange>
    <section id="log-in">
        <h2>Log in</h2>
           
            <#if errorMessage??>
                <div id="errorAlert"><img src="${urls.siteIcons}/iconAlert.png" width="24" height="24" alert="Error alert icon"/>
                    <p>${errorMessage}</p>
                </div>
            </#if>
           
            <form id="log-in-form" action="${formAction}" method="post" name="log-in-form" />
                <label for="newPassword">New Password</label>
                <input id="newPassword" class="focus" type="password" name="newPassword"  />
                <p class="passwordNote">Minimum of 6 characters in length.</p>
                <label for="confirmPassword">Confirm Password</label>
                <input id="confirmPassword" type="password" name="confirmPassword"  />
                <input name="passwordChangeForm" type="submit" class="submit" value="Save Changes"/> <span class="or">or <a class="cancel" href="${cancelUrl}">Cancel</a></span>
            </form>
    </section>
</#macro>

<#macro error>
    <p>There was an error in the system.</p>
</#macro>