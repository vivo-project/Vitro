<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#--Retrieve certain edit configuration information-->
<#assign editMode = editConfiguration.pageData.editMode />
<#assign literalValues = editConfiguration.existingLiteralValues />
<#assign uriValues = editConfiguration.existingUriValues />
<#assign htmlForElements = editConfiguration.pageData.htmlForElements />

Edit Mode is ${editMode}
<#--Freemarker variables with default values that can be overridden by specific forms-->


<#--buttonText, typeSelectorLabel, numDateFields, showRoleLabelField, roleExamples-->
<#if !buttonText?has_content>
	<#assign buttonText = roleDescriptor />
</#if>
<#if !typeSelectorLabel?has_content>
	<#assign typeSelectorLabel = roleDescriptor />
</#if>
<#if !numDateFields?has_content>
	<#assign numDateFields = 2 />
</#if>
<#if !showRoleLabelField?has_content>
	<#assign showRoleLabelField = true />
</#if>
<#if !roleExamples?has_content>
	<#assign roleExamples = "" />
</#if>

<#--Setting values for titleVerb, submitButonText, and disabled Value-->
<#if editConfiguration.objectUri?has_content>
	<#assign titleVerb = "Edit"/>
	<#assign submitButtonText>Edit ${buttonText}</#assign>
	<#if editMode = "repair">
		<#assign disabledVal = ""/>
	<#else>
		<#assign disabledVal = "disabled"/>
	</#if>
<#else>
	<#assign titleVerb = "Create"/>
	<#assign submitButtonText>${buttonText}</#assign>
	<#assign disabledVal = ""/>
	<#--The original jsp sets editMode to add, why?-->
</#if>

<#--Get existing value for specific data literals and uris-->


<#--Get selected activity type value if it exists, this is alternative to below-->
<#assign activityTypeValue = ""/>
<#if uriValues?keys?seq_contains("activityType") && uriValues.activityType?size > 0>
	<#assign activityTypeValue = uriValues.activityType[0] />
</#if>

 <#--Get activity label value-->
<#assign activityLabelValue = "" />
<#if literalValues?keys?seq_contains("activityLabel") && literalValues.activityLabel?size > 0>
	<#assign activityLabelValue = literalValues.activityLabel[0] />
</#if>

<#--Get role label-->
<#assign roleLabel = "" />
<#if literalValues?keys?seq_contains("roleLabel") && literalValues.roleLabel?size > 0 >
	<#assign roleLabel = literalValues.roleLabel[0] />
</#if>


ActivityLabel:${activityLabelValue}
Activity type: ${activityTypeValue}

<h2>${titleVerb}&nbsp;${roleDescriptor} entry for ${editConfiguration.subjectName}</h2>

<#--Display error messages if any-->
<#if errorNameFieldIsEmpty??>
    <#assign errorMessage = "Enter a name for the ." />
</#if>

<#if errorRoleFieldIsEmpty??>
    <#assign errorMessage = "Specify a role for this ." />
</#if>

<#if errorMessage?has_content>
    <section id="error-alert" role="alert">
        <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon" />
        <p>${errorMessage}</p>
    </section>
</#if>


<section id="add${roleDescriptor?capitalize}RoleToPersonTwoStage" role="region">        
    
    <form id="add${roleDescriptor?capitalize}RoleToPersonTwoStage" class="customForm noIE67" action="${submitUrl}"  role="add/edit grant role">

       <p class="inline"><label for="typeSelector">${roleDescriptor?capitalize} Type <span class='requiredHint'> *</span></label>
           <select id="typeSelector" name="roleActivityType" 
           <#if disabledVal?has_content>
           	disabled = ${disabledVal}
           </#if>
            >
           		<#assign roleActivityTypeSelect = editConfiguration.pageData.roleActivityType />
           		<#assign roleActivityTypeKeys = roleActivityTypeSelect?keys />
                <#list roleActivityTypeKeys as key>
                    <option value="${key}"
                    <#if activityTypeValue?has_content 
                    && activityTypeValue = key>selected</#if>
                    >
                    ${roleActivityTypeSelect[key]}
                    </option>
                </#list>
           </select>
       </p>
       
       
       
   <div class="fullViewOnly">        
            <p>
                <label for="relatedIndLabel">${roleDescriptor?capitalize} Name <span class='requiredHint'> *</span></label>
                <input class="acSelector" size="50"  type="text" id="relatedIndLabel" name="activityLabel"  value="${activityLabelValue}" 
                <#if disabledVal?has_content>
                	disabled=${disabledVal}
                </#if>
                />
            </p>
            
            <#if editMode = "edit">
            	<input type="hidden" id="roleActivityType" name="roleActivityType" value/>
            	<input type="hidden" id="activityLabel" name="activityLabel"/>
            </#if>

            <div class="acSelection">
                <p class="inline">
                    <label>Selected ${roleDescriptor?capitalize}:</label>
                    <span class="acSelectionInfo"></span>
                    <a href="/vivo/individual?uri=" class="verifyMatch">(Verify this match)</a>
                    </p>
                    <input class="acUriReceiver" type="hidden" id="roleActivityUri" name="roleActivity" value="" />
                    <!-- Field value populated by JavaScript -->
            </div>
            
            <#if showRoleLabelField = true>
            <p><label for="roleLabel">Role in ### <span class='requiredHint'> *</span> ${roleExamples}</label>
                <input  size="50"  type="text" id="roleLabel" name="roleLabel" value="${roleLabel}" />
            </p>
        	</#if>
        	
            <#if numDateFields == 1 >
               <#--Generated html is a map with key name mapping to html string-->
               <#if htmlForElements?keys?seq_contains("startField")>
                	<label for="startField">Start Year <span class='hint'>(YYYY)</span></label>
               		${htmlForElements["startField"]}
               </#if>
            <#else>
                <h4>Years of Participation in ${roleDescriptor?capitalize}</h4>
                <#if htmlForElements?keys?seq_contains("startField")>
                	 <label for="startField">Start Year <span class='hint'>(YYYY)</span></label>
               		${htmlForElements["startField"]}
               </#if>
               <#if htmlForElements?keys?seq_contains("endField")>
               		<label for="endField">End Year <span class='hint'>(YYYY)</span></label>
               		${htmlForElements["endField"]}
               </#if>
            </#if>
        </div>
        <input type="hidden" id="editKey" name="editKey" value="${editKey} />
        <p class="submit">
            <input type="submit" id="submit" value="submitButtonText"/><span class="or"> or <a class="cancel" href="${cancelUrl}">Cancel</a>
        </p>

        <p id="requiredLegend" class="requiredHint">* required fields</p>
    </form>

<#--Specifying form-specific script and adding stylesheets and scripts-->    
    
 <script type="text/javascript">
	var customFormData  = {
	    acUrl: '${urls.base}/autocomplete?tokenize=true',
	    editMode: '${editMode}',
	    submitButtonTextType: 'compound',
	    defaultTypeName: 'activity' // used in repair mode, to generate button text and org name field label
	};
	</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customFormWithAutocomplete.cs" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/edit/forms/js/customFormWithAutocomplete.js"></script>')}


 </section>   