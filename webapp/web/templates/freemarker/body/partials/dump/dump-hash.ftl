<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for dumping hash values -->

<strong>Type:</strong> hash<br />
<strong>Values:</strong><br />

<ul>
    <#list value?keys as key> 
        <li>${key} = ${value[key]}</li>   
    </#list>
</ul>