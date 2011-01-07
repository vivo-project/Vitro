<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for rendering the classes in a class group for menupages -->

<nav role="navigation">
    <ul id="vgraph-childClasses" role="list">
    <#list vClassGroup as vClass>
        <li role="listitem"><a href="#browse-by" title="Browse all people in this class" data-uri="${vClass.URI}">${vClass.name} <span class="count-classes">(${vClass.entityCount})</span></a></li>
    </#list>
    </ul>
</nav>