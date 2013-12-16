<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#include "manageLabelsForIndividualTerms.ftl" >
<#-- Custom form for managing labels for individuals -->
<#--This is used both for editing and for viewLabelsServlet-->
<#import "manageLabelsForIndividualMacros.ftl" as m >
<#assign requiredHint = "<span class='requiredHint'> *</span>" />
<#assign labelStr = "" >
<#assign languageTag = "" >
<#assign labelSeq = [] >
<#assign editable = false/>
<#assign displayRemoveLink = false/>

<#if subjectName?? >
<h2>${i18n().view_labels_for} ${subjectName}</h2>
<#else>
<h2>${i18n().view_labels_capitalized}</h2>
</#if>


    <section id="rdfsLabels" role="container">
    
        <ul id="existingLabelsList" name="existingLabelsList">
        <#if labelsSortedByLanguageName?has_content>
        	<#--List of labelInformation objects as value where key = language name -->
        	<#assign labelsSorted = labelsSortedByLanguageName />
        	<#--Keys would be the actual names of languages-->
        	<#assign labelLanguages = labelsSorted?keys?sort />
        	
        	<#--What we need here is printing out the labels by the language Name and not language code, starting with untyped first-->
        	<@m.displayExistingLabelsForLanguage "untyped" labelsSorted editable ""/>
        	<@m.displayExistingTypedLabels  labelLanguages labelsSorted editable ""/>
        	
        </#if>
        </ul>

	</section>
	    



${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/utils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>')}
              
