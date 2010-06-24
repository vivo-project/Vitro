<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- FreeMarker test cases -->

<h2>Dates</h2>
<ul>
<li>${now?datetime}</li>
<li>${now?date}</li>
<li>${now?time}</li>
</ul>

<h2>Apples</h2>
<ul>
<#list apples as apple>
    <li>${apple}</li>
</#list>
</ul>

<h2>Fruit</h2>
<ul>
<#list fruit as f>
    <li>${f}</li>
</#list>
</ul>

<p><strong>Animal:</strong> ${animal}</p>

<h2>Zoo 1</h2>
<ul>
<#list zoo1.animals as animal>
    <li>${animal}</li>
</#list>
</ul>

<h2>Zoo 2</h2>
<ul>
<#list zoo2.animals as animal>
    <li>${animal}</li>
</#list>
</ul>

<p><strong>Berries: </strong>${berries}</p>

<@dump var="now" />
<@dump var="urls" />
<@dump var="fruit" />
