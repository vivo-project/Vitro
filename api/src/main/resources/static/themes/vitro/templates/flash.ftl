<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if flash?has_content>
    <#if flash?starts_with("Welcome") >
    <section  id="welcome-msg-container" role="container">
        <section  id="welcome-message" role="alert">${flash}</section>
    </section>
    <#else>
    <section  id="flash-msg-container" role="container">
        <section id="flash-message" role="alert">${flash}</section>
    </section>
    </#if>
</#if>

<!--[if lte IE 8]>
<noscript>
    <p class="ie-alert">${i18n().javascript_ie_alert_text} Here are the <a href="http://www.enable-javascript.com" title="javascript instructions">${i18n().to_enable_javascript}</a>.</p>
</noscript>
<![endif]-->
