<#-- $This file is distributed under the terms of the license in LICENSE$ -->
<#include "manageLabelsForIndividualTerms.ftl" >
<#-- Custom form for managing labels for individuals -->
<#--This is used both for editing and for viewLabelsServlet-->
<#import "manageLabelsForIndividualMacros.ftl" as m >
<#assign requiredHint = "<span class='requiredHint'> *</span>" />
<#assign subjectUri = editConfiguration.subjectUri/>
<#assign labelStr = "" >
<#assign languageTag = "" >
<#assign labelSeq = [] >
<#assign submissionErrorsExist = "false"/>
<#assign selectLocalesFullList = {} />
<#assign editable = false/>
<#if editConfiguration.pageData.editable?has_content>
	<#assign editable = editConfiguration.pageData.editable />
</#if>
<#assign displayRemoveLink = true/>
<#if editConfiguration.pageData.displayRemoveLink?has_content>
	<#assign displayRemoveLink = editConfiguration.pageData.displayRemoveLink/>
</#if>
<#if editSubmission?has_content && editSubmission.submissionExists = true && editSubmission.validationErrors?has_content>
	<#assign submissionErrors = editSubmission.validationErrors/>
	<#assign submissionErrorsExist = "true" />
</#if>
<#assign availableLocalesNumber = 0/>
<#if editConfiguration.pageData.selectLocale?has_content>
	<#assign availableLocalesNumber = editConfiguration.pageData.selectLocale?size />
</#if>
<#if editConfiguration.pageData.subjectName?? >
<h2>${i18n().manage_labels_for} ${editConfiguration.pageData.subjectName}</h2>
<#else>
<h2>${i18n().manage_labels_capitalized}</h2>
</#if>



<p id="mngLabelsText">${i18n().manage_labels_intro}</p>


    <section id="rdfsLabels" role="container">

     <script type="text/javascript">
        var existingLabelsData = [];
    </script>

        <ul id="existingLabelsList" name="existingLabelsList">
        <#if editConfiguration.pageData.labelsSortedByLanguageName?has_content>
        	<#--List of labelInformation objects as value where key = language name -->
        	<#assign labelsSorted = editConfiguration.pageData.labelsSortedByLanguageName />
        	<#--Keys would be the actual names of languages-->
        	<#assign labelLanguages = labelsSorted?keys?sort />
        	<#assign editGenerator = "editForm=edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.RDFSLabelGenerator" />

        	<#--What we need here is printing out the labels by the language Name and not language code, starting with untyped first-->
        	<@m.displayExistingLabelsForLanguage "untyped" labelsSorted editable editGenerator/>
        	<@m.displayExistingTypedLabels  labelLanguages labelsSorted editable editGenerator/>

        </#if>
        </ul>

        <br />
        <p>

	    <#if editable>
		    <#include "manageLabelsForIndividualSubmissionErrors.ftl">
			<div id="showAddForm">
				<input type="submit" value="${i18n().add_label}" id="showAddFormButton" name="showAddFormButton">  ${i18n().or}
				<a class="cancel" href="${cancelUrl}&url=/individual" title="${returnText}">${returnText}</a>
			</div>
			<div id="showCancelOnly">
				<a class="cancel" href="${cancelUrl}&url=/individual" title="${returnText}">${returnText}</a>
			</div>
		    <#include "manageLabelsForIndividualAddForm.ftl" >
	    </#if>

		</p>
	</section>


<script type="text/javascript">
var selectLocalesFullList = [];
<#if editConfiguration.pageData.selectLocaleFullList?has_content>
	<#assign selectLocalesFullList = editConfiguration.pageData.selectLocaleFullList />
	<#list selectLocalesFullList as localeInfo>
		<#assign code = localeInfo["code"] />
		<#assign label= localeInfo["label"] />
		selectLocalesFullList.push({'code':'${code}', 'label':'${label}'});
	</#list>

</#if>

var customFormData = {
    processingUrl: '${urls.base}/edit/primitiveRdfEdit',
    individualUri: '${subjectUri!}',
    submissionErrorsExist: '${submissionErrorsExist}',
    selectLocalesFullList: selectLocalesFullList,
    numberAvailableLocales:${availableLocalesNumber}
};
var i18nStrings = {
    errorProcessingLabels: "${i18n().error_processing_labels?js_string}",
    selectLocaleOptionString : "${i18n().select_locale?js_string}"
};
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.12.1.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/utils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.12.1.min.js"></script>',
                '<script type="text/javascript" src="${urls.base}/js/individual/manageLabelsForIndividual.js"></script>')}

