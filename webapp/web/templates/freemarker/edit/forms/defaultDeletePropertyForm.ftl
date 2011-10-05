<form class="deleteForm" action="${editConfiguration.mainEditUrl}" method="get">       
 	<label for="delete"><h3 class="delete-entry">Delete this entry?</h3></label>
    <input type="hidden" name="subjectUri"   value="${editConfiguration.subjectUri}"/>
    <input type="hidden" name="predicateUri" value="${editConfiguration.predicateUri}"/>
    <input type="hidden" name="cmd"          value="delete"/>
    
   	<#if editConfiguration.dataProperty = true>
    	<input type="hidden" name="datapropKey" value="${editConfiguration.datapropKey}" />
    	<input type="submit" id="delete" value="Delete">
    	<a class="cancel">Cancel</a>   
    </#if>
    
    <#--The original jsp included vinput tag with cancel=empty string for case where both select from existing
    and offer create new option are true below
    so leaving as Cancel for now but unclear as to what the result would have been-->
    <#if editConfiguration.objectProperty = true> 
   	    <input type="hidden" name="objectUri"    value="${editConfiguration.objectUri}"/>    
   	
	   	<#if editConfiguration.propertySelectFromExisting = false 
	    	 && editConfiguration.propertyOfferCreateNewOption = false>
	      <input type="submit" id="delete" value="Delete">
	        <a class="cancel">Cancel</a>   
	    </#if>
	    
	    <#if editConfiguration.propertySelectFromExisting = true 
	    && editConfiguration.propertyOfferCreateNewOption = true>
	    	<input type="submit" id="delete" value="Delete">
	         <a class="cancel">Cancel</a>      
	    </#if>
    
    </#if>
</form>
