<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#--This contains the template for the Sparql Query content type that is to be cloned and used in page management-->
<section id="sparqlQuery" class="contentSectionContainer">
    <label id="variableLabel" for="variable">Variable Name<span class="requiredHint"> *</span></label>
    <input type="text" name="saveToVar" size="20" value="" id="saveToVar" role="input" />
    <#--Hiding query model for now-->
    <#-- <label id="queryModelLabel" for="queryModel">Query Model</label>  -->
    <input type="text" name="queryModel" size="20" value="" id="queryModel" role="input" style="display:none"/>
    <label id="queryLabel" for="queryLabel"><span id="querySpan">Enter SPARQL query here</span><span class="requiredHint"> *</span></label>
    <textarea id="query" name="query" cols="70" rows="15" style="margin-bottom:7px"></textarea><br />
    <input  type="button" id="doneWithContent" class="doneWithContent" name="doneWithContent" value="Save this content" />
    <#if menuAction == "Add">
        <span id="cancelContent"> or <a class="cancel" href="javascript:"  id="cancelContentLink" >Cancel</a></span>
    </#if>
</section>
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/processSparqlDataGetterContent.js"></script>')}
