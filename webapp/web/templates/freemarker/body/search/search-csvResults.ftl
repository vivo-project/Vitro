<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#assign today = .now >
<#assign todayDate = today?date>
Results from ${siteName} for ${querytext} on ${todayDate}

URI, Name, URL
<#list individuals as individual>                 
"${individual.uri}","${individual.name}","${individual.profileUrl}"
</#list>
