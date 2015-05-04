<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration Ontology Editor -->

<#if ontologyEditor?has_content>
    <section class="pageBodyGroup" role="region">
        <h3>${i18n().ontology_editor}</h3>
        
        <#if ontologyEditor.tboxReasonerStatus?has_content>
            <div class="notice">
                <p>${ontologyEditor.tboxReasonerStatus.error}</p>
                <#if ontologyEditor.tboxReasonerStatus.explanation?has_content>
                    <p>${i18n().cause} ${ontologyEditor.tboxReasonerStatus.explanation}</p>
                </#if>
            </div>
        </#if>
        
        <ul role="navigation">
            <li role="listitem">
                <a href="${ontologyEditor.urls.ontologies}" title="${i18n().ontology_list}">${i18n().ontology_list}</a>
            </li>
        </ul>
    
        <h4>${i18n().class_management}</h4>
        
        <ul role="navigation">
            <li role="listitem"><a href="${ontologyEditor.urls.classHierarchy}" title="${i18n().class_hierarchy}">${i18n().class_hierarchy}</a></li>
            <li role="listitem"><a href="${ontologyEditor.urls.classGroups}" title="${i18n().class_groups}">${i18n().class_groups}</a></li>
        </ul>
        
        <h4>${i18n().property_management}</h4>
        
        <ul role="navigation">
            <li role="listitem"><a href="${ontologyEditor.urls.objectPropertyHierarchy}" title="${i18n().object_property_hierarchy}">${i18n().object_property_hierarchy}</a></li>
            <li role="listitem"><a href="${ontologyEditor.urls.dataPropertyHierarchy}" title="${i18n().data_property_hierarchy}">${i18n().data_property_hierarchy}</a></li>
            <li role="listitem"><a href="${ontologyEditor.urls.fauxPropertyList}" title="${i18n().data_property_hierarchy}">${i18n().faux_property_listing}</a></li>
            <li role="listitem"><a href="${ontologyEditor.urls.propertyGroups}" title="${i18n().property_groups}">${i18n().property_groups}</a></li>
        </ul>
        
    </section>
</#if>