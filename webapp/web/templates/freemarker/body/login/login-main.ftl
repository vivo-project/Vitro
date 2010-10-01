<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Main template for the login panel -->

<#if loginTemplate??>
    <#include loginTemplate>
    
    ${stylesheets.add("/css/login.css")}
    ${scripts.add("/js/jquery.js", "/js/login/loginUtils.js")}
</#if>