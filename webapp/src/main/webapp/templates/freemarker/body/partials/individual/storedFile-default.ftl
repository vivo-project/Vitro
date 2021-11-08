<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Default object property statement template.

     This template must be self-contained and not rely on other variables set for the individual page, because it
     is also used to generate the property statement during a deletion.
 -->
<@showFiles statement individual />

<#macro showFiles statement individual>
  <a download="${statement.publicFilename}" title="${i18n().name}" href="${profileUrl(statement.url)}" >${statement.publicFilename}</a>
</#macro>
