<#-- $This file is distributed under the terms of the license in LICENSE$ -->
<#--The form for adding a new label-->
<form id="addLabelForm" name="addLabelForm" class="customForm" action="${submitUrl}">
   <h2>${i18n().add_label}</h2>
<p>
  <label for="name">${i18n().name} ${requiredHint}</label>
  <input size="30"  type="text" id="label" name="label" value="" />
</p>

<input type="hidden" name="editKey" id="editKey" value="${editKey}"/>

<input type="submit" class="submit" id="submit" value="${i18n().save_button}" role="button" />
${i18n().or}
<a href="${urls.referringPage}" class="cancel" title="${i18n().cancel_title}" >${i18n().cancel_link}</a>

</form>
