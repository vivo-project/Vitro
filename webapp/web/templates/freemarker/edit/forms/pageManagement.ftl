<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#--Set up variables-->
<#assign pageData = editConfiguration.pageData />
<#assign menuAction = pageData.menuAction />
<#assign classGroup = pageData.classGroup />
<#assign classGroups = pageData.classGroups />
<#assign pageName = "" />
<#assign selectedTemplateType = "default" />
<#assign prettyUrl = ""/>
<#assign associatedPage = ""/>
<#assign associatedPageURI = ""/>
<#assign menuItem = ""/>    	
<#assign isClassGroupPage = false/>
<#assign includeAllClasses = false/>


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

<#--class group section has associated page uri, but that depends on editing a current page or if one is selected later-->
<section id="pageDetails">
    <#--form method="POST" action="${formUrls}" role="${menuAction} menu item"-->
	<form method="POST" action="${submitUrl}" role="add page">
	        <input type="hidden" name="switchToDisplayModel" id="switchToDisplayModel" value="1" role="input" />
	        <input type="hidden" id="editKey" name="editKey" value="${editKey}" />
			<input type="hidden" id="menuItem" name="menuItem" value="${menuItem}"/>
    <h2>Add Page</h2>
    <section id="floatRight" style="margin-top:0px;float:right;background-color:#fff;width:580px;margin-right:-4px">
        <div id="rightSide">
            <section id="addPageOne" role="region">
                <label for="last-name">Content Type<span class="requiredHint"> *</span></label> 
                <select id="typeSelect"  name="typeSelect">
                    <option value="" selected="selected">Select one</option>
                    <option value="browseClassGroup">Browse Class Group</option>           
                    <option value="fixedHtml">Fixed HTML</option>           
                    <option value="sparqlQuery">SPARQL Query Results</option>           
                 </select>
                 <input  type="button" id="defineType" name="defineType" value="Define" class="delete" role="input" style="display:none"/>
            </section>
            <section id="contentDivs"></section>
            <section id="headerBar" style="background-color:#f5f5f5;border-color:#ccc;border-width:1px;border-style:solid;border-bottom-width:0px;padding-left:6px">
            </section>
            
            <section id="browseClassGroup" style="background-color:#f9f9f9;padding-left:6px;padding-top:2px;border-width:1px;border-style:solid;border-color:#ccc;">
                       
                <section id="selectContentType" name="selectContentType" ${selectClassGroupStyle} role="region">     
                    
                    <label for="selectClassGroup">Class Group<span class="requiredHint"> *</span></label>
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

                    <#include "pageManagement--classIntersections.ftl">

                    <ul id="selectedClasses" name="selectedClasses" role="menu">
                        <#--Adding a default class for "ALL" in case all classes selected-->
                        <li class="ui-state-default" role="menuitem">
                            <input type="checkbox" name="allSelected" id="allSelected" value="all" <#if !isIndividualsForClassesPage?has_content>checked</#if> />
                            <label class="inline" for="All"> All</label>
                        </li>
                        <#list classGroup as classInClassGroup>
                        <li class="ui-state-default" role="menuitem">
                            <input type="checkbox" id="classInClassGroup" name="classInClassGroup" value="${classInClassGroup.URI}" 
                            <#if includeAllClasses = true>checked</#if> 
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
            </section>
            <!--Broken fixed html and sparql content into separate portions-->
            <!--This content will be copied/shown for these particular content types, so any fields for n3 editing need to be included
            here that correspond to a specific content type.  These are related to specific "data getters" on the server side.  -->
            <section id="fixedHtml" style="background-color:#f9f9f9;padding-left:6px;padding-top:2px;border-width:1px;border-style:solid;border-color:#ccc;">
                <label id="fixedHTMLVariableLabel" for="fixedHTMLVariable">Variable Name<span class="requiredHint"> *</span></label>
                <input type="text" name="saveToVar" size="20" value="" id="fixedHTMLSaveToVar" role="input" />
                <label id="fixedHTMLValueLabel" for="fixedHTMLValue">HTML<span id="fixedHTMLValueSpan"></span><span class="requiredHint"> *</span></label>
                <textarea id="fixedHTMLValue" name="htmlValue" cols="70" rows="15" style="margin-bottom:7px"></textarea>
            </section>
            <section id="sparqlQuery" style="background-color:#f9f9f9;padding-left:6px;padding-top:2px;border-width:1px;border-style:solid;border-color:#ccc;">
                <label id="variableLabel" for="variable">Variable Name<span class="requiredHint"> *</span></label>
                <input type="text" name="saveToVar" size="20" value="" id="saveToVar" role="input" />
                <label id="queryModelLabel" for="queryModel">Query Model</label>
                <input type="text" name="queryModel" size="20" value="" id="queryModel" role="input" />
                <label id="queryLabel" for="queryLabel"><span id="querySpan">Query</span><span class="requiredHint"> *</span></label>
                <textarea id="query" name="query" cols="70" rows="15" style="margin-bottom:7px"></textarea>
            </section>
          
            <input  type="button" id="moreContent" name="moreContent" value="Add More Content" class="delete" style="margin-top:8px" />          
        </div>
    </section>
    <!--Information for page or menu item level-->
    <div id="leftSide">
        <section id="addPageOne" role="region" style="background-color:#fff;">
            <label for="page-name">Title<span class="requiredHint"> *</span></label>
            <input type="text" name="pageName" value="${pageName!''}" role="input" />
            <label for="pretty-url">Pretty URL<span class="requiredHint"> *</span></label> 
            <input type="text" name="prettyUrl" value="${prettyUrl!''}" role="input" />
            <p class="note">Must begin with a leading forward slash: / (e.g., /people)</p>
            <p style="margin-top:8px;margin-bottom:2px">Template<span class="requiredHint"> *</span></p>
            <input type="radio" class="default-template" name="selectedTemplate" value="default" <#if selectedTemplateType = "default">checked</#if> role="radio" />
            <label class="inline" for="default"> Default</label>
            <br />
            <input type="radio" name="selectedTemplate" class="custom-template" value="custom" <#if selectedTemplateType = "custom">checked</#if> role="input" />
            <label class="inline" for="custom"> Custom template</label>
            <section id="custom-template" <#if selectedTemplateType != 'custom'>class="hidden" </#if>role="region">
                <input type="text" name="customTemplate" value="${customTemplate!}" size="40" role="input" /><span class="requiredHint"> *</span>
            </section>
            <p style="margin-top:10px;margin-bottom:0px"><input id="menuCheckbox" type="checkbox" name="menuCheckbox"> This is a menu page</p>
            <section id="menu" role="region" style="margin-top:10px">
                <label for="default">Menu Item Name</label>
                <input type="text" id="menuLinkText" name="menuLinkText" value="" size="28" role="input" />
                <input type="text" id="menuPosition" name="menuPosition" value="6" />
                <p class="note">If left blank, the page title will be used.</p>
            </section>
            <br />
        </section>
    </div>
    <section >
        <span id="saveButton" ><input  id="pageSave" type="submit" name="submit-Add" value="Save changes" class="submit" role="input" /> or </span> 
        <a class="cancel" href="#"  id="cancelPage" style="color:#f70">Cancel</a>
        <br />
        <p class="requiredHint">* required fields</p>
    </section>
    <!--Hidden input with JSON objects added will be included here.  This is the field with the page content information
    mirroring what is required by the Data getter server side objects. -->
    <div id="pageContentSubmissionInputs" style="display:none"></div>
    </form>
</section>

<!-

<!--Hardcoding for now but should be retrieved from generator-->

<script type="text/javascript">
    var customFormData = {
      dataGetterLabelToURI:{
      		//maps labels to URIs
      		"browseClassGroup": "java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.ClassGroupPageData",
      		"individualsForClasses": "java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.IndividualsForClassesDataGetter",
      		"sparqlQuery":"java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.SparqlQueryDataGetter",
      		"fixedHtml":"java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.FixedHTMLDataGetter"
      },
      dataGetterMap:{"java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.ClassGroupPageData": "Class Group Page",
					"java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.BrowseDataGetter": "Browse Page",  
					"java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.IndividualsForClassesDataGetter": "Class Group Page - Selected Classes", 
					"java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.SparqlQueryDataGetter": "Sparql Query Results",
					"java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.FixedHTMLDataGetter": "Fixed HTML Page"}
    };
</script>
 


${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/menuManagement.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/pageManagement.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery.fix.clone.js"></script>')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/json2.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/browserUtils.js"></script>')}
<#--Include additional templates here to allow for additional data getters etc. for other products-->
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/processFixedHTMLDataGetterContent.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/processClassGroupDataGetterContent.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/processSparqlDataGetterContent.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/processIndividualsForClassesDataGetterContent.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/processDataGetterUtils.js"></script>')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/menupage/pageManagementUtils.js"></script>')}

