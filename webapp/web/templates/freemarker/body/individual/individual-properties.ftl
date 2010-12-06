<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for property listing on individual profile page -->

<#assign propertyGroups = individual.propertyList>

<#list propertyGroups as group>
 
    <#-- Display the group heading -->    
    <#-- If there are no groups, a dummy group has been created with a null name. -->
    <#if ! group.name??> 
        <#-- Here you might just do nothing and proceed to list the properties as in the grouped case, 
        or you might choose different markup for the groupless case. -->        
    <#-- This is the group for properties not assigned to any group. It has an empty name. -->
    <#elseif group.name?length == 0> 
        <h3>other</h3>       
    <#else>
        <h3>${group.name}</h3>
    </#if>
    
    <#-- Now list the properties in the group -->
    <#list group.properties as property>
        <h4>${property.name}</h4>
        
        <#-- List the statements for each property -->
        <#list property.statements as statement>
            <#if statement.value??> <#-- data property -->
                <div class="dataprop-value">
                    ${statement.value}
                </div>
            </#if>
        </#list>
    </#list>
    
</#list> 