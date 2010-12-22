<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for collated object property statement list -->

<p>Display of collated object property statements is in progress.</p>

<#list property.subclasses as subclass>
    <h3>${subclass}</h3>
    <@listStatements subclass.statements />
</#list>