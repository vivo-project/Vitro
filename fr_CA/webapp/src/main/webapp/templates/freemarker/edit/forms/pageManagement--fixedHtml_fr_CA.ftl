<#-- $This file is distributed under the terms of the license in LICENSE$ -->
<#--This contains the template for the fixed HTML content type that is to be cloned and used in page management-->

<section id="fixedHtml" class="contentSectionContainer">
    <label id="fixedHTMLVariableLabel" for="fixedHTMLVariable">${i18n().variable_name_all_caps}<span class="requiredHint"> *</span></label>
    <input type="text" name="saveToVar" size="20" value="" id="fixedHTMLSaveToVar" role="input" />
    <label id="fixedHTMLValueLabel" for="fixedHTMLValue">${i18n().enter_fixed_html_here}<span id="fixedHTMLValueSpan"></span><span class="requiredHint"> *</span></label>
    <textarea id="fixedHTMLValue" name="htmlValue" cols="70" rows="15" style="margin-bottom:7px"></textarea><br />
    <input  type="button" id="doneWithContent" name="doneWithContent" value="${i18n().save_this_content}" class="doneWithContent" />
    <#if menuAction == "Add">
        <span id="cancelContent"> ${i18n().or} <a class="cancel" href="javascript:"  id="cancelContentLink" title="${i18n().cancel_title}">${i18n().cancel_link}</a></span>
    </#if>
</section>
<script>
    var i18nStringsFixedHtml = {
        fixedHtml: "${i18n().fixed_html?js_string}",
        supplyVariableName: "${i18n().supply_variable_name?js_string}",
        noApostrophes: "${i18n().apostrophe_not_allowed?js_string}",
        noDoubleQuotes: "${i18n().double_quote_note_allowed?js_string}",
        supplyHtml: "${i18n().supply_html?js_string}"
    };
</script>
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/processFixedHTMLDataGetterContent.js"></script>')}
