<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for property group menu on individual profile page -->

<#assign nameForOtherGroup = nameForOtherGroup!"other">
<nav id="property-group-menus" role="navigation">
    <ul role="list">
        <#list propertyGroups as group>
            <#assign groupname = group.name(nameForOtherGroup)>
            <#if groupname?has_content>
                <#-- capitalize will capitalize each word in the name; cap_first only the first. We may need a custom
                function to capitalize all except function words. -->
                <li role="listitem"><a href="#${groupname}">${groupname?capitalize}</a></li>
            </#if>
        </#list>
    </ul>
</nav>