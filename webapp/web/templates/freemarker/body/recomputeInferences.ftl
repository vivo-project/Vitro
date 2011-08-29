<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if formAction?has_content>
    <form method="post" action="${formAction}">
        <input type="submit" value="Recompute Inferences" name="submit"/>
    </form>
</#if>

<#if message?has_content>
    <p>${message}</p>
</#if>