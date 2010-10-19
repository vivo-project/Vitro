<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration Ontology Editor -->

<#if ontologyEditor??>
    <div class="pageBodyGroup">

        <h3>Ontology Editor</h3>
        
        <#if ontologyEditor.pellet??>
            <div class="notice">
                <p>${ontologyEditor.pellet.error}</p>
                <#if ontologyEditor.pellet.explanation??>
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
        
        <#assign formId = "verbosePropertyForm">
        <form id="${formId}" action="${ontologyEditor.verbosePropertyForm.action}#${formId}" method="get">
            <input type="hidden" name="verbose" value="${ontologyEditor.verbosePropertyForm.verboseFieldValue}" />
            <span>Verbose property display for this session is <b>${ontologyEditor.verbosePropertyForm.currentValue}</b>.</span>
            <input type="submit" value="Turn ${ontologyEditor.verbosePropertyForm.newValue}" />
        </form>  
         
    </div>                       
</#if>