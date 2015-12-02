<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template to display the source of our RDF data, both for the context and for the current request. -->

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/showSources.css" />')}

<section id="show-sources" role="region">
    <p>
        <em>The information displayed here has also been written to the log.</em>
    </p>
    
    <h2>Current RDF Data Structures</h2>
    
    <h3>RDFServices</h3>
    <table>
        <tr>
            <th>&nbsp;</th><th>Context</th><th>Request</th>
        </tr>
        <#list rdfServices?keys as which>
            <tr>
                <th>${which}</th>
                <td><pre>${rdfServices[which].context}</pre></td>
                <td><pre>${rdfServices[which].request}</pre></td>
            </tr>
        </#list>
    </table>

    <h3>Datasets</h3>
    <table>
        <tr>
            <th>&nbsp;</th><th>Context</th><th>Request</th>
        </tr>
        <#list datasets?keys as which>
            <tr>
                <th>${which}</th>
                <td><pre>${datasets[which].context}</pre></td>
                <td><pre>${datasets[which].request}</pre></td>
            </tr>
        </#list>
    </table> 

    <h3>ModelAccess</h3>
    <table>
        <tr>
            <th>Context</th>
            <td><pre>${modelAccess.context}</pre></td>
        </tr>
        <tr>
            <th>Request</th>
            <td><pre>${modelAccess.request}</pre></td>
        </tr>
    </table>

    <h3>Models</h3>
    <table>
        <tr>
            <th>Name</th><th>(Context only)</th> 
        </tr>
        <tr>
            <th>&nbsp;</th><th>CONFIGURATION</th> 
        </tr>
        <#list models.CONFIGURATION?keys as name>
            <tr>
                <td><pre>${name}</pre></td>
                <td><pre>${models.CONFIGURATION[name].context}</pre></td>
            </tr>
        </#list>
        <tr>
            <th>&nbsp;</th><th>CONTENT</th> 
        </tr>
        <#list models.CONTENT?keys as name>
            <tr>
                <td><pre>${name}</pre></td>
                <td><pre>${models.CONTENT[name].context}</pre></td>
            </tr>
        </#list>
    </table> 

    <h3>OntModels</h3>
    <table>
        <tr>
            <th>Name</th><th>Context</th><th>Request</th>
        </tr>
        <#list ontModels?keys as name>
            <tr>
                <td><pre>${name}</pre></td>
                <td><pre>${ontModels[name].context}</pre></td>
                <td><pre>${ontModels[name].request}</pre></td>
            </tr>
        </#list>
    </table> 

</section>