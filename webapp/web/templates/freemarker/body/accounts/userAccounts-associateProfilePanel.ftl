<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for setting the account reference field, which can also associate a profile with the user account -->

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/autocomplete.css" />',
                   '<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}
<section id="externalAuthMatchId">
    <div id="associateProfileBackgroundOne">
        <div id="alignExternalAuthId">
        <#if showAssociation??>
             <label for="externalAuthId">External Auth. ID / Matching ID</label> 
            <input type="text" name="externalAuthId" value="${externalAuthId}" id="externalAuthId" role="input "/>
            <span id="externalAuthIdInUse" >This Identifier is already in use.</span>
            <p class="explanatoryText">Can be used to associate the account with the user's profile via the matching property.</p>
        <#else>
            <label for="externalAuthId">External Authentication ID</label> 
            <input type="text" name="externalAuthId" value="${externalAuthId}" id="externalAuthId" role="input "/>
            <span id="externalAuthIdInUse" >This Identifier is already in use.</span>
        </#if>
        </div>
    </div>
    </section>
    <#-- If there is an associated profile, show these -->
    <section id="associated">
        <div id="associateProfileBackgroundTwo">
            <p>
                <label for="associatedProfileName">Associated profile:</label>
                <span class="acSelectionInfo" id="associatedProfileName"></span>
                <a href="" id="verifyProfileLink" title="verify this match">(verify this match)</a>
                <a href="" id="changeProfileLink" title="change profile">(change profile)</a>
            </p>
            <input type="hidden" id="associatedProfileUri" name="associatedProfileUri" value="" />
        </div>
    </section>
            
    <#-- If we haven't selected one, show these instead -->
    <section id="associationOptions">
        <div id="associateProfileBackgroundThree">
            <p>
                <label for="associateProfileName">Select the associated profile</label>
                <input type="text" id="associateProfileName" name="associateProfileName" class="acSelector" size="35">
            </p>
        </div>
        <div id="associateProfileBackgroundFour">
            <p> - or - </p>
            <p> 
                <label for="">Create the associated profile</label> 
                <select name="newProfileClassUri" id="newProfileClassUri" >
                    <option value="" selected="selected">Select one</option>
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

