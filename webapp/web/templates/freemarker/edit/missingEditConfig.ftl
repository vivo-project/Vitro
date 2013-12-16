<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<p>Unable to process the edit, possibly due to session experation.</p>

<#list params?keys as key>   
  <#if key != "editKey">

  <div id=${key}>
    <h2>${key}</h2>
    <#list params[key] as value>
      <p>${value}</p>
    </#list>
  </div>

 </#if>
</#list>
  