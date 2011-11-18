 <#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
 
 <#if rangeOptionsExist  = true >
        <p>If you don't find the appropriate entry on the selection list above:</p>
 <#else>
        <p>Please create a new entry.</p>           
 </#if>
 
 <#if editConfiguration.objectUri?has_content>
    <#assign objectUri = editConfiguration.objectUri>
 <#else>
    <#assign objectUri = ""/>
 </#if>
 
<#assign typesList = editConfiguration.offerTypesCreateNew />
<form class="editForm" action="${editConfiguration.mainEditUrl}" role="input" />        
    <input type="hidden" value="${editConfiguration.subjectUri}" name="subjectUri" role="input" />  
    <input type="hidden" value="${editConfiguration.predicateUri}" name="predicateUri" role="input" />  
    <input type="hidden" value="${objectUri}" name="objectUri" role="input" />      
    <input type="hidden" value="create" name="cmd" role="input" />     
        
    <select id="typeOfNew" name="typeOfNew" role="selection">
    <#assign typeKeys = typesList?keys />
    <#list typeKeys as typeKey>
        <option value="${typeKey}" role="option"> ${typesList[typeKey]} </option>
    </#list>
    </select>
    
    <input type="submit" id="submit" value="Add a new item of this type" role="button" />  
    <#if rangeOptionsExist  = false >
        <span class="or"> or </span>
        <a title="Cancel" class="cancel" href="${cancelUrl}">Cancel</a>
    </#if>
</form>          

               