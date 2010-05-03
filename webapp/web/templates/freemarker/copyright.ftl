<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if copyrightText??>
    <#assign copyright = copyrightText>
    <#if copyrightUrl??>
        <#assign copyright><a href="${copyrightUrl}">${copyrightText}</a></#assign>
    </#if>
    <div class="copyright">
        &copy;${copyrightYear?c} ${copyright}                  
    </div>
</#if>