<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<section role="region">
    
    <h2>${pageTitle!}</h2>

    <#if !displayOption?has_content>
        <#assign displayOption = "asserted">
    </#if>
    <form name="classHierarchyForm" id="classHierarchyForm" action="showClassHierarchy" method="post" role="classHierarchy">
        <label id="displayOptionLabel" class="inline">${i18n().display_options}</label>
        <select id="displayOption" name="displayOption">
            <option value="asserted" <#if displayOption == "asserted">selected</#if> >${i18n().asserted_class_hierarchy}</option>
            <option  value="inferred" <#if displayOption == "inferred">selected</#if> >${i18n().inferred_class_hierarchy}</option>
            <option value="all" <#if displayOption == "all">selected</#if> >${i18n().all_classes}</option>
            <option value="group" <#if displayOption == "group">selected</#if> >${i18n().classes_by_classgroup}</option>
        </select>
        <input id="addClass" value="${i18n().add_new_classes}" class="form-button" type="submit" />
        <#if displayOption == "group">
                <input type="submit" id="addGroup" class="form-button" value="${i18n().add_new_group}"/>
        </#if>
    </form>
        
    <#if displayOption == "group">
        <div id="expandLink"><span id="expandAll" ><a href="javascript:" title="${i18n().hide_show_subclasses}">${i18n().hide_subclasses}</a></span></div>
    <#else>
        <div id="expandLink"><span id="expandAll" ><a href="#" title="${i18n().expand_all}">${i18n().expand_all}</a></span></div>
    </#if>
    <section id="container">

    </section>
</section>
<script language="javascript" type="text/javascript" >
    var json = [${jsonTree!}];
</script>

<script language="javascript" type="text/javascript" >
$(document).ready(function() {
    classHierarchyUtils.onLoad("${urls.base!}","${displayOption!}");
});    
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/classHierarchy.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/siteAdmin/classHierarchyUtils.js"></script>')}
              
