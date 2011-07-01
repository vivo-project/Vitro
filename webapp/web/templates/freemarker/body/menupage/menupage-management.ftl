<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for menu items management. Based on menu management wireframes -->

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
    it will be necessary to provide a freemarker variable that does the job -->
    
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
            
        <p>Select content type for the associated page</p>
    
        <p><span>${associatedPage}</span> <a href="#">Change content type</a></p>

        <p>Select content to display</p>
        
        <#list classGroups as classGroup>
            <input type="checkbox" name="classGroup" value="${classGroup.uri}" <#if selectedClassGroup = "${classGroup.uri}">checked</#if> />
            <label class="inline" for="$classGroup.label}"> ${classGroup.label}</label>
            <span class="ui-icon-sortable"></span> <#--sortable icon for dragging and dropping menu items-->
            <br />
        </#list>
    
        <#-- NICK, this is a better markup for making sortable classGroups using http://jqueryui.com/demos/sortable/
        <p>Select content to display</p>

        <section>
            <ul>
            <#list classGroups as classGroup>
                <li class="ui-state-default">
                    <input type="checkbox" name="classGroup" value="${classGroup.uri}" <#if selectedClassGroup = "${classGroup.uri}">checked</#if> />
                    <label class="inline" for="$classGroup.label}"> ${classGroup.label}</label>
                    <span class="ui-icon-sortable"></span>
                </li>
            </#list>
            </ul>
        </section>-->

    
        <input type="text" name="display-${associatedPage}" value="${associatedPage}" id="display-${associatedPage}" />
        <label for="display-${associatedPage}">Only display ${associatedPage} within my institution</label> 
    
        <input type="submit" name="submit-${menuAction}" value="Save changes" class="submit" /> or <a class="cancel" href="${formUrls}">Cancel</a>

        <p>* required fields</p>
    </form>
</section>

<#-- Add necessary css files associated with this page
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/menupage.css" />')}-->
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}

<#-- Add necessary javascript files associated with this page
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/file-sample.js"></script>')}   
-->