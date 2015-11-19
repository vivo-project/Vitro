<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if link??>
    <form method="post" action="sdbsetup">
        <p>${sdbstatus}</p>
        <input type="submit" value="${i18n().run_sdb_setup}" name="submit"/>
        <input type="hidden" value="setup" name="setupsignal">
    </form>
</#if>
<#if message??>
    <p>${message}</p>
</#if>