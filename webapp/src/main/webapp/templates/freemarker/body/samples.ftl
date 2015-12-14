<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- FreeMarker samples -->

<#import "lib-datetime.ftl" as dt>
<#import "lib-string.ftl" as str>

<h2>${title!}</h2>

<@widget name="test" />

<h3>${i18n().dates}</h3>
<ul>
    <li>${i18n().current_date_time} ${.now?datetime}</li>
    <li>${i18n().current_date} ${.now?date}</li>
    <li>${i18n().current_time} ${.now?time}</li>
</ul>

<h3>${i18n().formatted_date_time}</h3>
<p><p>${dt.xsdDateTimeToYear(xsddatetime)}</p>

<h3>${i18n().apples}</h3>
<ul>
<#list apples as apple>
    <li>${apple}</li>
</#list>
</ul>

<h3>${i18n().fruit}</h3>
<ul>
<#list fruit as f>
    <li>${f}</li>
</#list>
</ul>

<p><strong>${i18n().animal}</strong> ${animal}</p>

<p><strong>${i18n().book_title}</strong> ${bookTitle}</p>


<h3>${i18n().zoo_one}</h3>
<ul>
<#list zoo1.animals as animal>
    <li>${animal}</li>
</#list>
</ul>

<h3>${i18n().zoo_two}</h3>
<ul>
<#list zoo2.animals as animal>
    <li>${animal}</li>
</#list>
</ul>

<p><strong>${i18n().berries} </strong>${berries}</p>

<p>${year?number?c}</p>

<h3>${i18n().raw_string_literals}</h3>
<p>${r"#{title!}"}</p>
<p>${r"${title!}"}</p>

<h2>${i18n().containers_do_not_pick_up_changes}</h2>
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

<h3>${i18n().list_elements_of} ${r"${fruit}"}</h3>
<#list fruit as f>
    ${f}<br />
</#list>

<h3>${i18n().list_elements_of} ${r"${food}"}: ${i18n().contains_no_pears}</h3>
<#list food as item>
    <#list item as i>
        ${i}<br />
    </#list>
</#list>


<h3>${i18n().numbers}</h3>

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

<h3>${i18n().undo_camelcasing}</h3>
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




