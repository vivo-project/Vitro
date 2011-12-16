<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration Ontology Editor -->

<#if ontologyEditor?has_content>
    <section class="pageBodyGroup" role="region">
        <h3>Ontology Editor</h3>
        
        <#if ontologyEditor.pellet?has_content>
            <div class="notice">
                <p>${ontologyEditor.pellet.error}</p>
                <#if ontologyEditor.pellet.explanation?has_content>
                    <p>Cause: ${ontologyEditor.pellet.explanation}</p>
                </#if>
            </div>
        </#if>
        
        <ul role="navigation">
            <li role="listitem">
                <a href="${ontologyEditor.urls.ontologies}" title="Ontology list">Ontology list</a></h4>
            </li>
        </ul>
    
        <h4>Class Management</h4>
        
        <ul role="navigation">
            <li role="listitem"><a href="${ontologyEditor.urls.classHierarchy}" title="Class hierarchy">Class hierarchy</a></li>
            <li role="listitem"><a href="${ontologyEditor.urls.classGroups}" title="Class groups">Class groups</a></li>
        </ul>
        
        <h4>Property Management</h4>
        
        <ul role="navigation">
            <li role="listitem"><a href="${ontologyEditor.urls.objectPropertyHierarchy}" title="Object property hierarchy">Object property hierarchy</a></li>
            <li role="listitem"><a href="${ontologyEditor.urls.dataPropertyHierarchy}" title="Data property hierarchy">Data property hierarchy</a></li>
            <li role="listitem"><a href="${ontologyEditor.urls.propertyGroups}" title="Property groups">Property groups</a></li>
        </ul>
        
    </section>
</#if>