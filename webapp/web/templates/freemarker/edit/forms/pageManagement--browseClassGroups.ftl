<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#--Browse Class Groups Section-->
<#-----------Variable assignment-------------->
<#--Requires Menu action be defined in parent template-->
<#assign classGroup = pageData.classGroup />
<#assign classGroups = pageData.classGroups />
<#assign isClassGroupPage = false/>
<#assign includeAllClasses = false/>
<#-- some additional processing here which shows or hides the class group selection and classes based on initial action-->
<#assign existingClassGroupStyle = " " />
<#assign selectClassGroupStyle = 'class="hidden"' />
<#-- Reveal the class group and hide the class selects if adding a new menu item or editing an existing menu item with an empty class group (no classes)-->
<#-- Menu action needs to be sent from  main template-->
<#if menuAction == "Add" || !classGroup?has_content>
    <#assign existingClassGroupStyle = 'class="hidden"' />
    <#assign selectClassGroupStyle = " " />
</#if>


<#--HTML Portion-->
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
                            <span class="ui-icon-sortable"></span> 
                        </li>
                        </#list>
                    </ul>
                </section>
            </section>
 <#--Include JavaScript specific to the types of data getters related to this content-->           
 <#include "pageManagement--browseClassGroupsScripts.ftl">           