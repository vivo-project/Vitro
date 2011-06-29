/* $This file is distributed under the terms of the license in /doc/license.txt$ */

<h2>${formTitle}</h2>

<#assign form ="/edit/processRdfForm2.jsp">

<#assign formUrl ="${form?url}">

<#if ${predicate.selectFromExisting} == true>
	<#if ${rangeOptionsExist}  == true >
		<form class="editForm" action = "${formUrl}">
			<#if predicate.publicDescription??>
				<p>${predicate.publicDescription}</p>
				<input type="text" id="objectVar" size="80" />
				<div style="margin-top: 0.2em">
					<input type="submit" id="submit" value="${submitLabel}" cancel="true"/>
				</div>
			</#if>	
		</form>
	</#if>
</#if>
