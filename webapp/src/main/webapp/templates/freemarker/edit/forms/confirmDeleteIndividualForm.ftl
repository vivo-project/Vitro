<#-- $This file is distributed under the terms of the license in LICENSE$ -->
<#if editConfiguration.pageData.redirectUrl??>
	<#assign redirectUrl = editConfiguration.pageData.redirectUrl />
<#else>
	<#assign redirectUrl = "/" />
</#if>
<#if editConfiguration.pageData.individualName??>
	<#assign individualName = editConfiguration.pageData.individualName />
</#if>
<#if editConfiguration.pageData.individualType??>
	<#assign individualType = editConfiguration.pageData.individualType />
</#if>

<form action="${editConfiguration.deleteIndividualProcessingUrl}" method="get">
    <h2>${i18n().confirm_individual_deletion} </h2>

    <input type="hidden" name="individualUri"    value="${editConfiguration.objectUri}" role="input" />
    <input type="hidden" name="redirectUrl"    value="${redirectUrl}" role="input" />
    <p>
      <#if individualType??>
       ${individualType}
      </#if>
      <#if individualName??>
       ${individualName} 
      </#if>
    </p>
   <br />
    <p class="submit">
        <input type="submit" id="submit" value="${i18n().delete_button}" role="button"/>
        or
        <a class="cancel" title="${i18n().cancel_title}" href="${editConfiguration.cancelUrl}">${i18n().cancel_link}</a>
    </p>
</form>
