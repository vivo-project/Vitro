<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Login widget -->

<#macro assets>
    <#if ! loginName??>
        ${stylesheets.add("/css/login.css")}
        <#-- define any js files needed for the login widget 
        ${scripts.add("")}
        ${headScripts.add("")} -->
    </#if>
</#macro>

<#macro markup>
    <#if ! loginName??>    
        <section id="log-in">
            <h3>Log in</h3>
            
            <form id="log-in-form" action="${urls.home}/authenticate?login=block" method="post" name="log-in-form" />
                <label for="loginName">Email</label>
                <div class="input-field">
                    <input name="loginName" id="loginName" type="text" required />
                </div>
                <label for="loginPassword">Password</label>
                <div class="input-field">
                    <input name="loginPassword" id="loginPassword" type="password" required />
                </div>
                <div id="wrapper-submit-remember-me">
                    <input name="loginForm"  type="submit" class="login green button" value="Log in"/>
                    <div id="remember-me">
                        <input class="checkbox-remember-me" name="remember-me" type="checkbox" value="" />
                        <label class="label-remember-me"for="remember-me">Remember me</label>
                    </div>
                </div>
                <p class="forgot-password"><a href="#">Forgot your password?</a></p>
            </form>
            
            <div id="request-account">
                <a class="blue button" href="#">Request an account</a>
            </div>
        </section> <!-- #log-in -->
    </#if>
</#macro>
