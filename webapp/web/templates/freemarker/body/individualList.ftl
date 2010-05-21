<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- List individual members of a class. -->

<div class="contents">

    <div class="individualList">
        <h2>${title}</h2>
        <#if subtitle??>
            <h4>${subTitle}"</h4>
        </#if>
        
        <#-- RY NEED TO ACCOUNT FOR p:process stuff -->
        <ul>
            <#list individuals as individual>
                <li>
                    <a href="${individual.profileUrl}">${individual.name}</a> ${individual.tagline}                  
                </li>
            </#list>
        </ul>
    </div>
</div>
