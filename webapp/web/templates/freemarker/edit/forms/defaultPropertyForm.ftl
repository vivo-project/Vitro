<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<h2>${editConfiguration.formTitle}</h2>

<#if editConfiguration.propertySelectFromExisting = true>
	<#if editConfiguration.rangeOptionsExist  = true >
		<#assign rangeOptionKeys = editConfiguration.rangeOptions?keys />
		<form class="editForm" action = "${submitUrl}">
			<input type="hidden" name="editKey" id="editKey" value="${editKey}" />
			<#if editConfiguration.propertyPublicDescription?has_content>
				<p>${editConfiguration.propertyPublicDescription}</p>
				
				<select type="text" id="objectVar" name="objectVar">
					<#list rangeOptionKeys as key>
					 <option value="${key}"
					 <#if editConfiguration.objectUri?has_content && editConfiguration.objectUri = key>
					 	selected
					 </#if>
					 >${editConfiguration.rangeOptions[key]}</option>
					</#list>
				</select>
				<div style="margin-top: 0.2em">
					<input type="submit" id="submit" value="${editConfiguration.submitLabel}" cancel="true"/>
				</div>
			</#if>	
		</form>
	<#else>
		<p> There are no entries in the system from which to select.  </p>	
	</#if>
</#if>

<#if editConfiguration.propertyOfferCreateNewOption = true>
<#include "defaultOfferCreateNewOptionForm.ftl">

</#if>

<#if editConfiguration.propertySelectFromExisting = false && editConfiguration.propertyOfferCreateNewOption = false>
<p>This property is currently configured to prohibit editing. </p>
</#if>


<#if editConfiguration.includeDeletionForm = true>
<#include "defaultDeletePropertyForm.ftl">
</#if>

