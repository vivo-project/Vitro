<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for the main Site Administration page -->

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/admin.css" />')}

<div class="tab">
    <h2>Site Administration</h2>
</div>

<div id="adminDashboard">
    <#include "siteAdmin-dataInput.ftl">
    <#include "siteAdmin-siteConfiguration.ftl">
    <#include "siteAdmin-ontologyEditor.ftl">
    <#include "siteAdmin-advancedDataTools.ftl">
    <#include "siteAdmin-indexCacheRebuild.ftl">
</div>