<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- SelecListWidget  -->

<#macro assets>
    <#--
    ${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/something.css" />')}
    ${scripts.add('<script type="text/javascript" src="${urls.base}/js/somejavascript.js"></script>')}
    --> 
</#macro>

<#macro markup>                      
          <#assign keys = selectList?keys>
          <#list keys as key>
             <option value="${key}" >${selectList[key]}</option>           
          </#list>    
</#macro>
