<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#--This contains the template for the Sparql Query content type that is to be cloned and used in page management-->
<section id="sparqlQuery" style="background-color:#f9f9f9;padding-left:6px;padding-top:2px;border-width:1px;border-style:solid;border-color:#ccc;">
    <label id="variableLabel" for="variable">Variable Name<span class="requiredHint"> *</span></label>
    <input type="text" name="saveToVar" size="20" value="" id="saveToVar" role="input" />
    <label id="queryModelLabel" for="queryModel">Query Model</label>
    <#--Hiding query model for now-->
    <input type="text" name="queryModel" size="20" value="" id="queryModel" role="input" style="display:none"/>
    <label id="queryLabel" for="queryLabel"><span id="querySpan">Enter SPARQL query here</span><span class="requiredHint"> *</span></label>
    <textarea id="query" name="query" cols="70" rows="15" style="margin-bottom:7px"></textarea>
</section>
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/processSparqlDataGetterContent.js"></script>')}
