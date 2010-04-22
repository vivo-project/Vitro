<#-- $This file is distributed under the terms of the license in /doc/license.txt -->

<#--
<strong>
Simple scalar: ${aboutSS}<br />
Simple scalar converted to string with getAsString(): ${aboutStr}<br />
Simple scalar converted to string with toString(): ${aboutStr2}<br />
<#if (aboutSS == aboutText)>
    aboutText and aboutSS are equal<br />
</#if>
<#if (aboutSS == aboutStr)>
    aboutSS and aboutStr are equal<br />
</#if>
<#if (aboutStr == aboutStr2)>
    aboutStr and aboutStr2 are equal<br />
</#if>
</strong>


<h3>${testBean.name}</h3>
${testBean.setName("Fred")}
<h3>${testBean.name}</h3>
-->


<link rel="stylesheet" type="text/css" href="${stylesheetDir}ss1.css" />

<h2>${title}</h2>
<p class="noticeme">The red font color on the title of this page, and the styling of this paragraph, demonstrate the use of a stylesheet that has been invoked by the template and moved into the head by the servlet.</p>

<#if aboutText??>
    <div class="pageGroupBody" id="aboutText">${aboutText}</div>
</#if>
    
<#if acknowledgeText??>
    <div class="pageGroupBody" id="acknowledgementText">${acknowledgeText}</div> 
</#if>
          
<h3>Today's date, in different formats</h3>
<p>${date}</p>
<p>${date?string.short}</p>
<p>${date?string.medium}</p>
<p>${date?string.long}</p>
<p>${date?string.full}</p>
<h3>Current date and time, in different formats</h3>
<p>${datetime}</p>
<p>${datetime?string.short}</p>
<p>${datetime?string.medium}</p>
<p>${datetime?string.long}</p>
<p>${datetime?string.full}</p>          