<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for property listing on individual profile page -->

<#import "lib-properties.ftl" as p>
<#assign subjectUri = individual.controlPanelUrl()?split("=") >

<#if ( propertyGroups.all?size > 1 ) >
    <span id="toggleContainer">
        <a id="propertyGroupsToggle" href="javascript:" title="expand all groups">expand all</a>
    </span>
<#else>
    <p style="clear:both"><br /></p> 
</#if>
<#list propertyGroups.all as group>
    <#assign groupName = group.getName(nameForOtherGroup)>
    <#assign verbose = (verbosePropertySwitch.currentValue)!false>
    <#if groupName?has_content>
		<#--the function replaces spaces in the name with underscores, also called for the property group menu-->
    	<#assign groupNameHtmlId = p.createPropertyGroupHtmlId(groupName) >
    <#else>
        <#assign groupName = "Properties">
    	<#assign groupNameHtmlId = "properties" >
    </#if>
    
    <section id="${groupNameHtmlId}" class="property-group" role="region">
        <nav class="scroll-up" role="navigation">
            <img src="${urls.images}/individual/expand-prop-group.png" groupName="${groupNameHtmlId}" alt="expand property group" title="expand this property group"/>
        </nav>
        
        <#-- Display the group heading --> 
            <h2 id="${groupNameHtmlId}">${groupName?capitalize}</h2>
        
        <#-- List the properties in the group -->
            <div id="${groupNameHtmlId}Group" >
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
        </div>
    </section> <!-- end property-group -->
</#list>
<script>
var propGroupCount = ${propertyGroups.all?size};
if ( propGroupCount == 1 ) {
    $('section.property-group').find('div').show();
    $('section.property-group').find("h2").addClass("expandedPropGroupH2");
    $('section.property-group').children("nav").children("img").hide();
//    var innerSrc = $('section.property-group').children("nav").children("img").attr("src");
//    $('section.property-group').children("nav").children("img").attr("src",innerSrc.replace("expand-prop-group","collapse-prop-group"));
}
</script>