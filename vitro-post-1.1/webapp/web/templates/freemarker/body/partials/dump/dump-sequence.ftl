<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for dumping sequence (list and array) values -->

<strong>Type:</strong> sequence (array/list)<br />
<strong>Values:</strong><br />
<ul>
<#list value as item> 
    <#-- Shallow (non-recursive) dump  -->
    <li>${item_index}: ${item}</li> 
    <#-- Deep (recursive) dump 
    <li>${item_index}: <@dump var="${item}" /></li> -->
</#list>
</ul>