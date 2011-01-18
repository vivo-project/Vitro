<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for property listing on individual profile page -->

<#import "lib-properties.ftl" as p>

<#list propertyGroups.all as group>
    <#assign groupname = group.name(nameForOtherGroup)>
    
    <section class="property-group" role="region">
        <nav class="scroll-up" role="navigation">
            <a href="#property-group-menu">
                <img src="${urls.images}/individual/scroll-up.png" alt="scroll to property group menus" />
            </a>
        </nav>
        
        <#-- Display the group heading --> 
        <#if groupname?has_content>
            <h2 id="${groupname}">${groupname?capitalize}</h2>
        </#if>
        
        <#-- List the properties in the group -->
        <#list group.properties as property>
            <article class="property" role="article">
                <#-- Property display name -->
                <h3 id="${property.localName}">${property.name} <@p.addLink property editable /></h3>
                <#-- List the statements for each property -->
                <ul class="property-list" role="list">
                    <#-- data property -->
                    <#if property.type == "data">
                        <@p.dataPropertyList property editable />
                    <#-- object property -->
                    <#elseif property.collatedBySubclass> <#-- collated -->
                        <@p.collatedObjectPropertyList property editable />
                    <#else> <#-- uncollated -->
                        <#-- We pass property.statements and property.template even though we are also
                             passing property, because objecctPropertyList can get other values, and
                             doesn't necessarily use property.statements and property.template -->
                        <@p.objectPropertyList property property.statements property.template editable />
                    </#if>
                </ul>
            </article> <!-- end property -->
        </#list>
    </section> <!-- end property-group -->
</#list>