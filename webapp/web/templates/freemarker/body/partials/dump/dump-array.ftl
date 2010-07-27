<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for dumping array values -->

<strong>Type:</strong> array<br />
<strong>Values:</strong><br />
<ul>
<#list value as item> 
    <li>${item_index}: ${item}</li>   
</#list>
</ul>