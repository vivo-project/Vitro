<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Log in template for accessing site admin -->


${stylesheets.addFromTheme("/login.css")}

<div id="formLogin" class="pageBodyGroup">
       <h2>Create Your New Password</h2>
       
       <#if errorMessage??>
           <div id="errorAlert"><img src="${alertImageUrl}" width="24" height="24" alert="Error alert icon"/>
                  <p>${errorMessage}</p>
           </div>
       </#if>
       
       <form action="${formAction}" method="post" onsubmit="return isReasonableNewPassword(this)">
              <label for="newPassword">Password</label>
              <input type="password" name="newPassword"  />
              <label for="confirmPassword">Confirm Password</label>
              <input type="password" name="confirmPassword"  />
              <input name="passwordChangeForm" type="submit" class="submit" value="Save Changes"/>
       </form>
</div>

