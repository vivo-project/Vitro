<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Crop the replacement main image for an Individual, to produce a thumbnail. -->

<h2>Forced password change</h2>

${stylesheets.addFromTheme("/login.css")}

<div id="formLogin" class="pageBodyGroup">
       <h2>Create Your New Password</h2>
       
       <#if errorMessage??>
           <div id="errorAlert"><img src="${alertImageUrl}" width="32" height="31" alert="Error alert icon"/>
                  <p>${errorMessage}</p>
           </div>
       </#if>
       
       <form action="${formAction}" method="post" onsubmit="return isReasonableNewPassword(this)">
              <label for="newPassword">Password</label>
              <input type="password" name="newPassword"  />
              <label for="confirmPassword">Confirm Password</label>
              <input type="password" name="confirmPassword"  />
              <br />
              <input name="passwordChangeForm" type="submit" class="submit" value="Save Changes"/>
       </form>
</div>

