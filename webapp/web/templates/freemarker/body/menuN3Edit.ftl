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
      <label for="navigatioN3"></label>
      <textarea name="navigationN3" id="textarea" cols="45" rows="40">
      ${menuN3}
      </textarea>
      <input name="submit" class="submit" value="Save" type="submit"/>
      or <a href="#">Cancel</a>
      <p class="small red">&nbsp;</p>
    </form>
    </#if>

</div>                     