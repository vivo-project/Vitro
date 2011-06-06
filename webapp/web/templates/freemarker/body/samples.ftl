<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- FreeMarker samples -->

<#import "lib-datetime.ftl" as dt>
<#import "lib-string.ftl" as str>

<h2>${title}</h2>

<@widget name="test" />

<h3>Dates</h3>
<ul>
    <li>Current date & time: ${.now?datetime}</li>
    <li>Current date: ${.now?date}</li>
    <li>Current time: ${.now?time}</li>
</ul>
<h3>Dates</h3>


<h3>Formatted datetime</h3>
<p><p>${dt.xsdDateTimeToYear(xsddatetime)}</p>

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

<p>${year?number?c}</p>

<h3>Raw String Literals</h3>
<p>${r"#{title}"}</p>
<p>${r"${title}"}</p>

<h2>Containers do not pick up changes to the value of their elements</h2>
<#assign
    fruit = ["apples", "oranges", "bananas"]
    veg = ["beans", "peas", "carrots"]
    food = [fruit, veg]
    fruit = fruit + ["pears"]
>

<#noparse>
    <#assign<br />
        fruit = ["apples", "oranges", "bananas"]<br />
        veg = ["beans", "peas", "carrots"]<br />
        food = [fruit, veg]<br />
        fruit = fruit + ["pears"]<br />
    ><br />
</#noparse>

<h3>List elements of ${r"${fruit}"}</h3>
<#list fruit as f>
    ${f}<br />
</#list>

<h3>List elements of ${r"${food}"}: contains no pears</h3>
<#list food as item>
    <#list item as i>
        ${i}<br />
    </#list>
</#list>


<h3>Numbers</h3>

<#assign
    one = 1
    two = 2
    numbers = [one, two]
    two = 20
    numbers2 = [one, two]
>

<#noparse>
    <#assign<br />
        one = 1<br />
        two = 2<br />
        numbers = [one, two]<br />
        two = 20<br />
        numbers2 = [one, two]<br />
    ><br />
</#noparse>

${r"${two}"}: ${two}<br />
${r"${numbers[1]}"}: ${numbers[1]}<br />
${r"${numbers2[1]}"}: ${numbers2[1]}<br />

<h3>Uncamelcasing</h3>
<#assign s1 = "FreemarkerTest">
${s1} => ${str.unCamelCase(s1)}<br />
<#assign s2 = "Freemarker">
${s2} => ${str.unCamelCase(s2)}<br />

<@dump var="now" />
<@dump var="urls" />
<@dump var="fruit" />
<@dump var="trueStatement" />
<@dump var="zoo1" />

${scripts.add('<script type="text/javascript" src="${urls.base}/js/script1.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/script2.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/script3.js"></script>')}

<@dumpAll />

<@help for="dump" />

<@help for="profileUrl" />




