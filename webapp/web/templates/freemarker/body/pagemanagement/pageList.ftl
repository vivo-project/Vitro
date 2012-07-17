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
        <th id="iconColumns" scope="col">Controls</th>
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
                <a href="${urls.base}/editRequestDispatch?subjectUri=${pagex.listedPageUri?url}&switchToDisplayModel=1&editForm=edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.ManagePageGenerator"><img src="${urls.images!}/individual/editIcon.gif" title="edit this page" alt="edit"></a>
                &nbsp;&nbsp;
                <a href="${urls.base}/individual?uri=${pagex.listedPageUri?url}&switchToDisplayModel=1"><img src="${urls.images!}/profile-page-icon.png" title="view the individual profile for this page" alt="profile page"></a>
                &nbsp;&nbsp;
                <#if !pagex.listedPageCannotDeletePage?has_content >
                    <a cmd="deletePage" pageTitle=" ${pagex.listedPageTitle!}"  href="${urls.base}/deletePageController?pageURI=${pagex.listedPageUri?url}"><img src="${urls.images!}/individual/deleteIcon.gif" title="delete this page" alt="delete"></a>
                </#if>
            </td>
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
 <p style="margin-top:10px">Use <a id="menuMgmtLink" href="${urls.base}/individual?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fontologies%2Fdisplay%2F1.1%23DefaultMenu&switchToDisplayModel=true">Menu Ordering</a> to set the order of menu items.</p>
</section>


${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />',
				'<link rel="stylesheet" href="${urls.base}/css/menupage/pageList.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/browserUtils.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/pageDeletion.js"></script>')}


