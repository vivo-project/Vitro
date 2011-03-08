<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Test widget -->

<#macro assets>
    ${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/test.css" />')}
    ${scripts.add('<script type="text/javascript" src="${urls.base}/js/testscript.js"></script>')}
    ${headScripts.add('<script type="text/javascript" src="${urls.base}/js/testheadscript.js"></script>')}
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