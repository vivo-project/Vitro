<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template viewing the authorization mechanisms: current identifiers, factories, policies, etc. -->

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/showAuth.css" />')}

<h2>Authorization Info</h2>

<section id="show-auth" role="region">
    <h4>${i18n().current_user}</h4>
    <table summary="Information about the current user">
    <#if currentUser?has_content>
            <tr><th>URI:</th><td>${currentUser.uri}</td></tr>
            <tr><th>${i18n().first_name}:</th><td>${currentUser.firstName}</td></tr>
            <tr><th>${i18n().last_name}:</th><td>${currentUser.lastName}</td></tr>
            <tr><th>${i18n().email_address}:</th><td>${currentUser.emailAddress}</td></tr>
            <tr><th>${i18n().external_auth_id}:</th><td>${currentUser.externalAuthId}</td></tr>
            <tr><th>${i18n().login_count}:</th><td>${currentUser.loginCount}</td></tr>
            <#list currentUser.permissionSetUris as role>
                <tr><th>${i18n().user_role}:</th><td>${role}</td></tr>
            </#list>
    <#else>
        <tr><th>${i18n().not_logged_in}</th></tr>
    </#if>
    </table>
   
    <h4>${i18n().identifiers}:</h4>
    <table summary="Identifiers">
        <#list identifiers as identifier>
            <tr>
                <td>${identifier}</td>
            </tr>
        </#list>
    </table>

    <h4>
        ${i18n().associated_individuals}: 
        <#if matchingProperty??>
            (${i18n().match_by(matchingProperty)})
        <#else>
            (${i18n().matching_prop_not_defined})
        </#if>
    </h4>
    <table summary="Associated Individuals">
        <#if associatedIndividuals?has_content>
            <#list associatedIndividuals as associatedIndividual>
                <tr>
                    <td>${associatedIndividual.uri}</td>
                    <#if associatedIndividual.editable>
                        <td>${i18n().may_edit}</td>
                    <#else>
                        <td>${i18n().may_not_edit}</td>
                    </#if>
                </tr>
            </#list>
        <#else>
            <tr><td>${i18n().none}</td></tr>
        </#if>
    </table>

    <h4>${i18n().identifier_factories}:</h4>
    <table summary="Active Identifier Factories">
        <#list factories as factory>
            <tr>
                <td>${factory}</td>
            </tr>
        </#list>
    </table>

    <h4>${i18n().policies}:</h4>
    <table summary="Policies" width="100%">
        <#list policies as policy>
            <tr>
                <td>${policy}</td>
            </tr>
        </#list>
    </table>

    <h4>${i18n().authenticator}:</h4>
    <table summary="Authenticator" width="100%">
        <tr>
            <td>${authenticator}</td>
        </tr>
    </table>
</section>
