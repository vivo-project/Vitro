<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#--Save to variable is sparqlResults -->
<#assign resultsExist = false/>
<#if sparqlResults?has_content>
	<#assign resultsExist = true/>
</#if>

<h3>Sparql Query Results</h3>
<#if resultsExist>
	<#assign numberRows = sparqlResults?size/>
	<#assign firstRow = false/>
	<#list sparqlResults as resultRow>
		<#assign resultKeys = resultRow?keys />
		<#if firstRow = false>
			<div class="resultHeading resultRow">
			<#list resultKeys as resultKey>
					<div class="resultCell">${resultKey}</div>
			</#list>	
			</div>
			<#assign firstRow = true/>
		</#if>
		<div class="resultRow">
			<#list resultKeys as resultKey>
				<div class="resultCell">${resultRow[resultKey]}</div>
			</#list>
		</div>
	</#list>
<#else>
	No results were returned.  
</#if>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/sparqlresults.css" />')}

