<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#--Assign variables from editConfig-->
<#assign customErrorMessages = editConfiguration.pageData.customErrorMessages!""/>
<p>${customErrorMessages}</p>
<p>
<a class="cancel" href="${cancelUrl}&url=/individual" title="${i18n().return_to_profile}">${i18n().return_to_profile}</a>
</p>