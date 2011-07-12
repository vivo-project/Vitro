<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#--Since we are going to use one template for adding and editing a menu item,
    it will be necessary to provide a freemarker variable that lets us know if you are in edit or add mode. bThis is up 
    to you Huda the way you implement it. -->

<#-- some additional processing here which shows or hides the class group selection and classes based on initial action-->
<#assign existingClassGroupStyle = " " />
<#assign selectClassGroupStyle = 'class="hide"' />
<#if menuAction = "Add">
	<#assign existingClassGroupStyle = 'class="hide"' />
	<#assign selectClassGroupStyle = " " />
</#if>

<h3>${menuAction} menu item</h3>

<section>
    <form method="POST" action="${formUrls}">
    	<input type="hidden" name="cmd" id="cmd" value="${menuAction}"/>
        <legend>${menuAction} menu item</legend>
         
        <label for="menu-name">Name *</label>
        <input type="text" name="menuName" value="${menuName}" />

        <label for="pretty-url">Pretty URL *</label> 
        <input type="text" name="prettyUrl" value="${prettyUrl}" />
        <p>(Format: /<prettyURL> - ie. /people)</p>

		<#--Commented out for now -->
		
        
        <p>Template *</p>
        
        <input type="radio" name="selectedTemplate" value="default" <#if selectedTemplateType = "default">checked</#if> />
        <label class="inline" for="default"> Default</label>
        
        <br />
        
        <input type="radio" name="selectedTemplate" value="custom" <#if selectedTemplateType = "custom">checked</#if> />
        <label class="inline" for="custom"> Custom template</label>
        
        <#if selectedTemplateType = "custom">
            <input type="text" name="customTemplate" value="${customTemplate}"/>*
        </#if>
        
       <div id="existingContentType" name="existingContentType" ${existingClassGroupStyle}>
        <p>Selected content type for the associated page</p>
    	<p ><span id="selectedContentTypeValue" name="selectedContentTypeValue">${associatedPage}</span> <a id="changeContentType" name="changeContentType" href="#">Change content type</a></p>
       </div>
        
          
          <div id="selectContentType" name="selectContentType" ${selectClassGroupStyle}>     
			<label for="selectClassGroup">Select content type for the associated page *</label>
    			<select name="selectClassGroup" id="selectClassGroup">
    			<option value="-1"> </option>
    			
    			<#list classGroups as aClassGroup>
    				<option value="${aClassGroup.URI}" 
    				<#if aClassGroup.URI = associatedPageURI>
    					selected
    				</#if>
    				>${aClassGroup.publicName}</option>
        		</#list>
        		
   			 	</select>
   			</div> 
   					
        <p id="selectClassesMessage" name="selectClassesMessage">Select content to display</p>
        <section id="classesInSelectedGroup" name="classesInSelectedGroup">
        	
            <ul id="selectedClasses" name="selectedClasses">
            <#--Adding a default class for "ALL" in case all classes selected-->
            <li class="ui-state-default">
                    <input type="checkbox" name="allSelected" id="allSelected" value="all" <#if isClassGroupPage = true || includeAllClasses = true>checked</#if> 
                    <label class="inline" for="All"> All</label>
             </li>
             <#list classGroup as classInClassGroup>
                <li class="ui-state-default">
                    <input type="checkbox" name="classInClassGroup" value="${classInClassGroup.URI}" 
                    	<#if includeAllClasses = true>checked</#if> />
                    <label class="inline" for="${classInClassGroup.name}"> ${classInClassGroup.name}</label>
                    <span class="ui-icon-sortable"></span> <#--sortable icon for dragging and dropping menu items-->
                </li>
            </#list>
            </ul>
        </section>
    	
        <input type="submit" name="submit-${menuAction}" value="Save changes" class="submit" /> or <a class="cancel" href="${formUrls}">Cancel</a>

        <p>* required fields</p>
    </form>
</section>


<#-- Add necessary css files associated with this page
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/menupage.css" />')}-->
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/testmenupage.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}

<#-- Add necessary javascript files associated with this page -->
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/menumanagement_edit.js"></script>')}   
