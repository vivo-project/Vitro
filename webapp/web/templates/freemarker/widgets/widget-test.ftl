<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Test widget -->

<#macro assets>
    ${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/test.css" />')}
    ${scripts.add('<script type="text/javascript" src="${urls.base}/js/testscript.js"></script>')}
    ${headScripts.add('<script type="text/javascript" src="${urls.base}/js/testheadscript.js"></script>')}
</#macro>

<#macro loggedIn>
    <div class="testWidget">
        <h4>${i18n().test_for_logged_in_users}</h4>    
        <p>${i18n().login_status} ${status}.</p>
    </div>
</#macro>

<#macro notLoggedIn>
    <div class="testWidget">
        <h4>${i18n().test_for_nonlogged_in_users}</h4>    
        <p>${i18n().login_status} ${status}.</p>
    </div>
</#macro>