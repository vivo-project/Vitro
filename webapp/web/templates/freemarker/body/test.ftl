<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- FreeMarker test cases -->

<#import "lib-datetime.ftl" as dt>

<h2>${title}</h2>

<p>Current date & time: ${now?datetime}</p>
<p>Current date: ${now?date}</p>
<p>Current datetime: ${now?time}</p>


<p>${dt.xsdDateTimeToYear(datetime)}</p>