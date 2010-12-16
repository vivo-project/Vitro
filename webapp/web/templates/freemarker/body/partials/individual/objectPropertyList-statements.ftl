<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template to list statements for an object property -->
<ul class="object-property" role="list">
    <#list property.statements as statement>
        <li role="listitem">
            <#include "${property.template}">
        </li>
    </#list>
</ul>
