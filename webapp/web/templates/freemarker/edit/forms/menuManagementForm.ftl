<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#--    Template for menu items management. 
        Based on menu management wireframes: edit page: screen 3a -->

<#-- Assignment of validation error variables -->

<#if errorNameIsEmpty??>
      <#assign errorMessage = "Text for error message" />
</#if>

<#if errorPrettyUrlIsEmpty??>
      <#assign errorMessage = "Text for error message" />
</#if>

<#if errorTemplateTypeLIsEmpty??>
      <#assign errorMessage = "Text for error message" />
</#if>

<#-- Other error messages -->

<#if errorMessage?has_content>
      <section id="error-alert" role="alert">
          <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon"/>
          <p>${errorMessage}</p>
      </section>
</#if>

<#--Since we are going to use one template for adding and editing a menu item,
    it will be necessary to provide a freemarker variable that lets us know if you are in edit or add mode. bThis is up 
    to you Huda the way you implement it. -->
    
<h3>${menuAction} menu item</h3>

<section>
    <form method="POST" action="${formUrls}">
        <legend>${menuAction} menu item</legend>
         
        <label for="menu-name">Name *</label>
        <input type="text" name="menuName" value="${menuName}" />

        <label for="pretty-url">Pretty URL *</label> 
        <input type="text" name="prettyUrl" value="${prettyUrl}" />
        <p>(Format: /<prettyURL> - ie. /people)</p>

        
        <p>Template *</p>
        
        <input type="radio" name="default" value="default" <#if selectedTemplateType = "default">checked</#if> />
        <label class="inline" for="default"> Default</label>
        
        <br />
        
        <input type="radio" name="custom" value="custom" <#if selectedTemplateType = "custom">checked</#if> />
        <label class="inline" for="custom"> Custom template</label>
        
        <#if selectedTemplateType = "custom">
            <input type="text" name="customTemplate" value="${customTemplate}"/>*
        </#if>
            
        <p>Select content type for the associated page</p>
    
        <p><span>${associatedPage}</span> <a href="#">Change content type</a></p>

        <p>Select content to display</p>

        <section>
            <ul>
            <#list classesInClassGroup as classInClassGroup>
                <li class="ui-state-default">
                    <input type="checkbox" name="classInClassGroup" value="${classInClassGroup.uri}" <#if selectedClassInClassGroup = "${classInClassGroup.uri}">checked</#if> />
                    <label class="inline" for="${classInClassGroup.label}"> ${classInClassGroup.label}</label>
                    <span class="ui-icon-sortable"></span> <#--sortable icon for dragging and dropping menu items-->
                </li>
            </#list>
            </ul>
        </section>
    
        <input type="text" name="display-${associatedPage}" value="${associatedPage}" id="display-${associatedPage}" />
        <label for="display-${associatedPage}">Only display ${associatedPage} within my institution</label> 
    
        <input type="submit" name="submit-${menuAction}" value="Save changes" class="submit" /> or <a class="cancel" href="${formUrls}">Cancel</a>

        <p>* required fields</p>
    </form>
</section>

<#-- Based on menu management wireframes: edit page: screen 3b 
     Snippet for selecting class groups or content types-->
    
    <label for="selectClassGroup">Select content type for the associated page *</label>
    <select name="selectClassGroup">
        <#list classGroups as classGroup>
        <option value="${classGroup.uri}">${classGroup.label}</option>
        </#list>
    </select>

<#-- Based on menu management wireframes: add page: screen 3c 
     Snippet if there is no institutional internal class selected-->
     
     <#if internalClass?has_content>
        <#assign disableClass = 'class="disable"' />
        <#assign enableInternalClass = '<p>To enable this option, you must first select an <a href="' + internalClass.uri + '">institutional internal class for your instance</a></p>' />
     <#/if>
     
     <input type="text" ${disableClass} name="display-${associatedPage}" value="${associatedPage}" id="display-${associatedPage}" />
     <label ${disableClass} for="display-${associatedPage}">Only display ${associatedPage} within my institution</label>
     ${enableInternalClass}

<#-- Add necessary css files associated with this page
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/menupage.css" />')}-->
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}

<#-- Add necessary javascript files associated with this page
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/file-sample.js"></script>')}   
-->