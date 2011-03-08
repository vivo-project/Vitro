<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for displaying directive describe -->

<div class="dump">
    <h3>Methods available to variable <em>${var}</em></h3>

    <#list methods as method>
        ${method}<br />
    </#list>
</div>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/dump.css" />')}