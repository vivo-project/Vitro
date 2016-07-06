<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for property listing on individual profile page -->

<#import "lib-properties.ftl" as p>
<#assign subjectUri = individual.controlPanelUrl()?split("=") >

<#if (propertyGroups.all)??>
    <#assign groups = propertyGroups.all>   
    <#if groups?has_content>
        <#if (groups?size > 1) || (groups?first).getName(nameForOtherGroup)?has_content> 
            <nav id="property-group-menu" role="navigation">
                <ul role="list">
                    <#list groups as group>
                      <#if (group.properties?size > 0) >
                        <#assign groupname = group.getName(nameForOtherGroup)>                        
                        <#if groupname?has_content>
                    		<#--create property group html id is the function that will replace all spaces with underscore to make a valid id-->
                        	<#assign groupnameHtmlId = p.createPropertyGroupHtmlId(groupname) >
                            <#-- capitalize will capitalize each word in the name; cap_first only the first. We may need a custom
                            function to capitalize all except function words. -->
                            <li role="listitem"><a href="#${groupnameHtmlId}" title="${i18n().group_name}">${groupname?capitalize}</a></li>
                        </#if>
                      </#if>
                    </#list>
                </ul>
            </nav>
        </#if>
      </#if>
</#if>
<#list propertyGroups.all as group>
  <#if (group.properties?size > 0)>
    <#assign groupName = group.getName(nameForOtherGroup)>
    <#assign verbose = (verbosePropertySwitch.currentValue)!false>
    
    <section class="property-group" role="region">
        <nav class="scroll-up" role="navigation">
            <a href="#branding" title="${i18n().scroll_to_menus}">
                <img src="${urls.images}/individual/scroll-up.gif" alt="${i18n().scroll_to_menus}" />
            </a>
        </nav>
        
        <#-- Display the group heading --> 
        <#if groupName?has_content>
    		<#--the function replaces spaces in the name with underscores, also called for the property group menu-->
        	<#assign groupNameHtmlId = p.createPropertyGroupHtmlId(groupName) >
            <h2 id="${groupNameHtmlId}">${groupName?capitalize}</h2>
        <#else>
            <h2 id="properties">${i18n().properties_capitalized}</h2>
        </#if>
        
        <#-- List the properties in the group -->
        <#include "individual-properties.ftl">
    </section> <!-- end property-group -->
  </#if>
</#list>
