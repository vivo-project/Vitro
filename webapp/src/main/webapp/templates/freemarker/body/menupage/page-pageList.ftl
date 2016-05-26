<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#import "lib-generator-classes.ftl" as generators />

<div>

<#if pages?has_content >  
  <table>
    <th>${i18n().title_capitalized}</th><th>URI</th>
    
    <#list pages as pagex>      
      <tr>                
        <td>${(pagex.title)!i18n().untitled}</td>                  
        <#if pagex.pageUri??>
            <td><a href="${urls.base}/individual?uri=${pagex.pageUri?url}&switchToDisplayModel=1" title="${i18n().page_uri}">${pagex.pageUri}</a></td>
        <#else>        
            <td>${i18n().uri_not_defined}</td>
        </#if>                 
      </tr>    
    </#list>    
  </table>
  
<#else>
    <p>${i18n().no_pages_defined}</p>
</#if>
  
  <form id="addIndividualClass" action="${urls.base}/editRequestDispatch" method="get">
      <input type="hidden" name="typeOfNew" value="http://vitro.mannlib.cornell.edu/ontologies/display/1.1#Page">              
      <input type="hidden" name="switchToDisplayModel" value="1">
      <input type="hidden" name="editForm" value="${generators.NewIndividualFormGenerator}" role="input">
      <input type="submit" id="submit" value="${i18n().add_page}" role="button">
  </form>

</div> 