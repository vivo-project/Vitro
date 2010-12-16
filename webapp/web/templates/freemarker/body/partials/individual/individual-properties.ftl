<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for property listing on individual profile page -->

<#list propertyGroups as group>

    <#-- Get the group name -->
    <#if group.name??>        
        <#if group.name?has_content>
            <#assign groupName = group.name>
        <#else>
            <#-- This is the group for properties not assigned to any group. It has an empty name. -->
            <#assign groupName = "other">
        </#if>
    <#else>
        <#-- If there are no groups, a dummy group has been created with a null (as opposed to empty) name. -->
        <#assign groupName = "">
    </#if> 
    
    <section class="property-groups" role="region">
   
        <#-- Display the group heading --> 
        <#if groupName?has_content>
            <h2><a name="${groupName}"></a>${groupName}</h2
        </#if>
        
        <#-- List the properties in the group -->        
        <#list group.properties as property>
            <article class="property-group" role="article">
                <#-- Property display name -->
                <h3>${property.name}</h3>
                    
                <#-- List the statements for each property -->   
                <#-- data property -->                 
                <#if property.type == "data"> 
                    <#list property.statements as statement>
                        <p class="data-property">${statement.value}</p>
                    </#list>
                        
                <#-- object property -->      
                <#elseif property.collatedBySubclass>                             
                    <#include "objectPropertyList-collated.ftl">
                <#else>
                    <#include "objectPropertyList-statements.ftl">
                </#if>                   
            </article>              

        </#list>                    
    </section> 
</#list> 
