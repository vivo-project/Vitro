<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#--Save to variable is sparqlResults -->
<#assign resultsExist = false/>
<#if variableName?has_content>
	<#assign resultsExist = true/>
	<#--This will retrieve the results stored in the variable name being returned from the sparql query.
	For example, if "results" was specified as the variable storing the sparql results, the value
	of "results" will not be assigned to "sparqlResults" below. -->
	<#assign sparqlResults = .globals[variableName]/>
</#if>

<h3>${i18n().sparql_query_results}</h3>
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
	${i18n().no_results_returned} 
</#if>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/sparqlresults.css" />')}

