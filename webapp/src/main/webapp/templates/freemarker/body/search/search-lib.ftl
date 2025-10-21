
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
        <#assign filterGroupTabsContent>
            <@filterGroupTabs/>
        </#assign>
        <#if filterGroupTabsContent?has_content>
            <div id="filter-groups" class="tabs">
                ${filterGroupTabsContent}
            </div>
            <div class="tabs filter-area">
                <#assign active = true>
                <#list filterGroups as group>
                    <#if group.displayed && !isEmptyGroup(group)>
                        <@groupFilters group active/>
                        <#assign active = false>
                    </#if>
                </#list>
            </div>
        </#if>
        <div id="search-form-footer">
            <div>
                <@printResultNumbers />
            </div>
            <div>
                <div>
                    <@showHits />
                </div>
                <@printSorting />
            </div>
        </div> 
</#macro>

<#macro filterGroupTabs >
    <#assign active = true>
    <#list filterGroups as group>
        <#if group.displayed && !isEmptyGroup(group)>
            <@searchFormGroupTab group active/>
            <#assign active = false>
        </#if>
    </#list>
</#macro>

<#macro groupFilters group active>
        <div id="${group.id}" class="tab <#if active >active<#else>fade</#if>">
            <div id="search-filter-group-container-${group.id}" class="search-filter-group-container">
                <div class="tabs">
                    <#assign assignedActive = false>
                    <#list group.filters as filterId>
                        <#if filters[filterId]??>
                            <#assign f = filters[filterId]>
                            <#if f.displayed && !isEmptyFilter(f) >
                                <@searchFormFilterTab f assignedActive/>  
                                <#if !assignedActive && (f.selected || emptySearch )>
                                    <#assign assignedActive = true>
                                </#if>
                            </#if>
                        </#if>
                    </#list>
                </div>
            </div>
            <div id="search-filter-group-tab-content-${group.id}" class="tab-content">
                <#assign assignedActive = false>
                <#list group.filters as filterId>
                    <#if filters[filterId]??>
                        <#assign f = filters[filterId]>
                        <#if f.displayed && !isEmptyFilter(f) >
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
                <#if filter.displayed>
                    <@getSelectedLabel getValueID(filter.id, valueNumber)?html v filter v.count />
                </#if>
            </#if>
            <#assign valueNumber = valueNumber + 1>
        </#list>
        <@userSelectedInput filter "search-form" />
    </#list>
</#macro>

<#macro printSorting>
    <#if sortOptions?has_content>
        <div>
            <select form="search-form" name="sort" id="search-form-sort" onchange="this.form.submit()" >
                <#assign addDefaultOption = true>
                <#list sortOptions?values as option>
                    <#if option.displayed>
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

<#macro showHits form="search-form">
    <select form="${form}" name="hitsPerPage" id="${form}-hits-per-page" onchange="this.form.submit()">
        <#list hitsPerPageOptions as option>
            <#if option == hitsPerPage>
                <option value="${option}" selected="selected">${i18n().search_results_per_page(option)}</option>
            <#else>
                <option value="${option}">${i18n().search_results_per_page(option)}</option>
            </#if>
        </#list>
    </select>
</#macro>

<#macro searchFormGroupTab group active >
    <div class="tab <#if active>active</#if>">
        <a href="#" onclick="openTab(event, '${group.id?html}');return false;">${group.label?html}</a>
    </div>
</#macro>

<#macro searchFormFilterTab filter assignedActive>
    <div class="tab filter-tab" >
        <a href="#" onclick="openTab(event, '${filter.id?html}');return false;">${filter.name?html}</a>
    </div>
</#macro>

<#macro printFilterValues filter assignedActive isEmptySearch>
        <div id="${filter.id?html}" class="tab fade filter-area">
            <#if filter.id == "querytext">
            <#-- skip query text filter -->
            <#elseif filter.type == "RangeFilter">
                <@rangeFilter filter "search-form" />
            <#else>
                <#if filter.input >
                    <div class="user-filter-search-input">
                        <@createUserInput filter />
                    </div>
                </#if>
                <#if ( !filter.localizationRequired && filter.values?values?size > filter.moreLimit) >
                    <div class="user-filter-search-input">
                        <@createAutocomplete filter />
                    </div>
                </#if>
                <#assign valueNumber = 1>
                <#assign notSelectedCount = 0>
                <#assign additionalLabels = false>
                <#list filter.values?values as v>
                    <#if !v.selected>
                        <#if filter.moreLimit = notSelectedCount >
                            <#assign additionalLabels = true>
                            <a class="more-facets-link" href="javascript:void(0);" onclick="expandSearchOptions(this)">${i18n().paging_link_more}</a>
                        </#if>
                        <#if v.displayed>
                            <@getInput filter v getValueID(filter.id, valueNumber) valueNumber />
                            <@getLabel getValueID(filter.id, valueNumber)?html v filter v.count additionalLabels />
                        </#if>
                        <#assign notSelectedCount += 1>
                    </#if>
                    <#assign valueNumber += 1>
                </#list>
                <#if additionalLabels >
                    <a class="less-facets-link additional-search-options hidden-search-option" href="javascript:void(0);" onclick="collapseSearchOptions(this)">${i18n().display_less}</a>
                </#if>
            </#if>
        </div>
</#macro>

<#macro rangeFilter filter form>
    <#assign min = filter.min >
    <#assign max = filter.max >
    <#assign from = filter.fromYear >
    <#assign to = filter.toYear >

    <div class="range-filter" id="${filter.id?html}" class="tab-pane fade filter-area">
        <div class="range-slider-container" min="${filter.min?html}" max="${filter.max?html}">
            <div class="range-slider"></div>
            ${i18n().from}
            <input type="text" size="4" class="range-slider-start" value="<#if from?has_content>${from?html}<#else>${min?html}</#if>">
            ${i18n().to}
            <input type="text" size="4" class="range-slider-end" value="<#if to?has_content>${to?html}<#else>${max?html}</#if>">
            <input form="${form}" id="filter_range_${filter.id?html}" style="display:none;" class="range-slider-input" name="filter_range_${filter.id?html}" value="${filter.rangeInput?html}"/>
        </div>
    </div>
</#macro>


<#macro getSelectedLabel valueId value filter count >
    <#assign label = filter.name + " : " + value.name >
    <#if !filter.localizationRequired>
        <#assign label = filter.name + " : " + value.id >
    </#if>
    <label for="${valueId}">${getValueLabel(label, count)?html}</label>
</#macro>

<#macro getLabel valueId value filter count additional=false >
    <#assign label = value.name >
    <#assign additionalClass = "" >
    <#if !filter.localizationRequired>
        <#assign label = value.id >
    </#if>
    <#if additional=true>
        <#assign additionalClass = "additional-search-options hidden-search-option" >
    </#if>
    <label class="${additionalClass}" for="${valueId}" >${getValueLabel(label, count)?html}</label>
</#macro>

<#macro userSelectedInput filter form>
    <#if filter.inputText?has_content>
        <#assign inputID = "filter_input_" + filter.id >
        <#if filter.id == "querytext">
            <#assign inputID = filter.id >
        </#if>
        <button form="${form}" type="button" id="button_filter_input_${filter.id?html}" onclick="clearInput(event,'${inputID?js_string?html}')" class="checked-search-input-label">${filter.name?html} : ${filter.inputText?html}</button>
    </#if>
    <#assign from = filter.fromYear >
    <#assign to = filter.toYear >
    <#if from?has_content && to?has_content >
        <#assign range = i18n().from + " " + from + " " + i18n().to + " " + to >
        <button form="${form}" type="button" id="button_filter_range_${filter.id?html}" onclick="clearInput(event,'filter_range_${filter.id?js_string?html}')" class="checked-search-input-label">${filter.name?html} : ${range?html}</button>
    </#if>
</#macro>

<#macro createUserInput filter>
    <input form="search-form" id="filter_input_${filter.id?html}"  placeholder="${i18n().search_field_placeholder}" class="search-vivo" type="text" name="filter_input_${filter.id?html}" value="${filter.inputText?html}" autocapitalize="none" />
</#macro>

<#macro createAutocomplete filter form="search-form">
    <input id="filter_autocomplete_${filter.id?html}" placeholder="${i18n().search_field_placeholder}" class="facet-input" type="text" value="" autocapitalize="none" />
    <input form="${form}" id="filter_selected_autocomplete_${filter.id?html}" type="hidden" name="filters_autocomplete_${filter.id?html}" value="" />
    <script>
      $( function() {
          $( "#filter_autocomplete_${filter.id?html?js_string}" ).autocomplete({
          source: function (request, response) {
            $.getJSON("${facetOptionsUrl}&facet_filter=${filter.id?html?js_string}", {
                term: request.term
            }, response);
          },
          minLength: 3,
          select: function( event, ui ) {
            $("#filter_selected_autocomplete_${filter.id?html?js_string}").val("${filter.id?html?js_string}:" + ui.item.value);
            $('#${form}').trigger("submit");
          }
        } );
      } );
    </script>
</#macro>

<#macro getInput filter filterValue valueID valueNumber form="search-form">
    <#assign checked = "" >
    <#assign class = "" >
    <#assign inputName = "filters_" + valueID >
    <#if filterValue.selected>
        <#assign checked = " checked=\"checked\" " >
        <#assign class = "selected-input" >
    </#if>
    <#assign type = "checkbox" >
    <#if !filter.multivalued>
        <#assign type = "radio" >
        <#assign inputName = "filters_" + filter.id >
    </#if>
    <#assign filterName = filter.id >
    <#if filter.multivalued>
        <#assign filterName = filterName + "_" + valueNumber >
    </#if>
    <input form="${form}" type="${type}" id="${valueID?html}" value="${filter.id?html + ":" + filterValue.id?html}" name="${inputName?html}" style="display:none;" ${checked} <#if class?has_content>class="${class}"</#if> >
</#macro>

<#function getValueID id number>
    <#return id + "__" + number /> 
</#function>

<#function getValueLabel label count >
    <#assign result = label >
    <#if count!=0>
        <#assign result = result + " (" + count + ")" >
    </#if>
    <#return result />
</#function>

<#function isEmptyFilter filter >
    <#return filter.id == "querytext" || (filter.type != "RangeFilter" && !filter.input && filter.values?values?filter(v -> !v.selected)?size == 0 ) />
</#function>

<#function isEmptyGroup group >
	<#list group.filters as filterId>
        <#if filters[filterId]??>
            <#assign f = filters[filterId]>
            <#if f.displayed && !isEmptyFilter(f) >
                <#return false />
            </#if>
        </#if>
    </#list>
    <#return true />
</#function>
