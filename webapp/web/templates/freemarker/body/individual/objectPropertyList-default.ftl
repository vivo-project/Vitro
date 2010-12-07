<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Default object property list template -->

<#list property.statements as statement>
    <div class="obj-prop-stmt-obj">
        <#-- ${statement.object.name} -->
        statement ${statement_index +1}
    </div> <!-- end obj-prop-stmt-obj -->
</#list>