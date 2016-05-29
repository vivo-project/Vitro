<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

</header>

<#include "developer.ftl">

<nav role="navigation">
    <ul id="main-nav" role="list">
        <#list menu.items as item>
            <li role="listitem"><a href="${item.url}" <#if item.active> class="selected" </#if> title="${item.linkText!} ${i18n().menu_item}">${item.linkText}</a></li>
        </#list>
    </ul>
</nav>

<div id="wrapper-content" role="main">        
    <#if flash?has_content>
        <#if flash?starts_with("Welcome") >
            <section  id="welcome-msg-container" role="container">
                <section  id="welcome-message" role="alert">${flash}</section>
            </section>
        <#else>
            <section id="flash-message" role="alert">
                ${flash}
            </section>
        </#if>
    </#if>
    
    <!--[if lte IE 8]>
    <noscript>
        <p class="ie-alert">${i18n().javascript_ie_alert_text} Here are the <a href="http://www.enable-javascript.com" title="javascript instructions">${i18n().to_enable_javascript}</a>.</p>
    </noscript>
    <![endif]-->