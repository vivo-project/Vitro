<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for setting the account reference field, which can also associate a profile with the user account -->

<#assign strings = i18n() />

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/autocomplete.css" />',
                   '<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}
<section id="externalAuthMatchId">
    <div id="associateProfileBackgroundOne">
        <div id="alignExternalAuthId">
        <#if showAssociation??>
             <label for="externalAuthId">${strings.auth_matching_id_label}</label> 
            <input type="text" name="externalAuthId" value="${externalAuthId}" id="externalAuthId" role="input "/>
            <span id="externalAuthIdInUse" >${strings.auth_id_in_use}</span>
            <p class="explanatoryText">${strings.auth_id_explanation}</p>
        <#else>
            <label for="externalAuthId">${strings.auth_id_label}</label> 
            <input type="text" name="externalAuthId" value="${externalAuthId}" id="externalAuthId" role="input "/>
            <span id="externalAuthIdInUse" >${strings.auth_id_in_use}</span>
        </#if>
        </div>
    </div>
    </section>
    <#-- If there is an associated profile, show these -->
    <section id="associated">
        <div id="associateProfileBackgroundTwo">
            <p>
                <label for="associatedProfileName">${strings.associated_profile_label}</label>
                <span class="acSelectionInfo" id="associatedProfileName"></span>
                <a href="" id="verifyProfileLink" title="${strings.verify_this_match_title}">(${strings.verify_this_match})</a>
                <a href="" id="changeProfileLink" title="${strings.change_profile_title}">(${strings.change_profile})</a>
            </p>
            <input type="hidden" id="associatedProfileUri" name="associatedProfileUri" value="" />
        </div>
    </section>
            
    <#-- If we haven't selected one, show these instead -->
    <section id="associationOptions">
        <div id="associateProfileBackgroundThree">
            <p>
                <label for="associateProfileName">${strings.select_associated_profile}</label>
                <input type="text" id="associateProfileName" name="associateProfileName" class="acSelector" size="35">
            </p>
        </div>
        <div id="associateProfileBackgroundFour">
            <p> - ${strings.or} - </p>
            <p> 
                <label for="">${strings.create_associated_profile}</label> 
                <select name="newProfileClassUri" id="newProfileClassUri" >
                    <option value="" selected="selected">${strings.select_one}</option>
                    <#list profileTypes?keys as key>
                        <option value="${key}" <#if newProfileClassUri = key> selected </#if> >${profileTypes[key]}</option>           
                    </#list>    
                </select>    
            </p>
        </div>
    </section>

<script type="text/javascript">
var associateProfileFieldsData = {
    <#if userUri??>
        userUri: '${userUri}' ,
    <#else>    
        userUri: '' ,
    </#if>
    
    <#if associationIsReset??>
        associationIsReset: 'true' ,
    </#if>
    
    <#if associatedProfileInfo??>
        associatedProfileInfo: {
            label: '${associatedProfileInfo.label}',
            uri: '${associatedProfileInfo.uri}',
            url: '${associatedProfileInfo.url}'
        },
    </#if>
    
    <#if showAssociation??>
        associationEnabled: true ,
    </#if>

    ajaxUrl: '${formUrls.accountsAjax}'
};
</script>

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/account/accountAssociateProfile.js"></script>')}   

