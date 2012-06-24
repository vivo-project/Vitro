<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#import "lib-vivo-form.ftl" as lvf>

<#--------Set up variables-------->
<#assign pageData = editConfiguration.pageData />
<#assign menuAction = pageData.menuAction />

<#assign pageName = "" />
<#assign selectedTemplateType = "default" />
<#assign prettyUrl = ""/>
<#assign menuItem = ""/>    	
<#assign menuLinkText = "" />
<#assign menuPosition = pageData.highestMenuPosition />
<#--Existing Values For Editing condition-->
<#assign literalValues = editConfiguration.existingLiteralValues />
<#assign uriValues = editConfiguration.existingUriValues />
<#if menuAction = "Edit"> 
	<#assign pageName = lvf.getFormFieldValue(editSubmission, editConfiguration, "pageName")/>
	<#assign prettyUrl = lvf.getFormFieldValue(editSubmission, editConfiguration, "prettyUrl")/>
	<#assign menuItem =  lvf.getFormFieldValue(editSubmission, editConfiguration, "menuItem")/>
	<#assign menuLinkText =  lvf.getFormFieldValue(editSubmission, editConfiguration, "menuLinkText")/>
	<#assign customTemplate = lvf.getFormFieldValue(editSubmission, editConfiguration, "customTemplate")/>
	<#if customTemplate?has_content>
		<#assign selectedTemplateType = "custom" />
	</#if>
	<#assign editMenuPosition =  lvf.getFormFieldValue(editSubmission, editConfiguration, "menuPosition")/>
	<#--if menu position exists for a menu item, then use that, otherwise use the highest available menu position number from page data-->
	<#if editMenuPosition?has_content && editMenuPosition != "">
		<#assign menuPosition = editMenuPosition/>
	</#if>	
</#if>

<#------------HTML Portion------------->
<section id="error-alert" role="alert" class="hidden">
    <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon" />
    <p></p>
</section>

<#--class group section has associated page uri, but that depends on editing a current page or if one is selected later-->
<section id="pageDetails">
    <#--form method="POST" action="${formUrls}" role="${menuAction} menu item"-->
	<form method="POST" action="${submitUrl}" role="add page">
	        <input type="hidden" name="switchToDisplayModel" id="switchToDisplayModel" value="1" role="input" />
	        <input type="hidden" id="editKey" name="editKey" value="${editKey}" />
			<input type="hidden" id="menuItem" name="menuItem" value="${menuItem}"/>
    <h2>Add Page</h2>
    <!--Drop down for the types of content possible-->
    <section id="floatRight">
        <div id="rightSide">
            <section id="addPageOne" role="region" >
                <label for="last-name">Content Type<span class="requiredHint"> *</span></label> 
                <select id="typeSelect"  name="typeSelect" >
                    <option value="" selected="selected">Select one</option>
                    <option value="browseClassGroup">Browse Class Group</option>           
                    <option value="fixedHtml">Fixed HTML</option>           
                    <option value="sparqlQuery">SPARQL Query Results</option>           
                 </select>
            </section>
            <section id="contentDivs"></section>
            <section id="headerBar" >
            </section>
            
            <#--This include file contains links to the templates that will be cloned and used for the different content types-->
			 <!--This content will be copied/shown for these particular content types, so any fields for n3 editing need to be included
            here that correspond to a specific content type.  These are related to specific "data getters" on the server side.  -->
			<#include "pageManagement--contentTemplates.ftl">          
            <input  type="button" id="moreContent" name="moreContent" value="Add More Content" class="delete" />          
        </div>
    </section>
    <!--Information for page or menu item level-->
    <div id="leftSide">
        <section id="pageDetails" role="region" >
            <label for="page-name">Title<span class="requiredHint"> *</span></label>
            <input type="text" name="pageName" value="${pageName!''}" role="input" />
            <label for="pretty-url">Pretty URL<span class="requiredHint"> *</span></label> 
            <input type="text" name="prettyUrl" value="${prettyUrl!''}" role="input" />
            <p class="note">Must begin with a leading forward slash: / <br />(e.g., /people)</p>
            <p id="templatePTag">Template<span class="requiredHint"> *</span></p>
            <input type="radio" class="default-template" name="selectedTemplate" value="default" <#if selectedTemplateType = "default">checked="checked"</#if> role="radio" />
            <label class="inline" for="default"> Default</label>
            <br />
            <input type="radio" name="selectedTemplate" class="custom-template" value="custom" <#if selectedTemplateType = "custom">checked="checked"</#if> role="input" />
            <label class="inline" for="custom"> Custom template</label>
            <section id="custom-template" <#if selectedTemplateType != 'custom'>class="hidden" </#if>role="region">
                <input type="text" name="customTemplate" value="${customTemplate!''}" size="40" role="input" /><span class="requiredHint"> *</span>
            </section>
            <p id="checkboxPTag"><input id="menuCheckbox" type="checkbox" name="menuCheckbox"
            <#if menuAction="Edit" && menuItem?has_content>checked="checked"</#if>
            > This is a menu page</p>
            <section id="menu" role="region" 
            <#--Do not display menu section unless editing an existing menu item-->
            <#if menuAction = "Add" || (menuAction="Edit" && (!menuItem?has_content || menuItem = "")) >
            class="hideMenuSection"
            <#else>
            class="showMenuSection"
            </#if>
            >
                <label for="default">Menu Item Name</label>
                <input type="hidden" id="menuItem" name="menuItem" value="${menuItem!''}" />
                <input type="text" id="menuLinkText" name="menuLinkText" value="${menuLinkText!''}" size="28" role="input" />
                <input type="hidden" id="menuPosition" name="menuPosition" value="${menuPosition!''}" />
                <p class="note">If left blank, the page title will be used.</p>
            </section>
            <br />
        </section>
    </div>
    <section >
        <span id="saveButton" ><input  id="pageSave" type="submit" name="submit-Add" value="Save changes" class="submit" role="input" /> or </span> 
        <a class="cancel" href="#"  id="cancelPage" >Cancel</a>
        <br />
        <p class="requiredHint">* required fields</p>
    </section>
    <!--Hidden input with JSON objects added will be included here.  This is the field with the page content information
    mirroring what is required by the Data getter server side objects. -->
    <div id="pageContentSubmissionInputs" style="display:none"></div>
    <!--For existing content, will have div to save existing content-->
    <div id="existingPageContent" style="display:none">
    <#if pageData.existingPageContentUnits?has_content>
    	<input type='hidden' id='existingPageContentUnits' value='${pageData.existingPageContentUnits}'/>
    </#if>
    </div>
    </form>
</section>


<!--Hardcoding for now but should be retrieved from generator: Custom data-->
<#include "pageManagement--customDataScript.ftl">
 


${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/menuManagement.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/pageManagement.css" />')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery.fix.clone.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/json2.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/browserUtils.js"></script>')}
<#--Process Data Getter Utils will refer to the various content type specific javascript files that should
already have been added within the template section for each content type-->
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/processDataGetterUtils.js"></script>')}
<#--Page management is used on page load and utilizes processDataGetterUtils as well as the custom data from the custom data script-->
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/pageManagementUtils.js"></script>')}

