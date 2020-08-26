<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#--
        Used to display both the object and data property hierarchies, though there are
        separate controllers for those. Also used to display lists of "all" object and
        data properties, though there are separate controllers for those, too.
 -->
 <#if propertyType??>
     <#assign propType = propertyType>
<#else>
    <#assign propType = "group">
</#if>


<section role="region">

    <h2>${pageTitle!}</h2>

    <#if !displayOption?has_content>
        <#assign displayOption = "hierarchy">
    </#if>

    <#if propType == "group">
        <form action="editForm" method="get">
            <input type="submit" class="form-button" id="addProperty" value="${i18n().add_property_group}"/>
            <input type="hidden" name="controller" value="PropertyGroup"/>
        </form>
        <div id="expandLink"><span id="expandAll" ><a href="javascript:" title="${i18n().hide_show_properties}">${i18n().hide_properties}</a></span></div>
    <#else>
        <form name="classHierarchyForm" id="classHierarchyForm" action="show<#if propType == "object">Object<#else>Data</#if>PropertyHierarchy" method="post" role="classHierarchy">
        <label id="displayOptionLabel" class="inline">${i18n().display_options}</label>
            <select id="displayOption" name="displayOption">
                <option value="hierarchy" <#if displayOption == "asserted">selected</#if> >${propType?capitalize} ${i18n().property_hierarchy}</option>
                <option value="all" <#if displayOption == "all">selected</#if> >${i18n().all_x_properties(propType?capitalize)}</option>
                <option value="group" <#if displayOption == "group">selected</#if> >${i18n().property_groups}</option>
            </select>
            <input type="submit" class="form-button" id="addProperty" value="${i18n().add_new} <#if propType == "object">${i18n().object}<#else>${i18n().data}</#if> ${i18n().property}"/>
        </form>
        <div id="expandLink"><span id="expandAll" ><a href="#" title="${i18n().expand_all}">${i18n().expand_all}</a></span></div>
    </#if>

    <section id="container">
    </section>

</section>
<script language="javascript" type="text/javascript" >
    var json = [${jsonTree!}];
	var propertyType = '${propType}';
    var i18nStrings = {
        hideProperties: "${i18n().hide_properties?js_string}",
        showProperties: "${i18n().show_properties?js_string}",
        localNameString: "${i18n().local_name?js_string}",
        groupString: "${i18n().group_capitalized?js_string}",
        domainClass: "${i18n().domain_class?js_string}",
        rangeClass: "${i18n().range_class?js_string}",
        rangeDataType: "${i18n().range_data_type?js_string}",
        expandAll: "${i18n().expand_all?js_string}",
        collapseAll: "${i18n().collapse_all?js_string}",
        subProperties: "${i18n().sub_properties?js_string}",
        displayRank: "${i18n().display_rank?js_string}",
        subProperty: "${i18n().subproperty?js_string}",
        propertiesString: "${i18n().properties_capitalized?js_string}"
    };
</script>


<script language="javascript" type="text/javascript" >
$(document).ready(function() {
    objectPropHierarchyUtils.onLoad("${urls.base!}","${displayOption!}","${propType}");
});
</script>


${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/classHierarchy.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.12.1.min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/siteAdmin/objectPropertyHierarchyUtils.js"></script>')}

