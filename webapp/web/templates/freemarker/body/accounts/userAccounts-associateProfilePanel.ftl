<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for setting the account reference field, which can also associate a profile with the user account -->

<table>
    <tr>
        <td>
            <label for="externalAuthId">Account Reference</label> 
            <input type="text" name="externalAuthId" value="${externalAuthId}" id="externalAuthId" role="input "/>
            <p>
                External Auth. ID or other unique ID. Can associate the account to the user's profile.
            </p>
            <p id="externalAuthIdInUse">
                That Account Reference is already in use.
            </p>
        </td>
        <td>
            <#-- If there is an associated profile, show these -->
            <div id="associated">
                <p>
                    <label for="associatedProfileName">Associated profile:</label>
                    <span class="acSelectionInfo" id="associatedProfileName"></span>
                    <a href="" id="verifyProfileLink">(verify this match)</a>
                </p>
                <input type="hidden" id="associatedProfileUri" name="associatedProfileUri" value="" />
            </div>
            
            <#-- If we haven't selected one, show these instead -->
            <div id="associationOptions">
                <p>
                    <label for="associateProfileName">Select an existing profile</label>
                    <input type="text" id="associateProfileName" name="associateProfileName" class="acSelector" size="35">
                </p>
                <p> - or - </p>
                <p> 
                    <label for="">Create an associated profile</label> 
                    <select name="degreeUri" id="degreeUri" >
                        <option value="" selected="selected">Select one</option>
                        <option value="" disabled>Bogus</option>
                    </select>    
                </p>
            </div>
            
        </td>
    </tr>
</table>

<script type="text/javascript">
var associateProfileFieldsData = {
    ajaxUrl: '${formUrls.accountsAjax}'
};
</script>

${scripts.add('<script type="text/javascript" src="${urls.base}/js/account/accountAssociateProfile.js"></script>')}   

