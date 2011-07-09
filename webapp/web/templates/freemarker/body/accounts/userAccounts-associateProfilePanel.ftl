<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for setting the account reference field, which can also associate a profile with the user account -->

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/autocomplete.css" />',
                   '<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}

    <div id="associateProfileBackgroundOne">
        <div style="margin-left:8px">
            <label for="externalAuthId">External Auth. ID / Matching ID</label> 
            <input type="text" name="externalAuthId" value="${externalAuthId}" id="externalAuthId" role="input "/>
            <span id="externalAuthIdInUse"  style="display: none;" >This Account Reference is already in use.</span>
            <p class="explanatoryText" style="margin-top:-8px">Can be used to associate the account with the user's profile via the matching property.</p>
        </div>
    </div>
    <#-- If there is an associated profile, show these -->
    <div id="associated">
        <div id="associateProfileBackgroundTwo">
            <p>
                <label for="associatedProfileName">Associated profile:</label>
                <span class="acSelectionInfo" id="associatedProfileName"></span>
                <a href="" id="verifyProfileLink">(verify this match)</a>
                <a href="" id="changeProfileLink">(change profile)</a>
            </p>
            <input type="hidden" id="associatedProfileUri" name="associatedProfileUri" value="" />
        </div>
    </div>
            
    <#-- If we haven't selected one, show these instead -->
    <div id="associationOptions">
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
                <select name="degreeUri" id="degreeUri" >
                    <option value="" selected="selected">Select one</option>
                    <#list profileTypes?keys as key>
                        <option value="${key}" <#if degreeUri = key> selected </#if> >${profileTypes[key]}</option>           
                    </#list>    
                </select>    
            </p>
        </div>
    </div>

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
        ajaxUrl: '${formUrls.accountsAjax}'
    </#if>
};
</script>

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/account/accountAssociateProfile.js"></script>')}   

