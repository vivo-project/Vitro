<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for class groups menu in menupages -->

<nav role="navigation">
    <ul id="vgraph-childClasses">
    <#list vClassGroup as vClass>
        <li><a href="#browse-by" title="Browse all people in this class" data-uri="${vClass.URI}">${vClass.name} <span class="count-classes">(${vClass.entityCount})</span></a></li>
    </#list>
    </ul>
</nav>