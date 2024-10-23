<#-- $This file is distributed under the terms of the license in LICENSE$ -->
<#import "search-lib.ftl" as sl>

<script>
    let searchFormId = "filter-form";
</script>

<#if filterGenericInfo.filters[searchFilter]??>
    <section id="menupage-intro" role="region">
        <h2>${page.title}</h2>
    </section>
    <#assign additionalFilters = ["type"]>
    ${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/menupage.css" />')}
    <form id="filter-form" name="filter-form" autocomplete="off" method="get" action="${urls.currentPage}">
        <section id="noJavascriptContainer">
            <section id="browse-by" role="region">
                <nav role="navigation">
                    <ul id="browse-classes">
                        <#list additionalFilters as filterId>
                            <#if filterGenericInfo.filters[filterId]?? >
                                <#assign filter = filterGenericInfo.filters[filterId] >
                                <#if ( user.loggedIn || filter.public ) && !filter.hidden >
                                    <li class="filter-tab">
                                        <a href="#" <#if filter.selected> class="selected" </#if> >${filter.name?html}</a>
                                        <ul id="facet-values">
                                            <@collapsedFacets filter />
                                        </ul>
                                    </li>
                                </#if>
                            </#if>
                        </#list>
                        <div class="divider" style="border-top: 0.5rem solid #fff;margin: -2px;"></div>
                        <@filterFacets filterGenericInfo.filters[searchFilter] />
                    </ul>
                    <@alphabeticalIndexLinks />
                </nav>
                <section id="individuals-in-class" role="region">
                    <@printPagingLinks />
                    <@filteredResults />
                    <@printPagingLinks />
                </section>
            </section>
        </section>
    </form>
    
    <script type="text/javascript">
        $('.filter-tab > ul > li').not('.li-selected').hide();
        $('.filter-tab > a').click(function() {
            $(this).parent().find('ul > li').not('.li-selected').toggle();
        });
    </script>

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

<#macro collapsedFacets f>
        <#assign selectedValue = "" >
        <#assign valueNumber = 1>
        <#list f.values?values as value>
            <#if value.selected>
                <#assign selectedValue = value.id >
            </#if>
            <#assign valueLabel = value.name >
            <#if !(valueLabel?has_content)>
                <#assign valueLabel = value.id >
            </#if>
            <#if value.selected>
                <li id="${value.id?html}" class="li-selected">
                    <a href="#" class="selected">
                        <@sl.getInput f value sl.getValueID(f.id, valueNumber) valueNumber 'filter-form' />
                        <@sl.getSelectedLabel sl.getValueID(filter.id, valueNumber)?html value f getCurrentCount(f value) />
                    </a>
                </li>
            <#else>
                <li id="${value.id?html}">
                    <a href="#">
                        <@sl.getInput f value sl.getValueID(f.id, valueNumber) valueNumber 'filter-form' />
                        <@sl.getLabel sl.getValueID(f.id, valueNumber) value f getCurrentCount(f value) />
                    </a>
                </li>
            </#if>
            <#assign valueNumber = valueNumber + 1>
        </#list>
</#macro>

<#function getCurrentCount f v>
    <#if filters[f.id]??>
        <#assign filter = filters[f.id]>
        <#if filter.values[v.id]??>
            <#return filter.values[v.id].count >
        <#else>
            <#return 0 />
        </#if>
    <#else>
        <#return 0 />
    </#if>
</#function>

<#macro filterFacets f>
    <#assign selectedValue = "" >
    <#assign valueNumber = 1>
    <#list f.values?values as value>
        <#if value.selected>
            <#assign selectedValue = value.id >
        </#if>
        <#assign valueLabel = value.name >
        <#if !(valueLabel?has_content)>
            <#assign valueLabel = value.id >
        </#if>
        <#if value.selected>
            <li id="${value.id?html}" class="li-selected">
                <a href="#" class="selected">
                    <@sl.getInput f value sl.getValueID(f.id, valueNumber) valueNumber 'filter-form' />
                    <@sl.getSelectedLabel sl.getValueID(f.id, valueNumber)?html value f getCurrentCount(f value) />
                </a>
            </li>
        <#else>
            <li id="${value.id?html}">
                <a href="#">
                    <@sl.getInput f value sl.getValueID(f.id, valueNumber) valueNumber 'filter-form' />
                    <@sl.getLabel sl.getValueID(f.id, valueNumber) value f getCurrentCount(f value) />
                </a>
            </li>
        </#if>
        <#assign valueNumber = valueNumber + 1>
    </#list>
</#macro>

<#macro alphabeticalIndexLinks>
    <#if languageAware >
        <#assign indexFilterName = "label_regex">
    <#else>
        <#assign indexFilterName = "raw_label_regex">
    </#if>
    <#if filterGenericInfo.filters[indexFilterName]??>
        <#assign indexFilter = filterGenericInfo.filters[indexFilterName]>
        <nav id="alpha-browse-container" role="navigation">
            <ul id="alpha-browse-individuals">
            <li>
                 <a href="#" <#if indexFilter.inputText == ""> class="selected" </#if> >
                     <@getAlphabetInput indexFilter '' sl.getValueID(indexFilter.id, 0) />
                     <@getAlphabetLabel sl.getValueID(indexFilter.id, 0) i18n().all />
                 </a>
            </li>
            <#assign valueNumber = 1>
            <#list i18n().browse_results_alphabetical_index?split(",") as c>
                <#assign regexValue = "(" + c?lower_case?cap_first + "|" + c?lower_case + "|" + c?upper_case + ").*">
                <li>
                    <a href="#" <#if indexFilter.inputText == regexValue > class="selected" </#if> >
                        <@getAlphabetInput indexFilter regexValue sl.getValueID(indexFilter.id, valueNumber) />
                        <@getAlphabetLabel sl.getValueID(indexFilter.id, valueNumber) c?upper_case />
                    </a>
                </li>
                <#assign valueNumber = valueNumber + 1>
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

<#macro getAlphabetLabel valueId label>
    <label for="${valueId}" >${label?html}</label>
</#macro>
<#-- create radio input fields for alphabetical indexes -->
<#macro getAlphabetInput filter filterValue valueID form="filter-form">
    <#assign checked = "">
    <#if filter.inputText == filterValue>
        <#assign checked = " checked=\"checked\" " >
        <#assign class = "selected-input" >
    </#if>
    <#assign type = "radio" >
    <#assign filterName = filter.id >

    <input 
        form="${form}" 
        type="radio" 
        id="${valueID?html}" 
        value="${filter.id?html + ":"?html + filterValue?html}"
        name="filters_${filter.id?html}" 
        style="display:none;" 
        ${checked} 
    >
</#macro>

${scripts.add('<script type="text/javascript" src="${urls.base}/js/search/search_results.js"></script>')}
${stylesheets.add('<link rel="stylesheet" type="text/css" href="${urls.base}/css/search/custom_filters.css"/>')}


