<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Menu management page (uses individual display mechanism) -->

<#import "lib-properties.ftl" as p>

<#include "individual-setup.ftl">

<h3>Menu management</h3>

<#assign hasElement = propertyGroups.pullProperty("${namespaces.display}hasElement")>

<#-- List the menu items -->
<#list hasElement.statements as statement>
    <#-- can we just provide the name of the template? -->
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

<#-- Remove unneeded scripts and stylesheets -->
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual.css" />',
                  '<link rel="stylesheet" href="${urls.base}/css/individual/individual-vivo.css" />')}

${headScripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jquery.truncator.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/jquery_plugins/getURLParam.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/jquery_plugins/colorAnimations.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jquery.form.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/jquery_plugins/qtip/jquery.qtip-1.0.0-rc3.min.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/tiny_mce/tiny_mce.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/controls.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/toggle.js"></script>')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/imageUpload/imageUploadUtils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/individual/individualUtils.js"></script>')}