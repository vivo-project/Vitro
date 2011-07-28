<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for the Revision Information page. -->

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/revision.css" />')}

<section role="region">
    <h2>Revision Information</h2>
    
    <section id="revision-levels" role="region">
        
        <table summary="VIVO revision's levels table">
            <caption>Levels:</caption>
            
            <tr>
                <th>name</th>
                <th>release</th>
                <th>revision</th>
            </tr>
            <#list revisionInfoBean.levelInfos as level>
                <tr>
                    <td>${level.name}</td>
                    <td>${level.release}</td>
                    <td>${level.revision}</td>
                </tr>
            </#list>
        </table>
    </section>
    
    <section id="revision-build-date" role="region">
        <h3>Build date:</h3>
    
        <p>${revisionInfoBean.buildDate?datetime?string.full}</p>
    </section>
</section>
