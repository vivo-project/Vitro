<#-- $This file is distributed under the terms of the license in /doc/license.txt -->

<#-- FreeMarker test cases -->

<h2>Dates</h2>

<p>${now?datetime}</p>
<p>${now?date}</p>
<p>${now?time}</p>

<h2>Apples</h2>

<#list apples as apple>
    <p>${apple}</p>
</#list>