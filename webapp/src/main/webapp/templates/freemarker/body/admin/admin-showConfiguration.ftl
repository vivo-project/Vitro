<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template viewing the authorization mechanisms: current identifiers, factories, policies, etc. -->

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/showAuth.css" />')}

<h2>Configuration settings</h2>

<section id="show-auth" role="region">
    <h4>Build and runtime properties:</h4>
    <table summary="Build and Runtime Properties">
    	<#list configurationProperties?keys as key>
            <tr>
                <td>${key}</td>
                <td>${configurationProperties[key]}</td>
            </tr>
        </#list>
    </table>
    <h4>Java system properties:</h4>
    <table summary="Java System Properties">
    	<#list javaSystemProperties?keys as key>
            <tr>
                <td>${key}</td>
                <td>${javaSystemProperties[key]}</td>
            </tr>
        </#list>
    </table>
</section>
