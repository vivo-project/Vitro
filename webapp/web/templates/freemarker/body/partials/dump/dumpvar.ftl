<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Styles here are temporary; move to css file once stylesheets.add() works -->
<style>
div.dump.var {
    padding-top: .75em;
    padding-bottom: .75em;
    border-top: 1px solid #ccc;
    border-bottom: 1px solid #ccc; 
    margin-bottom: .5em;
}

div.dump.var p {
    margin-bottom: .5em;
}
</style>


<div class="dump var">
    <p><strong>Variable name:</strong> ${var.name}</p>
    <p><strong>Type:</strong> ${var.type}</p>
    
  
    <p><strong>Value:</strong> ${var.value}</p>
</div>

<#-- This will work after we move stylesheets to Configuration sharedVariables 
${stylesheets.add('<link rel="stylesheet" href="/css/fmdump.css">')}
-->