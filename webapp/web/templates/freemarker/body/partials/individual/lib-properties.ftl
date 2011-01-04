<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

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

<#macro simpleObjectPropertyList property>
    <@objectPropertyList property.statements "propStatement-simple.ftl" />
</#macro>

<#macro objectPropertyList statements template>
    <#list statements as statement>
        <@propertyListItem><#include "${template}"></@propertyListItem>
    </#list>
</#macro>

<#macro propertyListItem>
    <li role="listitem"><#nested></li>
</#macro>