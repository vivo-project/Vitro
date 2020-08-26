<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#--Template for adding and editing menu items -->

<#-- some additional processing here which shows or hides the class group selection and classes based on initial action-->
<#assign existingClassGroupStyle = " " />
<#assign selectClassGroupStyle = 'class="hidden"' />
<#-- Reveal the class group and hide the class selects if adding a new menu item or editing an existing menu item with an empty class group (no classes)-->
<#if menuAction == "Add" || !classGroup?has_content>
    <#assign existingClassGroupStyle = 'class="hidden"' />
    <#assign selectClassGroupStyle = " " />
</#if>
<section id="error-alert" role="alert" class="hidden">
    <img src="${urls.images}/iconAlert.png" width="24" height="24" alt="${i18n().error_alert_icon}" />
    <p></p>
</section>

<h3>${menuAction} ${i18n().menu_item}</h3>

<section id="${menuAction?lower_case}-menu-item" role="region">
    <form method="POST" action="${formUrls}" class="customForm" role="${menuAction} menu item">
        <input type="hidden" name="cmd" id="cmd" value="${menuAction}" role="input" />
        <input type="hidden" name="menuItem" id="menuItem" value="${menuItem}" role="input" />
        <input type="hidden" name="switchToDisplayModel" id="switchToDisplayModel" value="true" role="input" />

        <label for="menu-name">${i18n().name}<span class="requiredHint"> *</span></label>
        <input type="text" name="menuName" value="${menuName}" role="input" />

        <label for="pretty-url">${i18n().pretty_url}<span class="requiredHint"> *</span></label>
        <input type="text" name="prettyUrl" value="${prettyUrl}" role="input" />
        <p class="note">${i18n().start_with_leading_slash}</p>

        <p>${i18n().template_capitalized}<span class="requiredHint"> *</span></p>

        <input type="radio" class="default-template" name="selectedTemplate" value="default" <#if selectedTemplateType = "default">checked</#if> role="radio" />
        <label class="inline" for="default"> ${i18n().default}</label>
        <br />
        <input type="radio" name="selectedTemplate" class="custom-template" value="custom" <#if selectedTemplateType = "custom">checked</#if> role="input" />
        <label class="inline" for="custom"> ${i18n().custom_template_mixed_caps}</label>

        <section id="custom-template" <#if selectedTemplateType != 'custom'>class="hidden" </#if>role="region">
            <input type="text" name="customTemplate" value="${customTemplate!}" size="40" role="input" /><span class="requiredHint"> *</span>
        </section>

        <section id="existingContentType" name="existingContentType" ${existingClassGroupStyle} role="region">
            <p>${i18n().selected_page_content_type}</p>
            <p>
                <span id="selectedContentTypeValue" name="selectedContentTypeValue">${associatedPage!}</span>
                <a href="#" id="changeContentType" name="changeContentType" title="${i18n().change_content_type}">${i18n().change_content_type}</a>
            </p>
        </section>

        <#-- Select class group -->
        <section id="selectContentType" name="selectContentType" ${selectClassGroupStyle} role="region">
           <label for="selectClassGroup">${i18n().select_page_content_type}<span class="requiredHint"> *</span></label>

           <select name="selectClassGroup" id="selectClassGroup" role="combobox">
               <option value="-1" role="option">${i18n().select_one}</option>
               <#list classGroups as aClassGroup>
                    <option value="${aClassGroup.URI}" <#if aClassGroup.URI = associatedPageURI>selected</#if> role="option">${aClassGroup.publicName}</option>
               </#list>
           </select>
        </section>

        <section id="classesInSelectedGroup" name="classesInSelectedGroup" ${existingClassGroupStyle}>
            <#-- Select classes in a class group -->
            <p id="selectClassesMessage" name="selectClassesMessage">${i18n().select_content_display}<span class="requiredHint"> *</span></p>

            <#include "menuManagement--classIntersections.ftl">

            <ul id="selectedClasses" name="selectedClasses" role="menu">
                <#--Adding a default class for "ALL" in case all classes selected-->
                <li class="ui-state-default" role="menuitem">
                    <input type="checkbox" name="allSelected" id="allSelected" value="all" <#if !isIndividualsForClassesPage?has_content>checked</#if> />
                    <label class="inline" for="All"> ${i18n().all_capitalized}</label>
                </li>
                <#list classGroup as classInClassGroup>
                <li class="ui-state-default" role="menuitem">
                    <input type="checkbox" id="classInClassGroup" name="classInClassGroup" value="${classInClassGroup.URI}" <#if includeAllClasses = true>checked</#if>
                     <#if isIndividualsForClassesPage?has_content>
                            <#list includeClasses as includeClass>
                                <#if includeClass = classInClassGroup.URI>
                                    checked
                                </#if>
                            </#list>
                    </#if> />
                    <label class="inline" for="${classInClassGroup.name}"> ${classInClassGroup.name}</label>
                    <#-- PLACEHOLDER - not yet implemented) -->
                    <span class="ui-icon-sortable"></span> <#--sortable icon for dragging and dropping menu items-->
                </li>
                </#list>
            </ul>
        </section>

        <input type="submit" name="submit-${menuAction}" value="${i18n().save_changes}" class="submit" role="input" /> ${i18n().or} <a class="cancel" href="${cancelUrl}" title="${i18n().cancel_title}">${i18n().cancel_link}</a>

        <p class="requiredHint">* ${i18n().required_fields}</p>
    </form>
</section>
<script>
    var i18nStrings = {
        supplyName: "${i18n().supply_name?js_string}",
        supplyPrettyUrl: "${i18n().supply_url?js_string}",
        startUrlWithSlash: "${i18n().start_url_with_slash?js_string}",
        supplyTemplate: "${i18n().supply_template?js_string}",
        supplyContentType: "${i18n().supply_content_type?js_string}",
        selectContentType: "${i18n().select_content_type?js_string}",
        allCapitalized: "${i18n().all_capitalized?js_string}"
    };
</script>
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/menuManagement.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jquery.scrollTo-min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/menumanagement_edit.js"></script>')}
