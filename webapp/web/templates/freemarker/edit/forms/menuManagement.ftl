<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#--Template for adding and editing menu items -->

<#-- some additional processing here which shows or hides the class group selection and classes based on initial action-->
<#assign existingClassGroupStyle = " " />
<#assign selectClassGroupStyle = 'class="hidden"' />
<#-- Reveal the class group and hide the class selects if adding a new menu item or editing an existing menu item with an empty class group (no classes)-->
<#if menuAction == "Add" || !classGroup?has_content>
    <#assign existingClassGroupStyle = 'class="hidden"' />
    <#assign selectClassGroupStyle = " " />
</#if>
<section id="error-alert" role="alert" class="hidden">
    <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon" />
    <p></p>
</section>

<h3>${menuAction} menu item</h3>

<section id="${menuAction?lower_case}-menu-item" role="region">
    <form method="POST" action="${formUrls}" class="customForm" role="${menuAction} menu item">
        <input type="hidden" name="cmd" id="cmd" value="${menuAction}" role="input" />
        <input type="hidden" name="menuItem" id="menuItem" value="${menuItem}" role="input" />
        <input type="hidden" name="switchToDisplayModel" id="switchToDisplayModel" value="true" role="input" />
         
        <label for="menu-name">Name<span class="requiredHint"> *</span></label>
        <input type="text" name="menuName" value="${menuName}" role="input" />

        <label for="pretty-url">Pretty URL<span class="requiredHint"> *</span></label> 
        <input type="text" name="prettyUrl" value="${prettyUrl}" role="input" />
        <p class="note">Must begin with a leading forward slash: / (e.g., /people)</p>
        
        <p>Template<span class="requiredHint"> *</span></p>
        
        <input type="radio" class="default-template" name="selectedTemplate" value="default" <#if selectedTemplateType = "default">checked</#if> role="radio" />
        <label class="inline" for="default"> Default</label>
        <br />
        <input type="radio" name="selectedTemplate" class="custom-template" value="custom" <#if selectedTemplateType = "custom">checked</#if> role="input" />
        <label class="inline" for="custom"> Custom template</label>
        
        <section id="custom-template" <#if selectedTemplateType != 'custom'>class="hidden" </#if>role="region">
            <input type="text" name="customTemplate" value="${customTemplate!}" size="40" role="input" /><span class="requiredHint"> *</span>
        </section>
        
        <section id="existingContentType" name="existingContentType" ${existingClassGroupStyle} role="region">
            <p>Selected content type for the associated page</p>
            <p>
                <span id="selectedContentTypeValue" name="selectedContentTypeValue">${associatedPage}</span>
                <a href="#" id="changeContentType" name="changeContentType" title="change content type">Change content type</a>
            </p>
        </section>
        
        <#-- Select class group -->
        <section id="selectContentType" name="selectContentType" ${selectClassGroupStyle} role="region">     
           <label for="selectClassGroup">Select content type for the associated page<span class="requiredHint"> *</span></label>
           
           <select name="selectClassGroup" id="selectClassGroup" role="combobox">
               <option value="-1" role="option">Select one</option>
               <#list classGroups as aClassGroup>
                    <option value="${aClassGroup.URI}" <#if aClassGroup.URI = associatedPageURI>selected</#if> role="option">${aClassGroup.publicName}</option>
               </#list>
           </select>
        </section> 
        
        <section id="classesInSelectedGroup" name="classesInSelectedGroup" ${existingClassGroupStyle}>
            <#-- Select classes in a class group -->    
            <p id="selectClassesMessage" name="selectClassesMessage">Select content to display<span class="requiredHint"> *</span></p>
            
            <#include "menuManagement--classIntersections.ftl">
            
            <ul id="selectedClasses" name="selectedClasses" role="menu">
                <#--Adding a default class for "ALL" in case all classes selected-->
                <li class="ui-state-default" role="menuitem">
                    <input type="checkbox" name="allSelected" id="allSelected" value="all" <#if !isIndividualsForClassesPage?has_content>checked</#if> />
                    <label class="inline" for="All"> All</label>
                </li>
                <#list classGroup as classInClassGroup>
                <li class="ui-state-default" role="menuitem">
                    <input type="checkbox" id="classInClassGroup" name="classInClassGroup" value="${classInClassGroup.URI}" <#if includeAllClasses = true>checked</#if> 
                     <#if isIndividualsForClassesPage?has_content>
                            <#list includeClasses as includeClass>
                                <#if includeClass = classInClassGroup.URI>
                                    checked
                                </#if>
                            </#list>
                    </#if> />
                    <label class="inline" for="${classInClassGroup.name}"> ${classInClassGroup.name}</label>
                    <#-- PLACEHOLDER - not yet implemented) -->
                    <span class="ui-icon-sortable"></span> <#--sortable icon for dragging and dropping menu items-->
                </li>
                </#list>
            </ul>
        </section>
        
        <input type="submit" name="submit-${menuAction}" value="Save changes" class="submit" role="input" /> or <a class="cancel" href="${cancelUrl}" title="cancel">Cancel</a>

        <p class="requiredHint">* required fields</p>
    </form>
</section>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/menuManagement.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jquery.scrollTo-min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/menumanagement_edit.js"></script>')}