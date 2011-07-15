<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration Ontology Editor -->

<#if ontologyEditor?has_content>
    <div class="pageBodyGroup">

        <h3>Ontology Editor</h3>
        
        <#if ontologyEditor.pellet?has_content>
            <div class="notice">
                <p>${ontologyEditor.pellet.error}</p>
                <#if ontologyEditor.pellet.explanation?has_content>
                    <p>Cause: ${ontologyEditor.pellet.explanation}</p>
                </#if>
            </div>
        </#if>
        
        <ul>
            <li><a href="${ontologyEditor.urls.ontologies}">Ontology list</a></li>
        </ul>
    
        <h4>Class Management</h4>
        <ul>
            <li><a href="${ontologyEditor.urls.classHierarchy}">Class hierarchy</a></li> 
            <li><a href="${ontologyEditor.urls.classGroups}">Class groups</a></li>
        </ul>
    
        <h4>Property Management</h4>
        <ul>
            <li><a href="${ontologyEditor.urls.objectPropertyHierarchy}">Object property hierarchy</a></li>
            <li><a href="${ontologyEditor.urls.dataPropertyHierarchy}">Data property hierarchy</a></li>      
            <li><a href="${ontologyEditor.urls.propertyGroups}">Property groups</a></li>
        </ul>

    </div>                       
</#if>