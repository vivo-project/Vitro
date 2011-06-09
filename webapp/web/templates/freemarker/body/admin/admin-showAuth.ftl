<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template viewing the authorization mechanisms: current identifiers, factories, policies, etc. -->

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/showAuth.css" />')}

<h2>Authorization Info</h2>

<section id="show-auth" role="region">
    <#if currentUser?has_content>
        <table summary="Information about the current user" style="border: 1">
            <caption>Current user</caption>
            <tr><th>URI:</th><td>${currentUser.uri}</td></tr>
            <tr><th>First name:</th><td>${currentUser.firstName}</td></tr>
            <tr><th>Last name:</th><td>${currentUser.lastName}</td></tr>
            <tr><th>Email:</th><td>${currentUser.emailAddress}</td></tr>
            <tr><th>External Auth ID:</th><td>${currentUser.externalAuthId}</td></tr>
            <tr><th>Login count:</th><td>${currentUser.loginCount}</td></tr>
            <#list currentUser.permissionSetUris as role>
                <tr><th>Role:</th><td>${role}</td></tr>
            </#list>
        </table>
    <#else>
        <h3>Not logged in</h3>
    </#if>
   
    <table summary="VIVO revision's levels table">
        <caption>Identifiers:</caption>
        <#list identifiers as identifier>
            <tr>
                <td>${identifier}</td>
            </tr>
        </#list>
    </table>

    <table summary="Associated Individuals">
        <caption>AssociatedIndividuals: 
            <#if matchingProperty??>
                (match by <pre>${matchingProperty}</pre>)
            <#else>
                (matching property is not defined)
            </#if>
        </caption>
        <#if associatedIndividuals?has_content>
            <#list associatedIndividuals as associatedIndividual>
                <tr>
                    <td>${associatedIndividual.uri}</td>
                    <#if associatedIndividual.editable>
                        <td>May edit</td>
                    <#else>
                        <td>May not edit</td>
                    </#if>
                </tr>
            </#list>
        <#else>
            <tr><td>none</td></tr>
        </#if>
    </table>

    <table summary="Active Identifier Factories">
        <caption>Identifier factories:</caption>
        <#list factories as factory>
            <tr>
                <td>${factory}</td>
            </tr>
        </#list>
    </table>

    <table summary="Policies">
        <caption>Policies:</caption>
        <#list policies as policy>
            <tr>
                <td>${policy}</td>
            </tr>
        </#list>
    </table>
</section>
