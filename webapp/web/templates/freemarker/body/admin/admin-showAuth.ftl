<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template viewing the authorization mechanisms: current identifiers, factories, policies, etc. -->

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/showAuth.css" />')}

<h2>Authorization Info</h2>

<section id="show-auth" role="region">
    <h4>Current user</h4>
    <table summary="Information about the current user">
    <#if currentUser?has_content>
            <tr><th>URI:</th><td>${currentUser.uri}</td></tr>
            <tr><th>First name:</th><td>${currentUser.firstName}</td></tr>
            <tr><th>Last name:</th><td>${currentUser.lastName}</td></tr>
            <tr><th>Email:</th><td>${currentUser.emailAddress}</td></tr>
            <tr><th>External Auth ID:</th><td>${currentUser.externalAuthId}</td></tr>
            <tr><th>Login count:</th><td>${currentUser.loginCount}</td></tr>
            <#list currentUser.permissionSetUris as role>
                <tr><th>Role:</th><td>${role}</td></tr>
            </#list>
    <#else>
        <tr><th>Not logged in</th></tr>
    </#if>
    </table>
   
    <h4>Identifiers:</h4>
    <table summary="Identifiers">
        <#list identifiers as identifier>
            <tr>
                <td>${identifier}</td>
            </tr>
        </#list>
    </table>

    <h4>
        AssociatedIndividuals: 
        <#if matchingProperty??>
            (match by ${matchingProperty})
        <#else>
            (matching property is not defined)
        </#if>
    </h4>
    <table summary="Associated Individuals" width="100%">
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

    <h4>Identifier factories:</h4>
    <table summary="Active Identifier Factories" width="100%">
        <#list factories as factory>
            <tr>
                <td>${factory}</td>
            </tr>
        </#list>
    </table>

    <h4>Policies:</h4>
    <table summary="Policies" width="100%">
        <#list policies as policy>
            <tr>
                <td>${policy}</td>
            </tr>
        </#list>
    </table>
</section>
