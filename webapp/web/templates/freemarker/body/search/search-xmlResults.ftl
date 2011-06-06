<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<?xml version="1.0" encoding="UTF-8"?>
<response>

  <lst name="responseHeader">
    <str name="q">${querytext?xml}</str>
    <#if nextPage??>
      <str name="nextPage">${nextPage?xml}</str>
    </#if>   
  </lst>
  
  <result name="response" numFound="${hitCount}" start="${startIndex}" >
    <#list individuals as individual>                 
        <doc>
          <str name="uri">${individual.uri?xml}</str>                      
          <str name="name">${individual.name?xml}</str>
        </doc>
    </#list>
  </result>
  
</response>