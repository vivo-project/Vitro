<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if link??>
    <form method="post" action="RecomputeInferences">
        <input type="submit" value="Recompute Inferences" name="submit"/>
        <input type="hidden" value="Recompute" name="signal">
    </form>
</#if>
<#if message??>
    <p>${message}</p>
</#if>