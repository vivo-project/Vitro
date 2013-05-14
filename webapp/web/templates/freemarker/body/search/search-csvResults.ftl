<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
URI,Name
    <#list individuals as individual>                 
          ${individual.uri?xml},${individual.name?xml}
    </#list>
