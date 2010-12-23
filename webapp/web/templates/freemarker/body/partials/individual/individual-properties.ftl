<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for property listing on individual profile page -->

<#list propertyGroups as group>

    <#assign groupname = group.name(nameForOtherGroup)>
    
    <section class="property-group" role="region">
        <nav class="scroll-up" role="navigation">
            <a href="#property-nav">
                <img src="${urls.images}/individual/scroll-up.png" alt="scroll to property group menus" />
            </a>
        </nav>
   
        <#-- Display the group heading --> 
        <#if groupname?has_content>
            <h2><a name="${groupname}"></a>${groupname?capitalize}</h2>
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
                        <@dataPropertyList property.statements />

                    <#-- object property -->      
                    <#elseif property.collatedBySubclass> <#-- collated -->                            
                        <@collatedObjectPropertyList property />
                    <#else> <#-- uncollated -->
                        <@objectPropertyList property.statements property.template />
                    </#if>  
                </ul>                 
            </article> <!-- end property -->             
        </#list>                    
    </section> <!-- end property-group -->
</#list> 

<#-----------------------------------------------------------------------------
    Macros for generating property lists
------------------------------------------------------------------------------>

<#macro dataPropertyList statements>
    <#list statements as statement>
        <@propertyListItem>${statement.value}</@propertyListItem>
    </#list> 
</#macro>

<#macro collatedObjectPropertyList property>
    <#assign subclasses = property.subclasses>
    <#list subclasses?keys as subclass>
        <li class="subclass">
            <h3>${subclass?lower_case}</h3>
            <ul class="subclass-property-list">
                <@objectPropertyList subclasses[subclass] property.template /> 
            </ul>
        </li>
    </#list>
</#macro>

<#macro objectPropertyList statements template>
    <#list statements as statement>
        <@propertyListItem><#include "${template}"></@propertyListItem>
    </#list>
</#macro>

<#macro propertyListItem>
    <li role="listitem"><#nested></li>
</#macro>