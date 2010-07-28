<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- FreeMarker test cases -->

<h2>${title}</h2>

<h3>Dates</h3>
<ul>
<li>${now?datetime}</li>
<li>${now?date}</li>
<li>${now?time}</li>
</ul>

<h3>Apples</h3>
<ul>
<#list apples as apple>
    <li>${apple}</li>
</#list>
</ul>

<h3>Fruit</h3>
<ul>
<#list fruit as f>
    <li>${f}</li>
</#list>
</ul>

<p><strong>Animal:</strong> ${animal}</p>

<p><strong>Book Title:</strong> ${bookTitle}</p>


<h3>Zoo 1</h3>
<ul>
<#list zoo1.animals as animal>
    <li>${animal}</li>
</#list>
</ul>

<h3>Zoo 2</h3>
<ul>
<#list zoo2.animals as animal>
    <li>${animal}</li>
</#list>
</ul>

<p><strong>Berries: </strong>${berries}</p>

<@dump var="now" />
<@dump var="urls" />
<@dump var="fruit" />
<@dump var="trueStatement" />
<@dump var="falseStatement" />

<@dumpDataModel />

${stylesheets.addFromTheme("/sstest.css", "/sstest2.css")}
${scripts.addFromTheme("/jstest.js")}
${scripts.add("/js/script1.js", "/js/script2.js", "/js/script3.js")}