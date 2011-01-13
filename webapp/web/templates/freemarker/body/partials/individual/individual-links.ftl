<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for links on individual profile 

     Currently the page displays the vitro namespace links properties. Future versions 
     will use the vivo core ontology links property, eliminating the need for special handling.
-->

<nav role="navigation">
    <ul id ="individual-urls" role="list">
        <#list individual.links as link>                               
            <li role="listitem"><a href="${link.url}">${link.anchor}</a></li>                                 
        </#list>         
    </ul>
</nav>