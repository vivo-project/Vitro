<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for rendering the classes in a class group for menupages -->

<nav role="navigation">
    <ul id="vgraph-classes" role="list">
    <#list vClassGroup as vClass>
        <#-- Only display vClasses with individuals -->
        <#if (vClass.entityCount > 0)>
            <#-- Calculate the individual count for the group since it's not currently provided to menu page templates -->
            <#if !classGroupIndividualCount??>
                <#assign classGroupIndividualCount = vClass.entityCount />
            <#else>
                <#assign classGroupIndividualCount = classGroupIndividualCount + vClass.entityCount />
            </#if>
            <li role="listitem"><a href="#browse-by" title="Browse all individuals in this class" data-uri="${vClass.URI}">${vClass.name} <span class="count-classes">(${vClass.entityCount})</span></a></li>
        </#if>
    </#list>
    </ul>
</nav>