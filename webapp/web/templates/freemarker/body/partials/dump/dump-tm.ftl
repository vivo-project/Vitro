<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for dumping a template model object -->

<#if properties?has_content>
    <ul>
        <#list properties?keys as property>
            <li>${property}: ${properties[property]?html}</li>
        </#list>
    </ul>
</#if>

<#if methods?has_content>
    <ul>
        <#list methods as method>
            <li>${method}</li>
        </#list>
    </ul>
</#if>