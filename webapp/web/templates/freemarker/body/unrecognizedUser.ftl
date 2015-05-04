<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for the Unrecognized User page. -->

<section role="region">
    <h2>${i18n().unrecognized_user}</h2>
    
    <p>
      ${i18n().no_individual_associated_with_id(siteName!)}
    </p>

    <br/>
    <a href="${urls.home}" title="${i18n().continue}">${i18n().continue}</a>
</section>
