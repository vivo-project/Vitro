<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if copyright??>
    <div class="copyright">
        &copy;${copyright.year?c}
        <#if copyright.url??>
            <a href="${copyright.url}" title="${i18n().copyright}">${copyright.text}</a>
        <#else>
            ${copyright.text}
        </#if>                 
    </div>
</#if>