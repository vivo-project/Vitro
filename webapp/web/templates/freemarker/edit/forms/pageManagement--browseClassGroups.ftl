<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#--Browse Class Groups Section-->
<#-----------Variable assignment-------------->
<#--Requires Menu action be defined in parent template-->

<#assign classGroup = pageData.classGroup />
<#assign classGroups = pageData.classGroups />
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
 <section id="browseClassGroup" class="contentSectionContainer">
                       
                <section id="selectContentType" name="selectContentType" ${selectClassGroupStyle} role="region">     
                    
                    <label for="selectClassGroup">Class Group<span class="requiredHint"> *</span></label>
                    <select name="selectClassGroup" id="selectClassGroup" role="combobox">
                        <option value="-1" role="option">Select one</option>
                        <#list classGroups as aClassGroup>
                            <option value="${aClassGroup.URI}"  role="option">${aClassGroup.publicName}</option>
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
                            <input type="checkbox" name="allSelected" id="allSelected" value="all" checked="checked" />
                            <label class="inline" for="All"> All</label>
                        </li>
                        <#list classGroup as classInClassGroup>
                        <li class="ui-state-default" role="menuitem">
                            <input type="checkbox" id="classInClassGroup" name="classInClassGroup" value="${classInClassGroup.URI}" checked="checked" />
                            <label class="inline" for="${classInClassGroup.name}"> ${classInClassGroup.name}</label>
                            <span class="ui-icon-sortable"></span> 
                        </li>
                        </#list>
                    </ul><br />
                    <input  type="button" id="doneWithContent" class="doneWithContent" name="doneWithContent" value="Save this content" />
                    <#if menuAction == "Add">
                        <span id="cancelContent"> or <a class="cancel" href="javascript:"  id="cancelContentLink" >Cancel</a></span>
                    </#if>
                </section>
            </section>
 <#--Include JavaScript specific to the types of data getters related to this content-->           
 <#include "pageManagement--browseClassGroupsScripts.ftl">           