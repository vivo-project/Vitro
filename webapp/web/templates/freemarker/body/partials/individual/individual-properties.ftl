<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for property listing on individual profile page -->

<#import "lib-properties.ftl" as p>
<#assign subjectUri = individual.controlPanelUrl()?split("=") >
<#list propertyGroups.all as group>
    <#assign groupName = group.getName(nameForOtherGroup)>
    <#assign verbose = (verbosePropertySwitch.currentValue)!false>
    
    <section class="property-group" role="region">
        <nav class="scroll-up" role="navigation">
            <a href="#branding" title="scroll up">
                <img src="${urls.images}/individual/scroll-up.gif" alt="scroll to property group menus" />
            </a>
        </nav>
        
        <#-- Display the group heading --> 
        <#if groupName?has_content>
    		<#--the function replaces spaces in the name with underscores, also called for the property group menu-->
        	<#assign groupNameHtmlId = p.createPropertyGroupHtmlId(groupName) >
            <h2 id="${groupNameHtmlId}">${groupName?capitalize}</h2>
        <#else>
            <h2 id="properties">Properties</h2>
        </#if>
        
        <#-- List the properties in the group -->
        <#list group.properties as property>
            <article class="property" role="article">
                <#-- Property display name -->
                <#if property.localName == "authorInAuthorship" && editable  >
                    <h3 id="${property.localName}">${property.name} <@p.addLink property editable /> <@p.verboseDisplay property /> 
                        <a id="managePubLink" class="manageLinks" href="${urls.base}/managePublications?subjectUri=${subjectUri[1]!}" title="manage publications" <#if verbose>style="padding-top:10px"</#if> >
                            manage publications
                        </a>
                    </h3>
                <#elseif property.localName == "hasResearcherRole" && editable  >
                <h3 id="${property.localName}">${property.name} <@p.addLink property editable /> <@p.verboseDisplay property /> 
                    <a id="manageGrantLink" class="manageLinks" href="${urls.base}/manageGrants?subjectUri=${subjectUri[1]!}" title="manage grants & projects" <#if verbose>style="padding-top:10px"</#if> >
                        manage grants & projects
                    </a>
                </h3>
                <#elseif property.localName == "organizationForPosition" && editable  >
                <h3 id="${property.localName}">${property.name} <@p.addLink property editable /> <@p.verboseDisplay property /> 
                    <a id="managePeopleLink" class="manageLinks" href="${urls.base}/managePeople?subjectUri=${subjectUri[1]!}" title="manage people" <#if verbose>style="padding-top:10px"</#if> >
                        manage affiliated people
                    </a>
                </h3>
                <#else>
                    <h3 id="${property.localName}">${property.name} <@p.addLink property editable /> <@p.verboseDisplay property /> </h3>
                </#if>
                <#-- List the statements for each property -->
                <ul class="property-list" role="list" id="${property.localName}List">
                    <#-- data property -->
                    <#if property.type == "data">
                        <@p.dataPropertyList property editable />
                    <#-- object property -->
                    <#else>
                        <@p.objectProperty property editable />
                    </#if>
                </ul>
            </article> <!-- end property -->
        </#list>
    </section> <!-- end property-group -->
</#list>
