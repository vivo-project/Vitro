<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Main template for the login panel -->

<#if loginPanel??>
    ${loginPanel}
    
    ${stylesheets.add("/css/login.css")}
    ${stylesheets.addFromTheme("/css/formedit.css")}
    ${scripts.add("/js/jquery.js", "/js/login/loginUtils.js")}
</#if>