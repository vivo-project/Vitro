<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template for property listing on individual profile page -->

<#import "lib-properties.ftl" as p>
<#assign subjectUri = individual.controlPanelUrl()?split("=") >
<#assign tabCount = 1 >
<#assign sectionCount = 1 >
<!-- ${propertyGroups.all?size} -->
<ul class="propertyTabsList nav nav-tabs">
    <li  class="groupTabSpacer">&nbsp;</li>

<#list propertyGroups.all as groupTabs>
    <#if ( groupTabs.properties?size > 0 ) >
        <#assign groupName = groupTabs.getName(nameForOtherGroup)>
        <#if groupName?has_content>
		    <#--the function replaces spaces in the name with underscores, also called for the property group menu-->
    	    <#assign groupNameHtmlId = p.createPropertyGroupHtmlId(groupName) >
        <#else>
            <#assign groupName = "${i18n().properties_capitalized}">
    	    <#assign groupNameHtmlId = "${i18n().properties}" >
        </#if>
        <#if tabCount = 1 >
            <li class="nav-link active" data-bs-toggle="tab" data-bs-target="#${groupNameHtmlId?replace("/","-")}" type="button" role="tab" id="${groupNameHtmlId?replace("/","-")}-tab" aria-controls="${groupNameHtmlId?replace("/","-")}" aria-selected="true" groupName="${groupNameHtmlId?replace("/","-")}">${p.capitalizeGroupName(groupName)}</li>
            <li class="groupTabSpacer">&nbsp;</li>
            <#assign tabCount = 2>
        <#else>
            <li class="nav-link" data-bs-toggle="tab" data-bs-target="#${groupNameHtmlId?replace("/","-")}" type="button" role="tab" id="${groupNameHtmlId?replace("/","-")}-tab" aria-controls="${groupNameHtmlId?replace("/","-")}" aria-selected="false" groupName="${groupNameHtmlId?replace("/","-")}">${p.capitalizeGroupName(groupName)}</li>
            <li class="groupTabSpacer">&nbsp;</li>
        </#if>
    </#if>
</#list>

<#if (propertyGroups.all?size > 1) >
    <li class="nav-link" id="show-all-tabs" groupName="viewAll" data-bs-toggle="tab" type="button" role="tab" aria-selected="false">${i18n().view_all_capitalized}</li>
    <li class="groupTabSpacer">&nbsp;</li>
</#if>
</ul>

<div class="tab-content property-tabs">
<#assign tabCount = 1 >

<#list propertyGroups.all as group>
    <#if (group.properties?size > 0)>
        <#assign groupName = group.getName(nameForOtherGroup)>
        <#assign groupNameHtmlId = p.createPropertyGroupHtmlId(groupName) >
        <#assign verbose = (verbosePropertySwitch.currentValue)!false>
        
        <#if tabCount = 1 >
            <section id="${groupNameHtmlId?replace("/","-")}" class="tab-pane fade show active property-group" role="tabpanel" aria-labelledby="${groupNameHtmlId?replace("/","-")}-tab" role="region">
            <#assign tabCount = 2>
        <#else>
            <section id="${groupNameHtmlId?replace("/","-")}" class="tab-pane fade property-group" role="tabpanel" aria-labelledby="${groupNameHtmlId?replace("/","-")}-tab" role="region">
        </#if>

        <nav id="scroller" class="scroll-up hidden" role="navigation">
            <a href="#branding" title="${i18n().scroll_to_menus}" >
                <img src="${urls.images}/individual/scroll-up.gif" alt="${i18n().scroll_to_menus}" />
            </a>
        </nav>

        <#-- Display the group heading -->
        <#if groupName?has_content>
		    <#--the function replaces spaces in the name with underscores, also called for the property group menu-->
    	    <#assign groupNameHtmlId = p.createPropertyGroupHtmlId(groupName) >
            <h2 id="${groupNameHtmlId?replace("/","-")}" pgroup="tabs" class="hidden">${p.capitalizeGroupName(groupName)}</h2>
        <#else>
            <h2 id="properties" pgroup="tabs" class="hidden">${i18n().properties_capitalized}</h2>
        </#if>
        <div id="${groupNameHtmlId?replace("/","-")}Group" >
            <#-- List the properties in the group   -->
            <#include "individual-properties.ftl">
        </div>
        </section> <!-- end property-group -->
        <#assign sectionCount = 2 >
    </#if>
</#list>
</div>
<script>
    var individualLocalName = "${individual.localName}";
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual-property-groups.css" />')}
${headScripts.add('<script type="text/javascript" src="${urls.base}/js/amplify/amplify.store.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/individual/propertyGroupControls.js"></script>')}

