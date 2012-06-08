<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#--This contains the template for the fixed HTML content type that is to be cloned and used in page management-->

<section id="fixedHtml" style="background-color:#f9f9f9;padding-left:6px;padding-top:2px;border-width:1px;border-style:solid;border-color:#ccc;">
    <label id="fixedHTMLVariableLabel" for="fixedHTMLVariable">Variable Name<span class="requiredHint"> *</span></label>
    <input type="text" name="saveToVar" size="20" value="" id="fixedHTMLSaveToVar" role="input" />
    <label id="fixedHTMLValueLabel" for="fixedHTMLValue">Enter fixed HTML here<span id="fixedHTMLValueSpan"></span><span class="requiredHint"> *</span></label>
    <textarea id="fixedHTMLValue" name="htmlValue" cols="70" rows="15" style="margin-bottom:7px"></textarea>
</section>
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/processFixedHTMLDataGetterContent.js"></script>')}
