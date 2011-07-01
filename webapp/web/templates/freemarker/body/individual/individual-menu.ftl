<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Menu management page (uses individual display mechanism) -->

<#include "individual-setup.ftl">

<script type="text/javascript">
    var menuItemData = [];
</script>

<h3>Menu management</h3>

<#assign hasElement = propertyGroups.pullProperty("${namespaces.display}hasElement")>

<#-- List the menu items -->
<ul class="menuItems">
    <#list hasElement.statements as statement>
        <li class="menuItem"><#include "${hasElement.template}"> <span class="controls"><@p.editingLinks "hasElement" statement editable /></a></li>
    </#list>
</ul>

<#-- Link to add a new menu item -->
<#if editable>
    <#assign addUrl = hasElement.addUrl>
    <#if addUrl?has_content>
        <a class="add-hasElement green button" href="${addUrl}" title="Add new menu item">Add menu item</a>
    </#if>
</#if>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual.css" />',
                  '<link rel="stylesheet" href="${urls.base}/css/individual/menuManagement.css" />')}

${headScripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>')}

<#assign positionPredicate = "${namespaces.display}menuPosition" />

<script type="text/javascript">
    // <#-- We need the controller to provide ${reorderUrl}. This is where ajax request will be sent on drag-n-drop events. -->
    var menuManagementData = {
        // <#-- reorderUrl: '${reorderUrl}', -->
        positionPredicate: '${positionPredicate}'
    };
</script>

<#-- Since the individual page can currently be viewed by anonymous users, only invoke sortable if logged in for now
     Jim is working on this (see NIHVIVO-2749) -->
<#if editable>
    ${scripts.add('<script type="text/javascript" src="${urls.base}/js/individual/menuManagement.js"></script>')}
</#if>