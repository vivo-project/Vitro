<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for property listing on individual profile page -->

        <#list group.properties as property>
            <article class="property" role="article">
                <#-- Property display name -->
                <#if property.localName == "authorInAuthorship" && editable  >
                    <h3 id="${property.localName}">${property.name} <@p.addLink property editable /> <@p.verboseDisplay property /> 
                        <a id="managePubLink" class="manageLinks" href="${urls.base}/managePublications?subjectUri=${subjectUri[1]!}" title="${i18n().manage_publications}" <#if verbose>style="padding-top:10px"</#if> >
                            ${i18n().manage_publications}
                        </a>
                    </h3>
                <#elseif property.localName == "hasResearcherRole" && editable  >
                    <h3 id="${property.localName}">${property.name} <@p.addLink property editable /> <@p.verboseDisplay property /> 
                        <a id="manageGrantLink" class="manageLinks" href="${urls.base}/manageGrants?subjectUri=${subjectUri[1]!}" title="${i18n().manage_grants_and_projects}" <#if verbose>style="padding-top:10px"</#if> >
                            ${i18n().manage_grants_and_projects}
                        </a>
                    </h3>
                <#elseif property.localName == "organizationForPosition" && editable  >
                    <h3 id="${property.localName}">${property.name} <@p.addLink property editable /> <@p.verboseDisplay property /> 
                        <a id="managePeopleLink" class="manageLinks" href="${urls.base}/managePeople?subjectUri=${subjectUri[1]!}" title="${i18n().manage_affiliated_people}" <#if verbose>style="padding-top:10px"</#if> >
                            ${i18n().manage_affiliated_people}
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
