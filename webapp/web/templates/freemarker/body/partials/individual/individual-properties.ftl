<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for property listing on individual profile page -->

<#-- RY Just a temporary fix to prevent classgroup heading from being pushed to the right edge of the page. 
Feel free to redo/remove. -->
<#--><div style="clear: both;" />-->

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
        <#-- If there are no groups, a dummy group has been created with a null name. -->
        <#assign groupName = "">
    </#if> 
    
    <section class="property-groups" role="region">
   
        <#-- Display the group heading --> 
        <#if groupName?has_content>
            <h2><a name="${groupName}"></a>${groupName}</h2
        </#if>
        
        <#-- Now list the properties in the group -->
        
            <#list group.properties as property>
            <article class="property-group" role="article">
                <#-- Property display name -->
                <h3>${property.name}</h3>

                <#-- List the statements for each property -->                    
                <#if property.type == "data"> <#-- data property -->
                    <#list property.statements as statement>
                        <p class="data-property">${statement.value}</p>
                         <!-- end data-prop-stmt-value -->
                    </#list>
            
                <#else> <#-- object property -->      
                    <#if property.collatedBySubclass>                             
                        <#include "objectPropertyList-collated.ftl">
                    <#else>
                    <ul class="object-property" role="list">
                        <#include "${property.template}">
                    </ul> <!-- end obj-prop-stmt-obj -->
                    </#if>
                </#if>                   
             <!-- end property -->  
              </article>             
            </#list>            
        <!-- end properties -->        
    </section> <!-- end property-group -->   
</#list> 
