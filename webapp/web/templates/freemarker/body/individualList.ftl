<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- List individual members of a class. -->

<div class="contents">

    <div class="entityList">
        <h2>${title}</h2>
        <#if subtitle??>
            <h4>${subTitle}"</h4>
        </#if>
        
        <ul>
            <#list entities as entity>
                <#-- Iterate through the object's class hierarchy, looking for a custom view -->
                <#-- RY This should be done in a view method -->
                <#list entity.VClasses as type>
                    <#if type.customSearchView?has_content>
                        <#assign altRender = type.customSearchView>
                        <#-- What order are these returned in? If from specific to general, we should break if we find one -->
                    </#if>
                </#list>
                <li>
                    <#-- RY Create a directive to work like c:url with c:param -->
                    <#-- RY The JSP version includes URL rewriting in a filter - see URLRewritingHttpServletResponse -->
                    <a href="${entityUrl}${entity.URI?url}">${entity.name}</a> | <#-- add p:process to name -->
                    <#if entity.moniker?has_content>
                        ${entity.moniker} <#-- add p:process -->
                    <#else>
                        ${entity.VClass.name}
                    </#if>
                    
                </li>
            </#list>
        </ul>
    </div>
</div>