<#-- $This file is distributed under the terms of the license in LICENSE$ -->
<#import "search-lib.ftl" as sl>

<#-- <#assign additionalFilters = ["type"]> -->
<#if filterGenericInfo.filters[searchFilter]??>

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
    ${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/menupage.css" />')}
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
                            <@filterFacets filterGenericInfo.filters[searchFilter] />
                        </#if>
                    </ul>
                    <@alphabeticalIndexLinks />
                </nav>
                <section id="individuals-in-class" role="region">
                    <@printPagingLinks />
                    <div id="browsing-options">
                        <@showSortOptions />
                        <@sl.showHits "filter-form" />
                        <img id="downloadIcon" alt="${i18n().download_results}" title="${i18n().download_results}" />
                    </div>
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

<#macro filterTab filterId>
    <#if filterGenericInfo.filters[filterId]?? >
        <#assign filter = filterGenericInfo.filters[filterId] >
        <#if filter.display >
            <#assign filterValues><@getValues filter filterId /></#assign>
            <#if filterValues?has_content>
                <li class="filter-tab">
                    <a href="#">${filter.name?html}</a>
                    ${filterValues}
                </li>
            </#if>
        </#if>
    </#if>
</#macro>

<#macro getValues filter filterId>
    <#if filter.type == "RangeFilter">
        <ul class="facet-values">
            <#if filters[filterId]?? && filters[filterId].selected>
                <li class="li-selected">
                    <a href="#" class="selected">
                        <@sl.userSelectedInput filters[filterId] "filter-form" />
                    </a>
                </li>
            </#if>
            <li <#if filter.selected || (filters[filterId]?? && filters[filterId].selected)> class="li-selected" </#if>>
                <@sl.rangeFilter filters[filterId] 'filter-form'/>
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

<#macro filterFacets f idStart=1 zeroCount=false>
    <#assign idCounter = idStart >
    <#list f.values?values as value>
        <#if !value.display>
            <#continue>
        </#if>
        <#assign valueLabel = value.name >
        <#assign resultsCount = getCurrentCount(f value) >
        <#if (resultsCount == 0) != zeroCount>
            <#continue>
        </#if>
        <#if !(valueLabel?has_content)>
            <#assign valueLabel = value.id >
        </#if>
        <#if value.selected>
            <li id="${value.id?html}" class="li-selected">
                <a href="#" class="selected">
                    <@sl.getInput f value sl.getValueID(f.id, idCounter) idCounter 'filter-form' />
                    <@sl.getSelectedLabel sl.getValueID(f.id, idCounter)?html value f resultsCount />
                </a>
            </li>
        <#else>
            <#if zeroCount=false>
                    <li id="${value.id?html}">
                    <a href="#">
                        <@sl.getInput f value sl.getValueID(f.id, idCounter) idCounter 'filter-form' />
                        <@sl.getLabel sl.getValueID(f.id, idCounter) value f resultsCount />
                    </a>
                </li>
            </#if>
        </#if>
        <#assign idCounter = idCounter + 1>
    </#list>
    <#if zeroCount=false>
        <@filterFacets f idCounter true />
    </#if>
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
            <#assign idCounter = 1>
            <#list i18n().browse_results_alphabetical_index?split(",") as c>
                <#assign regexValue = "(" + c?lower_case?cap_first + "|" + c?lower_case + "|" + c?upper_case + ").*">
                <li>
                    <a href="#" <#if indexFilter.inputText == regexValue > class="selected" </#if> >
                        <@getAlphabetInput indexFilter regexValue sl.getValueID(indexFilter.id, idCounter) />
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

<#macro showSortOptions>
    <#if sortOptions?has_content && sortOptionIds?? && sortOptionIds?is_sequence>
        <select form="filter-form" name="sort" id="filter-form-sort" onchange="this.form.submit()" >
            <#assign addDefaultOption = true>
            <#list sortOptionIds as sortOptionId>
                <#if sortOptions[sortOptionId]??>
                    <#assign option = sortOptions[sortOptionId]>
                    <#if option.display>
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

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/webjars/jquery-ui/jquery-ui.css" />',
                  '<link rel="stylesheet" type="text/css" href="${urls.base}/css/jquery_plugins/qtip/jquery.qtip.min.css" />')}

${headScripts.add('<script type="text/javascript" src="${urls.base}/webjars/jquery-ui/jquery-ui.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/jquery_plugins/qtip/jquery.qtip.min.js"></script>'
                  )}

