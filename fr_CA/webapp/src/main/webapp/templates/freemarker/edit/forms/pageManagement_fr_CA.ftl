<#-- $This file is distributed under the terms of the license in LICENSE$ -->
<#import "lib-vitro-form.ftl" as lvf>

<#--------Set up variables-------->
<#assign pageData = editConfiguration.pageData />
<#assign menuAction = pageData.menuAction />
<#assign pageAvailablePermissions = pageData.availablePermissions />
<#assign pageAvailablePermissionsURIsList = pageData.availablePermissionOrderedList />

<#assign pageName = "" />
<#assign selectedTemplateType = "default" />
<#assign prettyUrl = ""/>
<#assign menuItem = ""/>
<#assign menuLinkText = "" />
<#assign action = "" />
<#assign menuPosition = pageData.highestMenuPosition />
<#assign addMenuItem = "" />
<#assign pageHeading = "${i18n().add_new_page}" />
<#assign saveBtnText = "${i18n().save_new_page}" />
<#if pageData.addMenuItem?has_content>
	<#assign addMenuItem = pageData.addMenuItem />
</#if>
<#--Existing Values For Editing condition-->
<#assign literalValues = editConfiguration.existingLiteralValues />
<#assign uriValues = editConfiguration.existingUriValues />
<#if menuAction = "Edit">
	<#assign pageName = lvf.getFormFieldValue(editSubmission, editConfiguration, "pageName")/>
	<#assign prettyUrl = lvf.getFormFieldValue(editSubmission, editConfiguration, "prettyUrl")/>
	<#assign menuItem =  lvf.getFormFieldValue(editSubmission, editConfiguration, "menuItem")/>
	<#assign menuLinkText =  lvf.getFormFieldValue(editSubmission, editConfiguration, "menuLinkText")/>
	<#assign customTemplate = lvf.getFormFieldValue(editSubmission, editConfiguration, "customTemplate")/>
	<#assign selfContainedTemplate = lvf.getFormFieldValue(editSubmission, editConfiguration, "isSelfContainedTemplate")/>
	<#assign action = lvf.getFormFieldValue(editSubmission, editConfiguration, "action")/>

	<#assign pageHeading = "${i18n().edit_page(pageName)}" />
    <#assign saveBtnText = "${i18n().save_changes}" />
	<#if customTemplate?has_content>
	    <#if selfContainedTemplate?has_content>
		    <#assign selectedTemplateType = "selfContained" />
    	<#else>
            <#assign selectedTemplateType = "custom" />
        </#if>
	</#if>
	<#assign editMenuPosition =  lvf.getFormFieldValue(editSubmission, editConfiguration, "menuPosition")/>
	<#--if menu position exists for a menu item, then use that, otherwise use the highest available menu position number from page data-->
	<#if editMenuPosition?has_content && editMenuPosition != "">
		<#assign menuPosition = editMenuPosition/>
	</#if>
</#if>
<#--If edit submission exists, then retrieve validation errors if they exist-->
<#if editSubmission?has_content && editSubmission.submissionExists = true && editSubmission.validationErrors?has_content>
	<#assign submissionErrors = editSubmission.validationErrors/>
</#if>

<#------------HTML Portion------------->
<section id="error-alert" role="alert" <#if !submissionErrors?has_content>class="hidden"</#if>>
    <img src="${urls.images}/iconAlert.png" width="24" height="24" alt="${i18n().error_alert_icon}" />
    <p>
    <#if submissionErrors?has_content>
    	<#list submissionErrors?keys as errorFieldName>
    		${errorFieldName} : ${submissionErrors[errorFieldName]} <br/ >
        </#list>
    </#if>
    </p>
</section>

<#--class group section has associated page uri, but that depends on editing a current page or if one is selected later-->
<section id="pageDetailsContainer">
    <#--form method="POST" action="${formUrls}" role="${menuAction} menu item"-->
	<form id="managePage" method="POST" action="${submitUrl}" role="add page">
	        <input type="hidden" name="switchToDisplayModel" id="switchToDisplayModel" value="1" role="input" />
	        <input type="hidden" id="editKey" name="editKey" value="${editKey}" />
    <h2>${pageHeading}</h2>
    <!--Drop down for the types of content possible-->
    <section id="floatRight">
        <div id="rightSide" <#if selectedTemplateType="selfContained">style="display:none;"</#if>>
            <section id="addPageOne" role="region" >
            <label for="contentType">${i18n().content_type}<span class="requiredHint"> *</span></label>
            <select id="typeSelect"  name="typeSelect" >
                <option value="" selected="selected">${i18n().select_type}</option>
                <option value="browseClassGroup">${i18n().browse_class_group}</option>
                <option value="fixedHtml">${i18n().fixed_html}</option>
                <option value="sparqlQuery">${i18n().sparql_query_results}</option>
                <option value="searchIndividuals">${i18n().search_individual_results}</option>
             </select>&nbsp;<span class="note">${i18n().add_types}</span>
            </section>
            <section id="contentDivs"></section>
            <section id="headerBar" >
            </section>

            <#--This include file contains links to the templates that will be cloned and used for the different content types-->
			 <!--This content will be copied/shown for these particular content types, so any fields for n3 editing need to be included
            here that correspond to a specific content type.  These are related to specific "data getters" on the server side.  -->
			<#include "pageManagement--contentTemplates.ftl">
        </div>
    </section>
    <!--Information for page or menu item level-->
    <div id="leftSide">
        <section id="pageDetails" role="region" >
            <label for="page-name">${i18n().title_capitalized}<span class="requiredHint"> *</span></label>
            <input id="pageName" type="text" name="pageName" value="${pageName!''}" role="input" />
            <label for="pretty-url">${i18n().pretty_url}<span class="requiredHint"> *</span></label>
            <input type="text" name="prettyUrl" value="${prettyUrl!''}" role="input" />
            <p class="note">${i18n().begin_with_slash_no_example} <br />${i18n().slash_example}</p>
            <p id="templatePTag">${i18n().template_capitalized}<span class="requiredHint"> *</span></p>
            <input type="radio" class="default-template" name="selectedTemplate" value="default" <#if selectedTemplateType = "default">checked="checked"</#if> role="radio" />
            <label class="inline" for="default"> ${i18n().default}</label>
            <br />
            <input type="radio" name="selectedTemplate" class="custom-template" value="custom" <#if selectedTemplateType = "custom">checked="checked"</#if> role="input" />
            <label class="inline" for="custom"> ${i18n().custom_template_requiring_content}</label>
            <br /><div id="selfContainedDiv">
            <input type="radio" name="selectedTemplate" class="selfContained-template" value="selfContained" <#if selectedTemplateType = "selfContained">checked="checked"</#if> role="input" />
            <label class="inline" for="selfContained"> ${i18n().custom_template_containing_content}</label></div>
            <section id="custom-template" <#if selectedTemplateType ="default">class="hidden" </#if>role="region">
                <input type="text" name="customTemplate" value="${customTemplate!''}" size="33" role="input" /><span class="requiredHint"> *</span>
                <input type="hidden" name="selfContainedTemplate" value="${selfContainedTemplate!''}"/>
            </section>
            <p id="menuCheckboxPTag"><input id="menuCheckbox" type="checkbox" name="menuCheckbox"
            <#if (menuAction="Edit" && menuItem?has_content) || (menuAction="Add" && addMenuItem = "true")>checked="checked"</#if>
            > ${i18n().a_menu_page}</p>
            <section id="menu" role="region"
            <#--Do not display menu section unless editing an existing menu item-->
            <#if (menuAction = "Add" && addMenuItem != "true")  || (menuAction="Edit" && (!menuItem?has_content || menuItem = "")) >
            class="hideMenuSection"
            <#else>
            class="showMenuSection"
            </#if>
            >
                <label for="default">${i18n().menu_item_name}</label>

                <input type="hidden" id="menuItem" name="menuItem" value="${menuItem!''}" />
                <input type="text" id="menuLinkText" name="menuLinkText" value="${menuLinkText!''}" size="28" role="input" />
                <input type="hidden" id="menuPosition" name="menuPosition" value="${menuPosition!''}" />


                <p class="note">${i18n().if_blank_page_title_used}</p>


            </section>
            <section id="pagePermissions">
            <br/>
              <label for="action">${i18n().page_select_permission}</label>

                <select id="action" name="action">
                	<option value="">${i18n().page_select_permission_option}</option>
                <#list pageAvailablePermissionsURIsList as permissionURI>
                	<option value="${permissionURI}"<#if action=permissionURI> selected="selected"</#if>>${pageAvailablePermissions[permissionURI]}</option>
                </#list>
           		</select>
            </section>
            <br />
        </section>
    </div>
    <section >
        <span id="saveButton" ><input  id="pageSave" type="submit" name="submit-Add" value="${saveBtnText}" class="submit" role="input" /> or </span>
        <a class="cancel" href="${cancelUrl!}"  id="cancelPage" title="${i18n().cancel_title}">${i18n().cancel_link}</a>
        <br />
        <p class="requiredHint">* ${i18n().required_fields}</p>
    </section>
    <!--Hidden input with JSON objects added will be included here.  This is the field with the page content information
    mirroring what is required by the Data getter server side objects. -->
    <div id="pageContentSubmissionInputs" style="display:none"></div>
    <!--For existing content, will have div to save existing content-->
    <div id="existingPageContent" style="display:none">
    <#if pageData.existingPageContentUnits?has_content>
    	<!--Using the ?html ensures that single and double quotes are html encoded - double quotes
    	if left unencoded will break the html and lead to errors-->
    	<input type='hidden' id='existingPageContentUnits' value='${pageData.existingPageContentUnits?html}'/>
    </#if>
    </div>
    </form>
</section>


<!--Hardcoding for now but should be retrieved from generator: Custom data-->
<#include "pageManagement--customDataScript.ftl">
<script>
    var i18nStrings = {
        browseClassGroup: "${i18n().browse_class_group?js_string}",
        fixedHtml: "${i18n().fixed_html?js_string}",
        sparqlResults: "${i18n().sparql_query_results?js_string}",
        searchIndividuals: "${i18n().search_individual_results?js_string}",
        orString: "${i18n().or?js_string}",
        deleteString: "${i18n().delete?js_string}",
        allCapitalized: "${i18n().all_capitalized?js_string}",
        mapProcessorError: "${i18n().map_processor_error?js_string}",
        codeProcessingError: "${i18n().code_processing_error?js_string}",
        supplyName: "${i18n().supply_name?js_string}",
        supplyPrettyUrl: "${i18n().supply_url?js_string}",
        startUrlWithSlash: "${i18n().start_url_with_slash?js_string}",
        supplyTemplate: "${i18n().supply_template?js_string}",
        selectContentType: "${i18n().select_content_type?js_string}",
        multipleContentWithDefaultTemplateError: "${i18n().multiple_content_default_template_error?js_string}"
    };
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/menuManagement.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/pageManagement.css" />')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.12.1.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery.fix.clone.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/json2.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/browserUtils.js"></script>')}
<#--Process Data Getter Utils will refer to the various content type specific javascript files that should
already have been added within the template section for each content type-->
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/processDataGetterUtils.js"></script>')}
<#--Page management is used on page load and utilizes processDataGetterUtils as well as the custom data from the custom data script-->
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/pageManagementUtils.js"></script>')}

