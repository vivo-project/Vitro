<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template to list statements for a data property -->

<#list property.statements as statement>
    <li role="listitem">${statement.value}</li>
</#list>