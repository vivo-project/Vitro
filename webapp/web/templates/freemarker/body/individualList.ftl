<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- List individual members of a class. -->

<#import "listMacros.ftl" as l>

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
                        <#-- The old JSP version uses a custom search view if one is defined, but it doesn't make sense
                        to do that in the current system, because we don't use the default search view. We could define
                        a custom list or use the custom short view, but for now just hard-code the view here; it will not be 
                        customizable.
                        <#include "${individual.searchView}">   -->
                        <a href="${individual.profileUrl}">${individual.name}</a>
                        <ul class="individualData">
                            <@l.firstLastList>
                                <#if individual.moniker??><li>${individual.moniker}</li>,</#if>
                                <#list individual.links as link>
                                    <li><a class="externalLink" href="${link.url}">${link.anchor}</a></li>,            
                                </#list>
                            </@l.firstLastList>
                        </ul>           
                    </li>
                </#list>
            </ul>
        </#if>
    </div>   
</div>
