<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- SelecListWidget  -->

<#macro assets>
    <#--
    ${stylesheets.add("/css/something.css")}
    ${scripts.add("/js/somejavascript.js")}
    --> 
</#macro>

<#macro markup>                      
          <#assign keys = selectList?keys>
          <#list keys as key>
             <option value="${key}" >${selectList[key]}</option>           
          </#list>    
</#macro>
