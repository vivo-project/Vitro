<#-- $This file is distributed under the terms of the license in LICENSE$ -->

${stylesheets.add('<link rel="stylesheet" type="text/css" href="${urls.base}/css/nouislider.css"/>')}
${stylesheets.add('<link rel="stylesheet" type="text/css" href="${urls.base}/css/search-results.css"/>')}

${headScripts.add('<script type="text/javascript" src="${urls.base}/webjars/floatingui/floating-ui.core.umd.js"></script>')}
${headScripts.add('<script type="text/javascript" src="${urls.base}/webjars/floatingui/floating-ui.dom.umd.js"></script>')}
${headScripts.add('<script type="text/javascript" src="${urls.base}/js/tooltip/tooltip-utils.js"></script>')}

${headScripts.add('<script type="text/javascript" src="${urls.base}/js/nouislider.min.js"></script>')}
${headScripts.add('<script type="text/javascript" src="${urls.base}/js/wNumb.min.js"></script>')}

<#include "search-lib.ftl">

<script>
	let searchFormId = "search-form";
	let urlsBase = "${urls.base}";
	if (window.location.toString().indexOf("?") == -1){
		var queryText = 'querytext=${querytext?js_string}';
	} else {
		var queryText = window.location.toString().split("?")[1];
	}
</script>

<@searchForm  />

<div class="contentsBrowseGroup">

    <@printPagingLinks />
    <#-- Search results -->
    <ul class="searchhits">
        <#list individuals as individual>
            <li>
                <@shortView uri=individual.uri viewContext="search" />
            </li>
        </#list>
    </ul>
    <input form="search-form" type="hidden" name="lang" value="${locale!}">

    <@printPagingLinks />
    <br />
</div> 

<#macro printPagingLinks>

<#-- Paging controls -->
    <#if (pagingLinks?size > 0)>
        <div class="searchpages">
            ${i18n().pages}:
            <#if prevPage??><a class="prev" href="${prevPage?html}" title="${i18n().previous}">${i18n().previous}</a></#if>
            <#list pagingLinks as link>
                <#if link.url??>
                    <a href="${link.url?html}" title="${i18n().page_link}">${link.text?html}</a>
                <#else>
                    <span>${link.text?html}</span> <#-- no link if current page -->
                </#if>
            </#list>
            <#if nextPage??><a class="next" href="${nextPage?html}" title="${i18n().next_capitalized}">${i18n().next_capitalized}</a></#if>
        </div>
    </#if>
</#macro>

<#macro printResultNumbers>
    <h2 class="searchResultsHeader">
        <#escape x as x?html>
            ${i18n().results_found(hitCount)} 
        </#escape>
        <img id="downloadIcon" src="images/download-icon.png" alt="${i18n().download_results}" title="${i18n().download_results}" />
    </h2>
</#macro>

<#macro searchForm>
        <div id="selected-filters">
            <@printSelectedFilterValueLabels filters />
        </div>  
        <div id="filter-groups">
            <ul class="nav nav-tabs">
                <#assign active = true>
                <#list filterGroups as group>
                    <#if ( user.loggedIn || group.public ) && !group.hidden >
                        <@searchFormGroupTab group active/>
                        <#assign active = false>
                    </#if>  
                </#list>
            </ul>
            <#assign active = true>
            <#list filterGroups as group>
                <#if ( user.loggedIn || group.public ) && !group.hidden >
                      <@groupFilters group active/>
                      <#assign active = false>
                </#if>
            </#list>
        </div>
        <div id="search-form-footer">
            <div>
                <@printResultNumbers />
            </div>
            <div>
                <@printHits />
                <@printSorting />
            </div>
        </div> 
</#macro>

<#macro groupFilters group active>
    <#if active >
        <div id="${group.id}" class="tab-pane fade in active filter-area">
    <#else>
        <div id="${group.id}" class="tab-pane fade filter-area">
    </#if>
            <div id="search-filter-group-container-${group.id}">
                <ul class="nav nav-tabs">
                    <#assign assignedActive = false>
                    <#list group.filters as filterId>
                        <#if filters[filterId]??>
                            <#assign f = filters[filterId]>
                            <#if ( user.loggedIn || f.public ) && !f.hidden >
                                <@searchFormFilterTab f assignedActive/>  
                                <#if !assignedActive && (f.selected || emptySearch )>
                                    <#assign assignedActive = true>
                                </#if>
                            </#if>
                        </#if>
                    </#list>
                </ul>
            </div>
            <div id="search-filter-group-tab-content-${group.id}" class="tab-content">
                <#assign assignedActive = false>
                <#list group.filters as filterId>
                    <#if filters[filterId]??>
                        <#assign f = filters[filterId]>
                        <#if ( user.loggedIn || f.public ) && !f.hidden >
                            <@printFilterValues f assignedActive emptySearch/>  
                            <#if !assignedActive && ( f.selected || emptySearch )>
                                <#assign assignedActive = true>
                            </#if>
                        </#if>
                    </#if>
                </#list>
            </div>
        </div>
</#macro>

<#macro printSelectedFilterValueLabels filters>
    <#list filters?values as filter>
        <#assign valueNumber = 1>
        <#list filter.values?values as v>
            <#if v.selected>
                <@getInput filter v getValueID(filter.id, valueNumber) valueNumber />
                <#if user.loggedIn || filter.public>
                    <@getSelectedLabel valueNumber, v, filter />
                </#if>
            </#if>
            <#assign valueNumber = valueNumber + 1>
        </#list>
        <@userSelectedInput filter />
    </#list>
</#macro>

<#macro printSorting>
    <#if sorting?has_content>
        <div>
            <select form="search-form" name="sort" id="search-form-sort" onchange="this.form.submit()" >
                <#assign addDefaultOption = true>
                <#list sorting as option>
                    <#if option.display>
                        <#if option.selected>
                            <option value="${option.id}" selected="selected">${i18n().search_results_sort_by(option.label)}</option>
                            <#assign addDefaultOption = false>
                        <#else>
                            <option value="${option.id}" >${i18n().search_results_sort_by(option.label)}</option>
                        </#if>
                    </#if>
                </#list>
                <#if addDefaultOption>
                    <option disabled selected value="" style="display:none">${i18n().search_results_sort_by('')}</option>
                </#if>
            </select>
        </div>
    </#if>
</#macro>

<#macro printHits>
    <div>
    <select form="search-form" name="hitsPerPage" id="search-form-hits-per-page" onchange="this.form.submit()">
        <#list hitsPerPageOptions as option>
            <#if option == hitsPerPage>
                <option value="${option}" selected="selected">${i18n().search_results_per_page(option)}</option>
            <#else>
                <option value="${option}">${i18n().search_results_per_page(option)}</option>
            </#if>
        </#list>
    </select>
    </div>
</#macro>

<#macro searchFormGroupTab group active >
    <#if active>
         <li class="active form-group-tab">
    <#else>
        <li class="form-group-tab">
    </#if>
            <a data-toggle="tab" href="#${group.id?html}">${group.label?html}</a>
        </li>
</#macro>

<#macro searchFormFilterTab filter assignedActive>
    <#if filter.id == "querytext">
        <#-- skip query text filter -->
        <#return>
    </#if>
        <li class="filter-tab">
            <a data-toggle="tab" href="#${filter.id?html}">${filter.name?html}</a>
        </li>
</#macro>

<#macro printFilterValues filter assignedActive isEmptySearch>
        <div id="${filter.id?html}" class="tab-pane fade filter-area">
            <#if filter.id == "querytext">
            <#-- skip query text filter -->
            <#elseif filter.type == "RangeFilter">
                <@rangeFilter filter/>
            <#else>
                <#if filter.input>
                    <div class="user-filter-search-input">
                        <@createUserInput filter />
                    </div>
                </#if>
                <#assign valueNumber = 1>
                <#assign additionalLabels = false>
                <#list filter.values?values as v>
                    <#if !v.selected>
                        <#if filter.moreLimit = valueNumber - 1 >
                            <#assign additionalLabels = true>
                            <a class="more-facets-link" href="javascript:void(0);" onclick="expandSearchOptions(this)">${i18n().paging_link_more}</a>
                        </#if>
                        <#if user.loggedIn || v.publiclyAvailable>
                            <@getInput filter v getValueID(filter.id, valueNumber) valueNumber />
                            <@getLabel valueNumber, v, filter, additionalLabels />
                        </#if>
                    </#if>
                    <#assign valueNumber = valueNumber + 1>
                </#list>
                <#if additionalLabels >
                    <a class="less-facets-link additional-search-options hidden-search-option" href="javascript:void(0);" onclick="collapseSearchOptions(this)">${i18n().display_less}</a>
                </#if>  
            </#if>
        </div>
</#macro>

<#macro rangeFilter filter>
    <#assign min = filter.min >
    <#assign max = filter.max >
    <#assign from = filter.fromYear >
    <#assign to = filter.toYear >

    <div class="range-filter" id="${filter.id?html}" class="tab-pane fade filter-area">
        <div class="range-slider-container" min="${filter.min?html}" max="${filter.max?html}">
            <div class="range-slider"></div>
            ${i18n().from}
            <#if from?has_content>
                <div class="range-slider-start">${from?html}</div>
            <#else>
                <div class="range-slider-start">${min?html}</div>
            </#if>
            ${i18n().to}
            <#if to?has_content>
                <div class="range-slider-end">${to?html}</div>
            <#else>
                <div class="range-slider-end">${max?html}</div>
            </#if>
            <input form="search-form" id="filter_range_${filter.id?html}" style="display:none;" class="range-slider-input" name="filter_range_${filter.id?html}" value="${filter.rangeInput?html}"/>
        </div>
    </div>
</#macro>


<#macro getSelectedLabel valueID value filter >
    <#assign label = filter.name + " : " + value.name >
    <#if !filter.localizationRequired>
        <#assign label = filter.name + " : " + value.id >
    </#if>
    <label for="${getValueID(filter.id, valueNumber)?html}">${getValueLabel(label, value.count)?html}</label>
</#macro>

<#macro getLabel valueID value filter additional=false >
    <#assign label = value.name >
    <#assign additionalClass = "" >
    <#if !filter.localizationRequired>
        <#assign label = value.id >
    </#if>
    <#if additional=true>
        <#assign additionalClass = "additional-search-options hidden-search-option" >
    </#if>

    <label class="${additionalClass}" for="${getValueID(filter.id, valueNumber)?html}" >${getValueLabel(label, value.count)?html}</label>
</#macro>


<#macro userSelectedInput filter>
    <#if filter.inputText?has_content>
        <button form="search-form" type="button" id="button_filter_input_${filter.id?html}" onclick="clearInput('filter_input_${filter.id?js_string?html}')" class="checked-search-input-label">${filter.name?html} : ${filter.inputText?html}</button>
    </#if>
    <#assign from = filter.fromYear >
    <#assign to = filter.toYear >
    <#if from?has_content && to?has_content >
        <#assign range = i18n().from + " " + from + " " + i18n().to + " " + to >
        <button form="search-form" type="button" id="button_filter_range_${filter.id?html}" onclick="clearInput('filter_range_${filter.id?js_string?html}')" class="checked-search-input-label">${filter.name?html} : ${range?html}</button>
    </#if>
</#macro>

<#macro createUserInput filter>
    <input form="search-form" id="filter_input_${filter.id?html}"  placeholder="${i18n().search_field_placeholder}" class="search-vivo" type="text" name="filter_input_${filter.id?html}" value="${filter.inputText?html}" autocapitalize="none" />
</#macro>

<#macro getInput filter filterValue valueID valueNumber form="search-form">
    <#assign checked = "" >
    <#assign class = "" >
    <#if filterValue.selected>
        <#assign checked = " checked=\"checked\" " >
        <#assign class = "selected-input" >
    </#if>
    <#assign type = "checkbox" >
    <#if !filter.multivalued>
        <#assign type = "radio" >
    </#if>
    <#assign filterName = filter.id >
    <#if filter.multivalued>
        <#assign filterName = filterName + "_" + valueNumber >
    </#if>

    <input 
        form="${form}" 
        type="${type}" 
        id="${valueID?html}" 
        value="${filter.id?html + ":" + filterValue.id?html}" 
        name="filters_${valueID?html}" 
        style="display:none;" 
        ${checked} 
        class="${class}" 
    >
</#macro>

<#function getValueID id number>
    <#return id + "__" + number /> 
</#function>

<#function getValueLabel label count >
    <#assign result = label >
    ${label}
    <#if count!=0>
        <#assign result = result + " (" + count + ")" >
    </#if>
    <#return result />
</#function>

${stylesheets.add('<link rel="stylesheet" href="//code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />',
  				  '<link rel="stylesheet" href="${urls.base}/css/search.css" />')}

${headScripts.add('<script src="//code.jquery.com/ui/1.10.3/jquery-ui.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/tiny_mce/tiny_mce.js"></script>'
                  )}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/searchDownload.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/search/search_results.js"></script>')}

