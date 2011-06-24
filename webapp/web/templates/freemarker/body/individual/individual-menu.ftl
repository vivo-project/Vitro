<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Menu management page (uses individual display mechanism) -->

<#include "individual-setup.ftl">

<h3>Menu management</h3>

<#assign hasElement = propertyGroups.pullProperty("${namespaces.display}hasElement")>

<#-- List the menu items -->
<#list hasElement.statements as statement>
    Position | <#include "${hasElement.template}"> | <@p.editingLinks "hasElement" statement editable /> <br />
</#list>

<br /> <#-- remove this once styles are applied -->

<#-- Link to add a new menu item -->
<#if editable>
    <#assign addUrl = hasElement.addUrl>
    <#if addUrl?has_content>
        <a class="add-hasElement green button" href="${addUrl}" title="Add new menu item">Add menu item</a>
    </#if>
</#if>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/individual/individualUtils.js"></script>')}