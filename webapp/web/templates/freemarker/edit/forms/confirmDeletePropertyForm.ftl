<#assign toBeDeletedClass = "dataProp" />
<#if editConfiguration.objectProperty = true>
	<#assign toBeDeletedClass = "objProp" />
</#if>
<#assign statement = editConfiguration.statementDisplay />
<form action="${editConfiguration.deleteProcessingUrl}" method="get">
    <label for="submit">
    	<h2>Are you sure you want to delete the following entry from 
    		<em>${editConfiguration.propertyName}</em>?
    	</h2>
    </label>
    <div class="toBeDeleted ${toBeDeletedClass}">
    	<#if editConfiguration.objectProperty = true>
    		<#if statement.object?has_content>
    			<#include "propStatement-default.ftl" />
    		</#if>
    	<#else>
    		${statement.dataValue}
    	</#if>
   	</div>
    <input type="hidden" name="subjectUri"   value="${editConfiguration.subjectUri}"/>
    <input type="hidden" name="predicateUri" value="${editConfiguration.predicateUri}"/>
    
    <#if editConfiguration.dataProperty = true>
    	<input type="hidden" name="datapropKey" value="${editConfiguration.datapropKey}" />
    	<input type="hidden" name="vitroNsProp" value="${editConfiguration.vitroNsProperty}" />
    <#else>
    	<input type="hidden" name="objectUri"    value="${editConfiguration.objectUri}"/>
    </#if>
    
   
    <p class="submit">
    		<input type="submit" id="delete" value="Delete"/>
			<span class="or"> or </span>
			<a title="Cancel" href="${editConfiguration.cancelUrl}">Cancel</a>
    </p>
	
</form>