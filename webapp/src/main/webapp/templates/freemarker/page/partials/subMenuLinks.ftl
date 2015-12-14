<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- List of links that appear in submenus, like the header and footer. -->

<li><a href="${urls.about}">About</a></li>
<#if urls.contact??>
    <li><a href="${urls.contact}">Contact Us</a></li>
</#if> 
<li><a href="http://www.vivoweb.org/support" title="${i18n().support}">${i18n().support}</a></li>
<li><a href="${urls.index}" title="${i18n().index}">${i18n().index}</a></li>