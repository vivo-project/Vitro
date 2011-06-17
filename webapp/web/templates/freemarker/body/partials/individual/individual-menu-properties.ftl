<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for menu management page -->

<#import "lib-properties.ftl" as p>

<#list propertyGroups.all as group>
    <#assign groupName = group.getName(nameForOtherGroup)>
    
        <#-- Display the group heading --> 
        <#if groupName?has_content>
            <h2 id="${groupName}">${groupName?capitalize}</h2>
        </#if>
        
        <#-- List the menu items(properties) in the group -->
        <#list group.properties as property>
            <#if property.localName == "hasElement">
                <#-- List menu Items -->
                <@p.objectProperty property editable property.template />
                <br /><#--remove break-->
                <@p.addLink property editable />             
            </#if>
        </#list>
</#list>