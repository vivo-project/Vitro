<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#--The form for adding a new label-->
<form id="addLabelForm" name="addLabelForm" class="customForm" action="${submitUrl}">
   <h2>${i18n().add_label}</h2>
<p>
  <label for="name">${i18n().name} ${requiredHint}</label>
  <input size="30"  type="text" id="label" name="label" value="" />
</p>
<label for="newLabelLanguage">${i18n().add_label_for_language}</label>
<select  name="newLabelLanguage" id="newLabelLanguage" >
<option value=""<#if !newLabelLanguageValue?has_content> selected="selected"</#if>>${i18n().select_locale}</option>  
<#if editConfiguration.pageData.selectLocale?has_content>
	<#assign selectLocale = editConfiguration.pageData.selectLocale />
	<#--Use the list of names because that is the order we want displayed-->

	<#list selectLocale as locale>
	
 	<option value="${locale.code}"<#if newLabelLanguageValue?has_content && locale.code == newLabelLanguageValue> selected="selected"</#if>>${locale.label}</option>
	</#list>
</#if>           
 </select>         

<input type="hidden" name="editKey" id="editKey" value="${editKey}"/>

<input type="submit" class="submit" id="submit" value="${i18n().save_button}" role="button" role="input" />
${i18n().or} 
<a href="${urls.referringPage}" class="cancel" title="${i18n().cancel_title}" >${i18n().cancel_link}</a>

</form>