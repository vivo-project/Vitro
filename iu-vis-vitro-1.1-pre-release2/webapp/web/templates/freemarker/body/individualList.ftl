<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- List individual members of a class. -->

<div class="contents">
    <div class="individualList">
        <h2>${title}</h2>
        <#if subtitle??>
            <h4>${subtitle}</h4>
        </#if>
        
        <#if message??>
            <p>${message}</p>
        <#else>
            <ul>
                <#list individuals as individual>                   
                    <li>
                        <#-- Currently we just use the search view here; there's no custom list view defined. -->
                        <#include "${individual.searchView}">              
                    </li>
                </#list>
            </ul>
        </#if>
    </div>   
</div>
