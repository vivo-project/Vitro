<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Login widget -->

<#-- Question for Nick and Manolo: can we use this in Vitro, where html 5 is not being used, or should it be moved
to the wilma theme? Please do whatever's appropriate. -->

<#macro styles>
    <#if ! loginName??>
        ${stylesheets.add("/css/login.css")}
    </#if>
</#macro>

<#macro scripts>
    <#if ! loginName??>
        <#-- define any js files needed for the login widget -->
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
