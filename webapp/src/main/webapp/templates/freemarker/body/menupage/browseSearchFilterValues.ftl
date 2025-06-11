<#-- $This file is distributed under the terms of the license in LICENSE$ -->
<#import "search-lib.ftl" as sl>

<#-- <#assign additionalFilters = ["type"]> -->
<#if filters[searchFilter]??>
    <#if languageAware >
        <#assign indexFilterName = "initial">
    <#else>
        <#assign indexFilterName = "raw_initial">
    </#if>
    <script>
        let searchFormId = "filter-form";
        let urlsBase = "${urls.base}";
        if (window.location.toString().indexOf("?") == -1){
            var queryText = 'querytext=${querytext?js_string}';
        } else {
            var queryText = window.location.toString().split("?")[1];
        }
        //If main filter is not set, then set it to default value [* TO *]
        if (!queryText.includes('filters_${searchFilter}')) {
            queryText += "&filters_${searchFilter}=${searchFilter}:${"[* TO *]"?url}";
        }
    </script>

    <section id="menupage-intro" role="region">
        <h2>${page.title}</h2>
    </section>
    <form id="filter-form" name="filter-form" autocomplete="off" method="get" action="${urls.currentPage}">
        <section id="noJavascriptContainer">
            <section id="browse-by" role="region">
                <nav role="navigation">
                    <ul id="browse-filters">
                        <#if additionalFilters?has_content && additionalFilters?is_sequence >
                            <#list additionalFilters as filterId>
                                <@filterTab filterId />
                            </#list>
                            <@filterTab searchFilter />
                        <#else>
                            <@filterFacets filters[searchFilter] />
                        </#if>
                    </ul>
                </nav>
                <section id="individuals-in-class" role="region">
                    <div id="browsing-options">
                        <button type="button" id="downloadIcon" class="download-results-text-button">${i18n().download_results}</button>
                        <@sl.showHits "filter-form" />
                        <@showSortOptions />
                    </div>
                    <@alphabeticalIndexLinks indexFilterName/>
                    <@printPagingLinks />
                    <@filteredResults indexFilterName />
                    <@printPagingLinks />
                </section>
            </section>
        </section>
        <input form="filter-form" type="hidden" name="lang" value="${locale!}">
    </form>
    
    <script type="text/javascript">
        $('.filter-tab > ul > li').not('.li-selected').addClass('hidden-all-search-options');
        $('.filter-tab > a').on( "click", function() {
            $(this).parent().find('ul > li').not('.li-selected').toggleClass('hidden-all-search-options');
        });
    </script>

</#if>

<#macro filterTab filterId>
    <#if filters[filterId]?? >
        <#assign filter = filters[filterId] >
        <#if filter.displayed>
            <#assign filterValues><@getValues filter /></#assign>
            <#if filterValues?has_content>
                <li class="filter-tab">
                    <a href="#">${filter.name?html}</a>
                    ${filterValues}
                </li>
            </#if>
        </#if>
    </#if>
</#macro>

<#macro getValues filter>
    <#if filter.type == "RangeFilter">
        <ul class="facet-values">
            <#if filter.selected>
                <li class="li-selected">
                    <a href="#" class="selected">
                        <@sl.userSelectedInput filter "filter-form" />
                    </a>
                </li>
            </#if>
            <li <#if filter.selected> class="li-selected" </#if>>
                <@sl.rangeFilter filter 'filter-form'/>
            </li>
        </ul>
    <#else>
        <#assign facets><@filterFacets filter /></#assign>
        <#if facets?has_content>
            <ul class="facet-values">
                ${facets}
            </ul>
        </#if>
    </#if>
</#macro>

<#macro filteredResults indexFilterName>
    <ul role="list">
        <#if individuals?has_content>
            <#list individuals as individual>
                <@shortView uri=individual.uri viewContext="browse" />
            </#list>
        <#elseif filters[indexFilterName]?? && filters[indexFilterName].inputText?has_content>
            <#assign selectedLetter = filters[indexFilterName].inputText >
            <li>
                <p class="no-individuals">${i18n().there_are_no_entries_starting_with} <i>${selectedLetter?upper_case}</i>.</p>
                <p class="no-individuals">${i18n().try_another_letter}</p>
            </li>
        <#else>
            <li><p class="no-individuals">${i18n().there_are_no_results_to_display}</p></li>
        </#if>
    </ul>
</#macro>

<#macro filterFacets filter >
    <#if ( !filter.localizationRequired && filter.values?values?size > filter.moreLimit) >
        <li>
            <@sl.createAutocomplete filter "filter-form" />
        </li>
    </#if>
    <#assign idCounter = 1 >
    <#assign notSelectedCount = 0>
    <#assign additionalLabels = false>
    <#list filter.values?values as value>
        <#if !value.displayed>
            <#continue>
        </#if>
        <#assign valueLabel = value.name >
        <#assign resultsCount = value.count >
        <#if !(valueLabel?has_content)>
            <#assign valueLabel = value.id >
        </#if>
        <#if value.selected>
            <#if value.id != "[* TO *]">
                <li class="li-selected">
                    <a href="#" class="selected">
                        <@sl.getInput filter value sl.getValueID(filter.id, idCounter) idCounter 'filter-form' />
                        <@sl.getLabel sl.getValueID(filter.id, idCounter)?html value filter resultsCount />
                    </a>
                </li>
            </#if>
        <#else>
            <#if resultsCount != 0>
                <#if filter.moreLimit = notSelectedCount >
                    <#assign additionalLabels = true>
                    <li class="more-facets-link" href="javascript:void(0);" onclick="expandSearchOptions(this)">${i18n().paging_link_more}</li>
                </#if>
                <li <#if additionalLabels>class="additional-search-options hidden-search-option"</#if> >
                    <a href="#">
                        <@sl.getInput filter value sl.getValueID(filter.id, idCounter) idCounter 'filter-form' />
                        <@sl.getLabel sl.getValueID(filter.id, idCounter) value filter resultsCount />
                    </a>
                </li>
                <#assign notSelectedCount += 1>
            </#if>
        </#if>
        <#assign idCounter = idCounter + 1>
    </#list>
    <#if additionalLabels >
        <li class="less-facets-link additional-search-options hidden-search-option" href="javascript:void(0);" onclick="collapseSearchOptions(this)">${i18n().display_less}</li>
    </#if>
</#macro>

<#macro alphabeticalIndexLinks indexFilterName>
    <#if filters[indexFilterName]??>
        <#assign indexFilter = filters[indexFilterName]>
        <nav id="alphabetical-index-container" role="navigation">
            <ul id="alphabetical-index-individuals">
            <li>
                 <a href="#" <#if indexFilter.inputText == ""> class="selected" </#if> >
                     <@getAlphabetInput indexFilter '' sl.getValueID(indexFilter.id, 0) />
                     <@getAlphabetLabel sl.getValueID(indexFilter.id, 0) i18n().all />
                 </a>
            </li>
            <#assign idCounter = 1>
            <#list i18n().browse_results_alphabetical_index?split(",") as c>
                <li>
                    <a href="#" <#if indexFilter.inputText == c > class="selected" </#if> >
                        <@getAlphabetInput indexFilter c sl.getValueID(indexFilter.id, idCounter) />
                        <@getAlphabetLabel sl.getValueID(indexFilter.id, idCounter) c?upper_case />
                    </a>
                </li>
                <#assign idCounter = idCounter + 1>
            </#list>
            </ul>
        </nav>
    </#if>
</#macro>

<#macro printPagingLinks>
    <#if (pagingLinks?? && pagingLinks?size > 0)>
        <div class="pagination-container">
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

<#macro showSortOptions>
    <#if !sortOptionIds??>
        <#assign sortOptionIds = ["titledesc", "titleasc"]>
    </#if>
    <#if sortOptions?has_content && sortOptionIds?? && sortOptionIds?is_sequence>
        <select form="filter-form" name="sort" id="filter-form-sort" onchange="this.form.submit()" >
            <#assign addDefaultOption = true>
            <#list sortOptionIds as sortOptionId>
                <#if sortOptions[sortOptionId]??>
                    <#assign option = sortOptions[sortOptionId]>
                    <#if option.displayed>
                        <#if option.selected>
                            <option value="${option.id}" selected="selected">${i18n().search_results_sort_by(option.label)}</option>
                            <#assign addDefaultOption = false>
                        <#else>
                            <option value="${option.id}" >${i18n().search_results_sort_by(option.label)}</option>
                        </#if>
                    </#if>
                </#if>
            </#list>
            <#if addDefaultOption>
                <option disabled selected value="" style="display:none">${i18n().search_results_sort_by('')}</option>
            </#if>
        </select>
    </#if>
</#macro>

${scripts.add('<script type="text/javascript" src="${urls.base}/js/search/search_results.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/searchDownload.js"></script>')}
${stylesheets.add('<link rel="stylesheet" type="text/css" href="${urls.base}/css/search/custom_filters.css"/>')}
${stylesheets.add('<link rel="stylesheet" type="text/css" href="${urls.base}/css/nouislider.css"/>')}
${headScripts.add('<script type="text/javascript" src="${urls.base}/js/nouislider.min.js"></script>')}
${headScripts.add('<script type="text/javascript" src="${urls.base}/js/wNumb.min.js"></script>')}
${headScripts.add('<script type="text/javascript" src="${urls.base}/webjars/floatingui/floating-ui.core.umd.js"></script>')}
${headScripts.add('<script type="text/javascript" src="${urls.base}/webjars/floatingui/floating-ui.dom.umd.js"></script>')}
${headScripts.add('<script type="text/javascript" src="${urls.base}/js/tooltip/tooltip-utils.js"></script>')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/webjars/jquery-ui/jquery-ui.css" />')}

${headScripts.add('<script type="text/javascript" src="${urls.base}/webjars/jquery-ui/jquery-ui.js"></script>')}

