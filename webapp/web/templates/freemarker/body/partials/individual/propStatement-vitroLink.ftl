<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for vitro:primaryLink and vitro:additionalLink -->

<#--
<#if statement.url?? & statement.anchor??>
    <a href="${statement.url}">
        <span about="${individual.uri}" rel="${curie(statement.property)}" resource="${statement.link}">
            <span about="${statement.link}" property="vitro:linkURL" content="${statement.url}">
                <span class="link" about="${statement.link}" property="vitro:linkText">
                    ${statement.anchor}<#t>
                </span><#t>
            </span><#t>
        </span><#t>
    </a><#t>
<#elseif statement.url??>
    <a href="${statement.url}">
        <span about="${individual.uri}" rel="${curie(statement.property)}" resource="${statement.link}">
            <span class="link" about="${statement.link}" property="vitro:linkURL" content="${statement.url}">
                missing link anchor text<#t>
            </span><#t>
        </span><#t>
    </a><#t>
<#elseif statement.anchor??>
    <a href="${profileUrl(statement.link)}">
        <span about="${individual.uri}" rel="${curie(statement.property)}" resource="${statement.link}">
            <span class="link" about="${statement.link}" property="vitro:linkText">
                ${statement.anchor}<#t>
            </span><#t>
        </span><#t>
    </a><#t>
<#else>
    <a href="${profileUrl(statement.link)}">
        <span class="link" about="${individual.uri}" rel="${curie(statement.property)}" resource="${statement.link}">
            missing link information<#t>
        </span><#t>
    </a><#t>
</#if>
-->


<#if statement.url??>
    <a href="${statement.url}">
<#else>
    <a href="${profileUrl(statement.link)}">
</#if>
        <span about="${individual.uri}" rel="${curie(statement.property)}" resource="${statement.link}">
        <#if statement.url??>
            <span about="${statement.link}" property="vitro:linkURL" content="${statement.url}">
        </#if>
        <#if statement.anchor??>
            <span class="link" about="${statement.link}" property="vitro:linkText">
                ${statement.anchor}<#t>
            </span><#t>
        <#else> 
            missing link anchor text<#t>       
        </#if>
        <#if statement.url??>
            </span>
        </#if>
</a><#t>
