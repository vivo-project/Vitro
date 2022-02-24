<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template for property group menu on individual profile page nameForOtherGroup! -->

<#assign nameForOtherGroup = "${i18n().other}">

<#if (propertyGroups.all)??>
    <#assign groups = propertyGroups.all>

    <#if groups?has_content>
        <#if (groups?size > 1) || (groups?first).getName(nameForOtherGroup)?has_content>
            <nav id="property-group-menu" role="navigation">
                <ul role="list">
                    <#list groups as group>
                        <#assign groupname = group.getName(nameForOtherGroup)>
                        <#if groupname?has_content>
                    		<#--create property group html id is the function that will replace all spaces with underscore to make a valid id-->
                        	<#assign groupnameHtmlId = p.createPropertyGroupHtmlId(groupname) >
                            <#-- capitalize will capitalize each word in the name; cap_first only the first. We may need a custom
                            function to capitalize all except function words. -->
                            <li role="listitem"><a href="#${groupnameHtmlId}" title="${i18n().group_name}">${groupname?capitalize}</a></li>
                        </#if>
                    </#list>
                </ul>
            </nav>
        </#if>
    </#if>
</#if>
