<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<section id="pageList">
    <div class="tab">
        <h2>Page Management</h2>
    </div>


<#if pages?has_content >  
<table id="pageList" style="margin-bottom:2px">  <caption>Page Management</caption>
  
    <thead>
      <tr>
        <th scope="col">Title</th>
        <!--th scope="col">Type</th-->
        <th scope="col">URL</th>
        <th scope="col">Custom Template</th>
        <th id="isMenuPage" scope="col" >Menu Page</th>
        <th id="iconColumns" scope="col">&nbsp;</th>
      </tr>
    </thead>
    
    <tbody>
    <#list pages as pagex>      
    	 <tr>                
            <td> 
            	<#if pagex.listedPageUri?has_content> 
            	    <#if pagex.listedPageTitle == "Home" >
            	        ${pagex.listedPageTitle!}
            	    <#else>
            		<a href="${urls.base}/editRequestDispatch?subjectUri=${pagex.listedPageUri?url}&switchToDisplayModel=1&editForm=edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.ManagePageGenerator">${(pagex.listedPageTitle)!'-untitled-'}</a>
            		</#if>
            		
            	<#else>
            		No URI defined for page. 
            	</#if>
            </td>                  
            <!--td> {pagex.dataGetterLabel}</td-->
            <td>${pagex.listedPageUrlMapping}</td>
            <td>${(pagex.listedPageTemplate)!''}</td>
            <td style="text-align:center">
            <#if pagex.listedPageMenuItem?has_content>
            	<div class="menuFlag"></div>
            </#if>
            </td>
            <td>
                <a href="${urls.base}/individual?uri=${pagex.listedPageUri?url}&switchToDisplayModel=1"><img src="${urls.images!}/profile-page-icon.png" title="view the profile properties for this page" alt="profile page"></a>
                &nbsp;&nbsp;
<#--                <#if pagex.listedPageCannotDeletePage?? >
                <#else>
                    <a href="#"><img src="${urls.images!}/individual/deleteIcon.gif" title="delete this page" alt="delete"></a>
                </#if>
-->            </td>
        </tr>    
    
 
    </#list>   
    </tbody> 
  </table>
  
<#else>
    <p>There are no pages defined yet.</p>
</#if>
  
  <form id="pageListForm" action="${urls.base}/editRequestDispatch" method="get">
      <input type="hidden" name="typeOfNew" value="http://vitro.mannlib.cornell.edu/ontologies/display/1.1#Page">              
      <input type="hidden" name="switchToDisplayModel" value="1">
      <input type="hidden" name="editForm" value="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.ManagePageGenerator" role="input">
 	<input id="submit" value="Add Page" role="button" type="submit" >
  </form>
  <br />
 <p style="margin-top:10px">Use <a id="menuMgmtLink" href="#">Menu Management</a> to set the order of menu items.</p>
</section>


${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/pageList.css" />',
                  '<link rel="stylesheet" href="${urls.base}/css/menumanagement/menuManagement.css" />')}


