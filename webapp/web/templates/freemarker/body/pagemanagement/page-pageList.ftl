<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<div>

<#if pages?has_content >  
  <table>
    <th>Title</th><th>URI</th>
    
    <#list pages as pagex>      
      <tr>                
        <td>${(pagex.title)!'-untitled-'}</td>                  
        <#if pagex.pageUri??>
            <td><a href="${urls.base}/individual?uri=${pagex.pageUri?url}&switchToDisplayModel=1">${pagex.pageUri}</a></td>
        <#else>        
            <td>URI for page not defined</td>
        </#if>                 
      </tr>    
    </#list>    
  </table>
  
<#else>
    <p>There are no pages defined yet.</p>
</#if>
  
  <form id="addIndividualClass" action="${urls.base}/editRequestDispatch" method="get">
      <input type="hidden" name="typeOfNew" value="http://vitro.mannlib.cornell.edu/ontologies/display/1.1#Page">              
      <input type="hidden" name="switchToDisplayModel" value="1">
      <input type="hidden" name="editForm" value="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.NewIndividualFormGenerator" role="input">
      <input type="submit" id="submit" value="Add Page" role="button">
  </form>

</div> 