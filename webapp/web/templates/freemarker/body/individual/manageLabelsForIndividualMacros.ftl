<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#--LabelsSorted is a hash keyed by language name where the value is a list of LabelInformation class objects-->
<#macro displayExistingLabelsForLanguage lang labelsSorted editable editGenerator="" displayRemoveLink=true>
	<#--get label information for this language-->
	<#if labelsSorted?keys?seq_contains(lang) >
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
			<#assign labelEditLink = ""/>
			<#if labelObject.editLinkURL?has_content>
				<#assign labelEditLink = labelObject.editLinkURL/>
			</#if>
		
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
		    <@displayLabel labelSeq labelStr languageCode labelEditLink tagOrTypeStr editGenerator editable displayRemoveLink/>
		    
		    <#assign labelSeq = labelSeq + [labelStr]>
		</#list>
	</#if>
</#macro>

<#--ignore 'untyped' and display everything-->
<#macro displayExistingTypedLabels langList labelsSorted editable editGenerator="" displayRemoveLink=true>
	
	
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
				<#assign labelEditLink = ""/>
				<#if labelObject.editLinkURL?has_content>
					<#assign labelEditLink = labelObject.editLinkURL/>
				</#if>
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
			    <@displayLabel labelSeq labelStr languageCode labelEditLink tagOrTypeStr editGenerator editable displayRemoveLink/>
				<#assign labelSeq = labelSeq + [labelStr]>
			</#list>
		</#if>
	</#list>
</#macro>

<#macro displayLabel labelSeq labelStr languageCode labelEditLink tagOrTypeStr editGenerator editable displayRemoveLink>
    <li>${labelStr} <#if labelSeq?seq_contains(labelStr)> (duplicate value) </#if> 
    <#if editable> <#if labelEditLink?has_content> <a href="${labelEditLink}&${editGenerator}">Edit</a></#if>
    <#if displayRemoveLink>
	<a href="${urls.base}/edit/primitiveRdfEdit" languageName="${labelLang}" languageCode="${languageCode}"
	labelValue="${labelStr}" tagOrType="${tagOrTypeStr!}" class="remove" title="${i18n().remove_capitalized}">${i18n().remove_capitalized}</a>
	</#if>
	</#if>
    
    </li>
</#macro>