<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Default object property statement template. 
    
     This template must be self-contained and not rely on other variables set for the individual page, because it
     is also used to generate the property statement during a deletion.  
 -->

<a href="${profileUrl(statement.uri("object"))}" title="name">${statement.label!statement.localName!}</a> 
