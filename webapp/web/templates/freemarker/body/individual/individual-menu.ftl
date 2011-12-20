<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Menu management page (uses individual display mechanism) -->

<#include "individual-setup.ftl">

<#assign hasElement = propertyGroups.pullProperty("${namespaces.display}hasElement")!>

<#if hasElement?has_content>
    <script type="text/javascript">
        var menuItemData = [];
    </script>
    
    <h3>Menu management</h3>
    
    <#-- List the menu items -->
    <ul class="menuItems">
        <#list hasElement.statements as statement>
            <li class="menuItem"><#include "${hasElement.template}"> <span class="controls"><@p.editingLinks "hasElement" statement editable /></span></li>
        </#list>
    </ul>
    
    <#-- Link to add a new menu item -->
    <#if editable>
        <#assign addUrl = hasElement.addUrl>
        <#if addUrl?has_content>
            <p><a class="add-hasElement green button" href="${addUrl}" title="Add new menu item">Add menu item</a></p>
            
            <p class="note">Refresh page after reordering menu items</p>
        </#if>
    </#if>
    
    ${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual.css" />',
                      '<link rel="stylesheet" href="${urls.base}/css/individual/menuManagement-menuItems.css" />')}
                      
    ${headScripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>')}
    
    <#assign positionPredicate = "${namespaces.display}menuPosition" />
    
    <script type="text/javascript">
        // <#-- We need the controller to provide ${reorderUrl}. This is where ajax request will be sent on drag-n-drop events. -->
        var menuManagementData = {
            reorderUrl: '${reorderUrl}',
            positionPredicate: '${positionPredicate}'
        };
    </script>
    
    ${scripts.add('<script type="text/javascript" src="${urls.base}/js/individual/menuManagement.js"></script>')}
<#else>
    <p id="error-alert">There was an error in the system. The display:hasElement property could not be retrieved.</p>
</#if>