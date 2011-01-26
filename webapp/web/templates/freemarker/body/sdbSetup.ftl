<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Standard template to display a message generated from any controller. Keeps this out of individual templates. -->


<#if link??>
<form method="post" action="sdbsetup">
<input type="submit" value="SDB Setup" name="submit"/>
<input type="hidden" value="setup" name="setupsignal">
</form>
</#if>
<#if message??>
    <p>${message}</p>
</#if>