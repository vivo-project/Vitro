<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Test widget -->

<#macro assets>
    ${stylesheets.add("/css/test.css")}
    ${scripts.add("/js/testscript.js")}
    ${headScripts.add("/js/testheadscript.js")} 
</#macro>

<#macro loggedIn>
    <div class="testWidget">
        <h4>This is the test widget for logged-in users.</h4>    
        <p>Login status: ${status}.</p>
    </div>
</#macro>

<#macro notLoggedIn>
    <div class="testWidget">
        <h4>This is the test widget for non-logged-in users.</h4>    
        <p>Login status: ${status}.</p>
    </div>
</#macro>