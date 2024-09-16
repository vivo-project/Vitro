<#-- $This file is distributed under the terms of the license in LICENSE$ -->
<#--Browse Search Filter Values Section-->
<#-----------Variable assignment-------------->
<#--Requires Menu action be defined in parent template-->

<#assign searchFilter = pageData.searchFilter />
<#assign searchFilters = pageData.searchFilters />
<#-- some additional processing here which shows or hides the class group selection and classes based on initial action-->
<#assign selectFilterStyle = 'class="hidden"' />
<#-- Reveal the class group and hide the class selects if adding a new menu item or editing an existing menu item with an empty class group (no classes)-->
<#-- Menu action needs to be sent from  main template-->
<#if menuAction == "Add" || !searchFilter?has_content>
    <#assign selectFilterStyle = " " />
</#if>

<#--HTML Portion-->
 <section id="searchFilterValues" class="contentSectionContainer">
    <section id="selectContentType" name="selectContentType" ${selectFilterStyle} role="region">

        <label for="selectSearchFilter">${i18n().select_search_filter_to_browse}<span class="requiredHint"> *</span></label>
        <select name="selectSearchFilter" id="selectSearchFilter" role="combobox">
            <option value="-1" role="option">${i18n().select_one}</option>
            <#list searchFilters as filter>
                <option value="${filter.URI}"  role="option">${filter.publicName}</option>
            </#list>
        </select>
    </section>
    <input  type="button" id="doneWithContent" class="doneWithContent" name="doneWithContent" value="${i18n().save_this_content}" />
    <#if menuAction == "Add">
        <span id="cancelContent"> ${i18n().or} <a class="cancel" href="javascript:"  id="cancelContentLink" title="${i18n().cancel_title}">${i18n().cancel_link}</a></span>
    </#if>
</section>
<script>
    var i18nStringsBrowseSearchFilters = {
        browseSearchFilter: '${i18n().browse_search_filter_facets?js_string}',
        allCapitalized: '${i18n().all_capitalized?js_string}',
        supplySearchFilter: '${i18n().supply_search_filer?js_string}'
    };
</script>
 <#--Include JavaScript specific to the types of data getters related to this content-->
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/processSearchFilterValuesDataGetterContent.js"></script>')}
