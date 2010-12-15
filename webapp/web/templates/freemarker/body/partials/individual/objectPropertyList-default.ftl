<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Default object property list template -->

<#list property.statements as statement>
    <li role="listitem"><a href="${statement.object.url}">${statement.object.name}</a> | ${statement.object.moniker!}</li>
</#list>
