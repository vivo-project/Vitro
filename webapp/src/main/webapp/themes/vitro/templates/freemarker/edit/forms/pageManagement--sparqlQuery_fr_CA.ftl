<#-- $This file is distributed under the terms of the license in LICENSE$ -->
<#--This contains the template for the Sparql Query content type that is to be cloned and used in page management-->
<section id="sparqlQuery" class="contentSectionContainer">
    <label id="variableLabel" for="variable">${i18n().variable_name_all_caps}<span class="requiredHint"> *</span></label>
    <input type="text" name="saveToVar" size="20" value="" id="saveToVar" role="input" />
    <#--Hiding query model for now-->
    <#-- <label id="queryModelLabel" for="queryModel">${i18n().query_model}</label>  -->
    <input type="text" name="queryModel" size="20" value="" id="queryModel" role="input" style="display:none"/>
    <label id="queryLabel" for="queryLabel"><span id="querySpan">${i18n().enter_sparql_query_here}</span><span class="requiredHint"> *</span></label>
    <textarea id="query" name="query" cols="70" rows="15" style="margin-bottom:7px"></textarea><br />
    <input  type="button" id="doneWithContent" class="doneWithContent" name="doneWithContent" value="${i18n().save_this_content}" />
    <#if menuAction == "Add">
        <span id="cancelContent"> or <a class="cancel" href="javascript:"  id="cancelContentLink" title="${i18n().cancel_title}">${i18n().cancel_link}</a></span>
    </#if>
</section>
<script>
    var i18nStringsSparqlQuery = {
        sparqlResults: "${i18n().sparql_query_results}",
        supplyQueryVariable: "${i18n().supply_query_variable}",
        noApostrophes: "${i18n().apostrophe_not_allowed}",
        noDoubleQuotes: "${i18n().double_quote_note_allowed}",
        supplyQuery: "${i18n().supply_sparql_query}"
    };
</script>
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/processSparqlDataGetterContent.js"></script>')}
