<form class="deleteForm" action="${editConfiguration.mainEditUrl}" method="get">       
 	<label for="delete"><h3 class="delete-entry">Delete this entry?</h3></label>
    <input type="hidden" name="subjectUri"   value="${editConfiguration.subjectUri}"/>
    <input type="hidden" name="predicateUri" value="${editConfiguration.predicateUri}"/>
    <input type="hidden" name="cmd"          value="delete"/>
    <input type="hidden" name="editKey" value="${editConfiguration.editKey}"/>
   	<#if editConfiguration.dataProperty = true>
    	<input type="hidden" name="datapropKey" value="${editConfiguration.datapropKey}" />
    	<div style="margin-top: 0.2em">
			<input type="submit" id="delete" value="Delete">
		</div>     
    	
    </#if>
    
    <#--The original jsp included vinput tag with cancel=empty string for case where both select from existing
    and offer create new option are true below
    so leaving as Cancel for first option but not second below-->
    <#if editConfiguration.objectProperty = true> 
   	    <input type="hidden" name="objectUri"    value="${editConfiguration.objectUri}"/>    
   	
	   	<#if editConfiguration.propertySelectFromExisting = false 
	    	 && editConfiguration.propertyOfferCreateNewOption = false>
	      	<div style="margin-top: 0.2em">
				<input type="submit" id="delete" value="Delete">
				<span class="or"> or </span>
				<a title="Cancel" href="${editConfiguration.cancelUrl}">Cancel</a>
			</div>     
	    </#if>
	    
	    <#if editConfiguration.propertySelectFromExisting = true 
	    && editConfiguration.propertyOfferCreateNewOption = true>
	    	<div style="margin-top: 0.2em">
				<input type="submit" id="delete" value="Delete">
			</div>       
	    </#if>
    
    </#if>
</form>
