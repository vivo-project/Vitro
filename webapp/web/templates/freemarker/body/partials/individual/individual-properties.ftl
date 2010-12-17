<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for property listing on individual profile page -->

<#list propertyGroups as group>

    <#assign groupname = groupName(group)>
    
    <section class="property-group" role="region">
   
        <#-- Display the group heading --> 
        <#if groupname?has_content>
            <h2><a name="${groupname}"></a>${groupname}</h2>
        </#if>
        
        <#-- List the properties in the group -->        
        <#list group.properties as property>
            <article class="property" role="article">
                <#-- Property display name -->
                <h3>${property.name}</h3>
                    
                <#-- List the statements for each property -->   
                <ul class="property-list" role="list"> 
                    <#-- data property -->                 
                    <#if property.type == "data"> 
                        <#list property.statements as statement>
                            <li role="listitem">${statement.value}</li>
                        </#list>
                    
                    <#-- object property -->      
                    <#elseif property.collatedBySubclass>                             
                        <#include "objectPropertyList-collated.ftl">
                    <#else>
                        <#include "objectPropertyList-statements.ftl">
                    </#if>  
                </ul>                 
            </article> <!-- end property -->             
        </#list>                    
    </section> <!-- end property-group -->
</#list> 
