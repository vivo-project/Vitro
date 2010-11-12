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
            <h2>Log in</h2>

            <form id="log-in-form" action="${urls.home}/authenticate?login=block" method="post" name="log-in-form" />
                <label for="email">Email</label>
                <input class="text-field" name="loginName" id="loginName" type="text" required />

                <label for="password">Password</label>
                <input class="text-field" name="loginPassword" id="password" type="loginPassword" required />
                
                <p class="submit"><input name="loginForm" type="submit" class="green button" value="Log in"/></p>
    
                <input class="checkbox-remember-me" name="remember-me" type="checkbox" value="" />  
                <label class="label-remember-me" for="remember-me">Remember me</label>
            </form>

            <p class="forgot-password"><a href="#">Forgot your password?</a></p>
            <p class="request-account"><a class=" blue button" href="#">Request an account</a> </p>
        </section><!-- #log-in -->
    </#if>
</#macro> -->