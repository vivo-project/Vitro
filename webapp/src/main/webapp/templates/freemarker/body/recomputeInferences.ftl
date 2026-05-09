<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#if formAction?has_content>
    <form method="post" action="${formAction}">
        <input class="submit" type="submit" value="${i18n().recompute_inferences}" name="submit" />
    </form>
</#if>

<#if message?has_content>
    <p>${message}</p>
</#if>
