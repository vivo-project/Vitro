<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#--This contains the template for the fixed HTML content type that is to be cloned and used in page management-->

<section id="fixedHtml" class="contentSectionContainer">
    <label id="fixedHTMLVariableLabel" for="fixedHTMLVariable">Variable Name<span class="requiredHint"> *</span></label>
    <input type="text" name="saveToVar" size="20" value="" id="fixedHTMLSaveToVar" role="input" />
    <label id="fixedHTMLValueLabel" for="fixedHTMLValue">Enter fixed HTML here<span id="fixedHTMLValueSpan"></span><span class="requiredHint"> *</span></label>
    <textarea id="fixedHTMLValue" name="htmlValue" cols="70" rows="15" style="margin-bottom:7px"></textarea><br />
    <input  type="button" id="doneWithContent" name="doneWithContent" value="Save this content" class="doneWithContent" />
    <#if menuAction == "Add">
        <span id="cancelContent"> or <a class="cancel" href="javascript:"  id="cancelContentLink" >Cancel</a></span>
    </#if>
</section>
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/processFixedHTMLDataGetterContent.js"></script>')}
