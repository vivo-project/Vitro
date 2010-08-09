<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for displaying directive describe -->

<h3>Methods available to variable <em>${var}</em></h3>
    
<div class="dump">
    <#list methods as method>
        ${method}<br />
    </#list>
</div>

${stylesheets.add("/css/dump.css")}