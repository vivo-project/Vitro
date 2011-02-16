<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<?xml version="1.0" encoding="UTF-8"?>
<responce>

  <lst name="responseHeader">
    <str name="q">${querytext?xml}</str>
    <#if nextPage??>
      <str name="nextPage">${nextPage?xml}</str>
    </#if>   
  </lst>
  
  <result name="responce" numFound="${hitsLength}" start="${startIndex}" >
    <#list individuals as individual>                 
        <doc>
          <str name="uri">${individual.uri}</str>                      
          <str name="name">${individual.name}</str>
        </doc>
    </#list>
  </result>
  
</responce>