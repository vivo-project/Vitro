<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Custom form for managing labels for individuals -->
<#assign labelStr = "" >
<#assign languageTag = "" >
<#assign labelSeq = [] >
<#if subjectName?? >
<h2>Manage Labels for ${subjectName}</h2>
<#else>
<h2>${i18n().manage_labels}</h2>
</#if>
<p id="mngLabelsText">${i18n().manage_labels_intro}</p>

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
            <input type="button" class="submit" id="submit" value="${i18n().save_button}" role="button" role="input" />
            <span class="or"> or </span>
            <a href="${urls.referringPage}" class="cancel" title="${i18n().cancel_title}" >${i18n().cancel_link}</a>
            <span id="indicator" class="indicator hidden">
                <img class="indicator" src="${urls.base}/images/indicatorWhite.gif" alt="${i18n().processing_icon}"/>&nbsp;${i18n().selection_in_process}
            </span>
        </p>
    </section>

<script type="text/javascript">
var customFormData = {
    processingUrl: '${urls.base}/edit/primitiveRdfEdit',
    individualUri: '${subjectUri!}'
};
var i18nStrings = {
    errorProcessingLabels: '${i18n().error_processing_labels}'
};
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/utils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>',
                '<script type="text/javascript" src="${urls.base}/js/individual/manageLabelsForIndividual.js"></script>')}
              
