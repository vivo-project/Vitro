<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<h2>${editConfiguration.formTitle}</h2>

<#assign predicateProperty = "${editConfiguration.predicateProperty}" />
<#assign literalValues = "${editConfiguration.dataLiteralValuesAsString}" />

		<form class="editForm" action = "${submitUrl}" method="post">
			<input type="hidden" name="editKey" id="editKey" value="${editKey}" />
			<#if editConfiguration.propertyPublicDescription?has_content>
			   <label for="${editConfiguration.dataLiteral}"><p class="propEntryHelpText">${predicateProperty.publicDescription}</p></label>
			</#if>   
				<p>${editConfiguration.propertyPublicDescription}</p>
				
				<input rows="2" type="textarea" 
				id="${editConfiguration.dataLiteral}" name="${editConfiguration.dataLiteral}"
				value="${literalValues}"/>
			
				<div style="margin-top: 0.2em">
					<input type="submit" id="submit" value="${editConfiguration.submitLabel}" cancel="true"/>
				</div>
			</#if>	
		</form>
		
<#--ToDo: Include delete portion-->
		

