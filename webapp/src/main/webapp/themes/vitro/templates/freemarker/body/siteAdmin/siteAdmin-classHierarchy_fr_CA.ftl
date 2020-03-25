<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<section role="region">
	<#if propertyName?? >
		<h2>Classes with a restriction on <em><a href="${editController}${propertyURI?url}">${propertyName!}</a></em></h2>
		<br/>
	<#else>
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
	</#if>
    <section id="container">
		<#if propertyName?? && !jsonTree?? >
			${i18n().no_class_restrictions}
		</#if>
    </section>
</se	ction>
<script language="javascript" type="text/javascript" >
    var json = [${jsonTree!}];
    var i18nStrings = {
        hideSubclasses: "${i18n().hide_subclasses}",
        showSubclasses: "${i18n().show_subclasses}",
        classGroup: "${i18n().class_group_all_caps}",
        ontologyString: "${i18n().ontology_capitalized}",
        subclassesString: "${i18n().subclasses_capitalized}",
        expandAll: "${i18n().expand_all}",
        collapseAll: "${i18n().collapse_all}",
        classesString: "${i18n().classes_capitalized}",
        displayRank: "${i18n().display_rank}"
    };
</script>

<script language="javascript" type="text/javascript" >
$(document).ready(function() {
    classHierarchyUtils.onLoad("${urls.base!}","${displayOption!}");
});
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/classHierarchy.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.12.1.min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/siteAdmin/classHierarchyUtils.js"></script>')}

