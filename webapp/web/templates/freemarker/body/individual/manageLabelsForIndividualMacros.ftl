<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#--LabelsSorted is a hash keyed by language name where the value is a list of LabelInformation class objects-->
<#macro displayExistingLabelsForLanguage lang labelsSorted editable editGenerator>
	<#--get label information for this language-->
	<#assign labelList = labelsSorted[lang] />
	<#--Reset for every language-->
	<#assign labelSeq = []/>
	<#list labelList as labelObject>
		<#assign labelLiteral = labelObject.labelLiteral />
		<#assign labelStringValue = labelObject.labelStringValue />
		<#--Try label as label literal-->
		<#assign label = labelLiteral />
		<#assign labelLang = labelObject.languageName />
		<#assign languageCode = labelObject.languageCode />
		<#assign labelEditLink = labelObject.editLinkURL />
		<#if label?? && ( label?index_of("@") > -1 ) >
	    	<#assign labelStr = label?substring(0, label?index_of("@")) >
	    	<#assign tagOrTypeStr = label?substring(label?index_of("@")) >
	    <#elseif label?? && ( label?index_of("^^") > -1 ) >
	        <#assign labelStr = label?substring(0, label?index_of("^^")) >
	        <#assign tagOrTypeStr = label?substring(label?index_of("^^")) >
	        <#assign tagOrTypeStr = tagOrTypeStr?replace("^^http","^^<http") >
	        <#assign tagOrTypeStr = tagOrTypeStr?replace("#string","#string>") >
	    <#else>
	        <#assign labelStr = label >
	        <#assign tagOrTypeStr = "" >
	    </#if>
	    <li>${labelStr} <#if labelSeq?seq_contains(labelStr)> (duplicate value) </#if> 
	    <#if editable && labelEditLink?has_content> <a href="${labelEditLink}&${editGenerator}">Edit</a>
		<a href="${urls.base}/edit/primitiveRdfEdit" languageName="${labelLang}" languageCode="${languageCode}"
		labelValue="${labelStr}" tagOrType="${tagOrTypeStr!}" class="remove" title="${i18n().remove_capitalized}">${i18n().remove_capitalized}</a>
		</#if>
	    
	    </li>
		<#assign labelSeq = labelSeq + [labelStr]>
	</#list>
</#macro>

<#--ignore 'untyped' and display everything-->
<#macro displayExistingTypedLabels langList labelsSorted editable editGenerator>
	<#list langList as lang>
		<#if lang != "untyped">
			<h3 languageName="${lang}">${lang}</h3>
			<#--get label information for this language-->
			<#assign labelList = labelsSorted[lang] />
			<#--Reset for every language-->
			<#assign labelSeq = []/>
			<#list labelList as labelObject>
				<#assign labelLiteral = labelObject.labelLiteral />
				<#assign labelStringValue = labelObject.labelStringValue />
				<#--Try label as label literal-->
				<#assign label = labelLiteral />
				<#assign labelLang = labelObject.languageName />
				<#assign languageCode = labelObject.languageCode />
				<#assign labelEditLink = labelObject.editLinkURL />
				<#if label?? && ( label?index_of("@") > -1 ) >
			    	<#assign labelStr = label?substring(0, label?index_of("@")) >
			    	<#assign tagOrTypeStr = label?substring(label?index_of("@")) >
			    <#elseif label?? && ( label?index_of("^^") > -1 ) >
			        <#assign labelStr = label?substring(0, label?index_of("^^")) >
			        <#assign tagOrTypeStr = label?substring(label?index_of("^^")) >
			        <#assign tagOrTypeStr = tagOrTypeStr?replace("^^http","^^<http") >
			        <#assign tagOrTypeStr = tagOrTypeStr?replace("#string","#string>") >
			    <#else>
			        <#assign labelStr = label >
			        <#assign tagOrTypeStr = "" >
			    </#if>
			    <li>${labelStr} <#if labelSeq?seq_contains(labelStr)> (duplicate value) </#if> 
			    <#if editable && labelEditLink?has_content> <a href="${labelEditLink}&${editGenerator}">Edit</a>
				<a href="${urls.base}/edit/primitiveRdfEdit" languageName="${labelLang}" languageCode="${languageCode}"
				labelValue="${labelStr}" tagOrType="${tagOrTypeStr!}" class="remove" title="${i18n().remove_capitalized}">${i18n().remove_capitalized}</a>
				</#if>
			    
			    </li>
				<#assign labelSeq = labelSeq + [labelStr]>
			</#list>
		</#if>
	</#list>
</#macro>