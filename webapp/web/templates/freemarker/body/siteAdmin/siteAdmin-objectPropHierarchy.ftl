<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

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
            <input type="submit" class="form-button" id="addProperty" value="Add new property group"/>
            <input type="hidden" name="controller" value="PropertyGroup"/>
        </form>
        <div id="expandLink"><span id="expandAll" ><a href="javascript:" title="hide/show properties">hide properties</a></span></div>
    <#else>
        <form name="classHierarchyForm" id="classHierarchyForm" action="show<#if propType == "object">Object<#else>Data</#if>PropertyHierarchy" method="post" role="classHierarchy">
        <label id="displayOptionLabel" class="inline">Display Options</label>
            <select id="displayOption" name="displayOption">
                <option value="hierarchy" <#if displayOption == "asserted">selected</#if> >${propType?capitalize} Property Hierarchy</option>
                <option value="all" <#if displayOption == "all">selected</#if> >All ${propType?capitalize} Properties</option>
                <option value="group" <#if displayOption == "group">selected</#if> >Property Groups</option>
            </select>
            <input type="submit" class="form-button" id="addProperty" value="Add new <#if propType == "object">object<#else>data</#if> property"/>
        </form>
        <div id="expandLink"><span id="expandAll" ><a href="#" title="expand all">expand all</a></span></div>
    </#if>

    <section id="container">
    </section>

</section>
<script language="javascript" type="text/javascript" >
    var json = [${jsonTree!}];
</script>


<script language="javascript" type="text/javascript" >
$(document).ready(function() {
    objectPropHierarchyUtils.onLoad("${urls.base!}","${displayOption!}","${propType}");
});    
</script>


${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/classHierarchy.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/siteAdmin/objectPropertyHierarchyUtils.js"></script>')}
              
