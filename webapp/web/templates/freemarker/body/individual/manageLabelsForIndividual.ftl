<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Custom form for managing labels for individuals -->
<#assign labelStr = "" >
<#assign languageTag = "" >
<#assign labelSeq = [] >
<#if subjectName?? >
<h2>Manage Labels for ${subjectName}</h2>
<#else>
<h2>Manage Labels</h2>
</#if>
<p id="mngLabelsText">
Multiple labels exist for this profile but there should only be one. Select the label you want displayed on the profile page, and the others will be deleted. 
</p>

    <section id="rdfsLabels" role="container">
        <ul>
        <#list labels as label>
            <#-- the query will return labels with their language tag or datatype, if any. So strip those out  -->
            <#if label?? && ( label?index_of("@") > -1 ) >
                <#assign labelStr = label?substring(0, label?index_of("@")) >
                <#assign tagOrTypeStr = label?substring(label?index_of("@")) >
            <#elseif label?? && ( label?index_of("^^") > -1 ) >
                <#assign labelStr = label?substring(0, label?index_of("^^")) >
                <#assign tagOrTypeStr = label?substring(label?index_of("^^")) >
                <#assign tagOrTypeStr = tagOrTypeStr?replace("^^http","^^<http") >
                <#assign tagOrTypeStr = tagOrTypeStr?replace("#string","#string>") >
            <#else>
                <#assign labelStr = label >
                <#assign tagOrTypeStr = "" >
            </#if>
            <li>
            <input type="radio" class="labelCheckbox" name="labelCheckbox" id="${labelStr}" tagOrType="${tagOrTypeStr!}" role="radio" />
            <label class="inline">${labelStr}
                <#if labelSeq?seq_contains(labelStr)>
                    (duplicate value)
                </#if>
            </label>
            </li>
            <#assign labelSeq = labelSeq + [labelStr]>
        </#list>
        </ul>

        <br />   
        <p>       
            <input type="button" class="submit" id="submit" value="Save" role="button" role="input" />
            <span class="or"> or </span>
            <a href="${urls.referringPage}" class="cancel" title="cancel" >Cancel</a>
            <span id="indicator" class="indicator hidden">
                <img class="indicator" src="${urls.base}/images/indicatorWhite.gif" />&nbsp;Your selection is being processed.
            </span>
        </p>
    </section>

<script type="text/javascript">
var customFormData = {
    processingUrl: '${urls.base}/edit/primitiveRdfEdit',
    individualUri: '${subjectUri!}'
};
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/utils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>',
                '<script type="text/javascript" src="${urls.base}/js/individual/manageLabelsForIndividual.js"></script>')}
              
