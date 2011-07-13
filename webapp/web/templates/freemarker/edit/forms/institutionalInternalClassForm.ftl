<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- 
Institutional Internal Class Form 
To be associated later (upon completion of N3 Refactoring) with 
edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.InstitutionalInternalClassForm.
-->



<h3>Institutional Internal Class</h3>

<section id="introMessage" role="region">
	This class will be used to designate those individuals internal to your institution.
	This will allow you to limit the individuals displayed on your menu pages (People, Research, etc.) 
	to only those within your institution.
</section>

<section>
       <form method="POST" action="${formUrl}" class="customForm">
       <input type="hidden" name="submitForm" id="submitForm" value="true" />
       <#if ontologiesExist = false>
        <section id="noLocalOntologyExists">
        	${noLocalOntologiesMessage}
        </section>
       
       	<#elseif useExistingInternalClass?has_content> 
        <section id="existingLocalClass">
        	<#--Populated based on class list returned-->
        	<select id="existingLocalClasses" name="existingLocalClasses">
	        	<#list localClasses as localClass>
	                    <option value="${localClass.URI}" <#if existingInternalClass.URI = localClass.URI>selected</#if> >${localClass.name}</option>
	            </#list>
        	</select>
        </section>
        <#elseif createNewClass?has_content>
        <section id="createNewLocalClass">
        	<h2>Create a new class</h2>
         	<label for="menu-name">Name<span class="requiredHint"> *</span></label>
        	<input type="text" id="localClassName" name="localClassName" value="" />
        	
        	<#--If more than one local namespace, generate select-->
        	<#if multipleLocalNamespaces = true>
        	<select id="existingLocalNamespaces" name="existingLocalNamespaces">
	        	<#list existingLocalNamespaces as existingNamespace>
	                  <option value="${existingNamespace.URI}">"${existingNamespace.URI}"</option>
	            </#list>
        	</select>
        	<#else>
        		<input type="hidden" id="existingLocalNamespaces" name="existingLocalNamespaces" value="{existingLocalNamespaces[0]}"/>
        	</#if>
        	
        </section>
        <#else>
        	Problematic section as above should all have been handled
        </#if>
        <input type="submit" name="submit-internalClass" value="${submitAction}" class="submit" /> or <a class="cancel" href="${cancelUrl}">Cancel</a>
		<p class="requiredHint">* required fields</p>
    </form>
</section>

<#-- Add necessary css files associated with this page
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/institutional.css" />')}-->
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}

<#-- Add necessary javascript files associated with this page 
${scripts.add('<script type="text/javascript" src="${urls.base}/js/institutional.js"></script>')}   
-->
