 <#if editConfiguration.rangeOptionsExist  = true >
     	<p style="margin-top: 2.2em">If you don't find the appropriate entry on the selection list above:</p>
 <#else>
   		<p style="margin-top: 5em">Please create a new entry.</p>  		    
 </#if>
 
<#assign typesList = editConfiguration.offerTypesCreateNew />
<form class="editForm" action="{editConfiguration.mainEditUrl}">        
        <input type="hidden" value="${editConfiguration.subjectUri}" name="subjectUri"/>
        <input type="hidden" value="${editConfiguration.predicateUri}" name="predicateUri"/>
        <input type="hidden" value="${editConfiguration.objectUri}" name="objectUri"/>        
		<input type="hidden" value="create" name="cmd"/>   
		<select id="typeOfNew" name="typeOfNew">
		<#assign typeKeys = typesList?keys />
		<#list typeKeys as typeKey>
			<option value="${typeKey}"> ${typesList[typeKey]} </option>
		</#list>
		</select>
		     
        <input type="submit" name="submit" id="submit" value="Add a new item of this type" />
        <a class="cancel">Cancel</a>
	</form>                            