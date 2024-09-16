<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#if filterGenericInfo.filters[searchFilter]??>
    <#assign f = filterGenericInfo.filters[searchFilter]>
    <section id="menupage-intro" role="region">
        <h2>${page.title}</h2>
    </section>

    ${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/menupage.css" />')}

    <section id="noJavascriptContainer">
        <section id="browse-by" role="region">
            <nav role="navigation">
                <@filterFacets />
                <@alphabeticalIndexLinks />
            </nav>
            <section id="individuals-in-class" role="region">
                <@printPagingLinks />
                <@filteredResults />
                <@printPagingLinks />
            </section>
        </section>
    </section>
</#if>

<#macro filteredResults>
    <ul role="list">
        <#if individuals??>
            <#list individuals as individual>
                <li class="individual" role="listitem">
                    <@shortView uri=individual.uri viewContext="index" />
                </li>
            </#list>
        </#if>
    </ul>
</#macro>


<#macro filterFacets>
    <ul id="browse-classes">
        <#assign selectedValue = "" >
        <#list f.values?values as value>
            <#if value.selected>
                <#assign selectedValue = value.id >
            </#if>
            <#assign valueLabel = value.name >
            <#if !(valueLabel?has_content)>
                <#assign valueLabel = value.id >
            </#if>
            <li id="${value.id?html}">
                <a href="${urls.currentPage}?filters_main=${searchFilter?url + ":"?url + value.id?url}" data-uri="${value.id?html}" <#if value.selected> class="selected" </#if> >${valueLabel?html}<span class="count-classes">(${value.count})</span>
                </a>
            </li>
        </#list>
    </ul>
</#macro>


<#macro alphabeticalIndexLinks>
    <#if languageAware >
        <#assign indexFilterName = "label_regex">
    <#else>
        <#assign indexFilterName = "raw_label_regex">
    </#if>
    <#if filters[indexFilterName]??>
        <#assign indexFilter = filters[indexFilterName]>
        <nav id="alpha-browse-container" role="navigation">
            <ul id="alpha-browse-individuals">
            <li><a 
            <#if indexFilter.inputText == "">
                 class="selected" 
             </#if>
            href="${urls.currentPage}?filters_main=${searchFilter?url + ":"?url + selectedValue?url}" title="select all">${i18n().all}</a></li>
            <#list i18n().browse_results_alphabetical_index?split(",") as c>
                <#assign regexValue = "[" + c?lower_case + c?upper_case + "].*">
                <#assign regexEncodedValue = "%5B" + c?lower_case?url + c?upper_case?url + "%5D.*">
                <li><a 
                    <#if indexFilter.inputText == regexValue >
                        class="selected" 
                    </#if>
                    href="${urls.currentPage}?filters_main=${searchFilter?url + ":"?url + selectedValue?url}&filter_input_${indexFilterName}=${regexEncodedValue}" title="${i18n().browse_all_starts_with(c?upper_case)}">${c?upper_case}</a>
                </li>
            </#list>
            </ul>
        </nav>
    </#if>
</#macro>

<#macro printPagingLinks>
    <#if (pagingLinks?? && pagingLinks?size > 0)>
        <div class="pagination menupage">
            ${i18n().pages}:
            <ul>
            <#list pagingLinks as link>
                <#if link.url??>
                    <li class="round"><a href="${link.url?html}" title="${i18n().page_link}">${link.text?html}</a></li>
                <#else>
                    <li class="round selected"><span>${link.text?html}</span></li> <#-- no link if current page -->
                </#if>
            </#list>
            </ul>
        </div>
    </#if>
</#macro>
