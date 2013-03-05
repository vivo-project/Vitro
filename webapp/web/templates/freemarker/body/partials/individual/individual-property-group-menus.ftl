<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for property listing on individual profile page -->

<#import "lib-properties.ftl" as p>
<#assign subjectUri = individual.controlPanelUrl()?split("=") >
<#assign nameForOtherGroup = nameForOtherGroup!"other">

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
                            <li role="listitem"><a href="#${groupnameHtmlId}" title="group name">${groupname?capitalize}</a></li>
                        </#if>
                    </#list>
                </ul>
            </nav>
        </#if> 
    </#if>
</#if>
<#list propertyGroups.all as group>
    <#assign groupName = group.getName(nameForOtherGroup)>
    <#assign verbose = (verbosePropertySwitch.currentValue)!false>
    
    <section class="property-group" role="region">
        <nav class="scroll-up" role="navigation">
            <a href="#branding" title="scroll up">
                <img src="${urls.images}/individual/scroll-up.gif" alt="scroll to property group menus" />
            </a>
        </nav>
        
        <#-- Display the group heading --> 
        <#if groupName?has_content>
    		<#--the function replaces spaces in the name with underscores, also called for the property group menu-->
        	<#assign groupNameHtmlId = p.createPropertyGroupHtmlId(groupName) >
            <h2 id="${groupNameHtmlId}">${groupName?capitalize}</h2>
        <#else>
            <h2 id="properties">Properties</h2>
        </#if>
        
        <#-- List the properties in the group -->
        <#include "individual-properties.ftl">
    </section> <!-- end property-group -->
</#list>
