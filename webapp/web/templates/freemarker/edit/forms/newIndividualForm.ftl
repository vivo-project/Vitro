<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for adding a new individual from the Site Admin page: VIVO version -->

<#--Retrieve certain edit configuration information-->
<#assign typeName = editConfiguration.pageData.typeName />

<#--If edit submission exists, then retrieve validation errors if they exist-->
<#if editSubmission?has_content && editSubmission.submissionExists = true && editSubmission.validationErrors?has_content>
	<#assign submissionErrors = editSubmission.validationErrors/>
</#if>


<h2>${i18n().create_new} ${typeName}</h2>

<#if submissionErrors?has_content >
    <section id="error-alert" role="alert">
        <img src="${urls.images}/iconAlert.png" width="24" height="24" alt="${i18n().error_alert_icon}" />
        <p>
        <#list submissionErrors?keys as errorFieldName>
        	<#if  errorFieldName == "label">
        	    ${i18n().enter_value_name_field}
    	    </#if>
    	    <br />
    	</#list>
        </p>
    </section>
</#if>



<#assign requiredHint = "<span class='requiredHint'> *</span>" />

<section id="newIndividual" role="region">        
    
    <form id="newIndividual" class="customForm noIE67" action="${submitUrl}"  role="add new individual">
 
      <p>
          <label for="name">${i18n().name} ${requiredHint}</label>
          <input size="30"  type="text" id="label" name="label" value="" />
      </p>

      <p class="submit">
          <input type="hidden" name = "editKey" value="${editKey}"/>
          <input type="submit" id="submit" value="${i18n().create_capitalized} ${typeName}"/>
          <span class="or"> or <a class="cancel" href="${urls.base}/siteAdmin" title="${i18n().cancel_title}">${i18n().cancel_link}</a>
      </p>

      <p id="requiredLegend" class="requiredHint">* ${i18n().required_fields}</p>

    </form>

</section>

