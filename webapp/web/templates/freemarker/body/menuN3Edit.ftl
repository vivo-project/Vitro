<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<div id="navigationManagement">
    <h2>Navigation Management</h2>
    
    <#if errorMessage??>
        <div id="errorAlert">
            <img src="../images/iconAlert.png" alert="Error alert icon" height="31" width="32">
            <p>${errorMessage}</p>
        </div>
    </#if>
    
    <#if message??>
        <p>${message}</p>
    </#if>
    
    <#if menuN3??>
        <form class="" action="${urls.base}/${currentPage}" method="post">
            <label for="navigatioN3">Setup the primary navigational menu for your website</label>
            <textarea name="navigationN3" id="navigationN3" cols="45" rows="40" class="maxWidth">
                ${menuN3}
            </textarea>
            <input name="submit" id="submit" value="Save" type="submit"/> or <a href="#">Cancel</a>
        </form>
    </#if>
</div>                     