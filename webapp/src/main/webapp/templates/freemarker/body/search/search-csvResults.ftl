<#-- $This file is distributed under the terms of the license in LICENSE$ -->
<#assign today = .now >
<#assign todayDate = today?date>
Results from ${siteName} for ${querytext} on ${todayDate}

Name, URI, URL
<#list individuals as individual>
"${individual.name}","${individual.uri}","${individual.profileUrl}"
</#list>
