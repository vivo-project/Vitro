<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Log in template for accessing site admin -->

${stylesheets.addFromTheme("/login.css")}
<noscript><p class="alertNoJavaScript"></p></noscript>

<div id="formLogin" class="pageBodyGroup">

       <h2>Log in</h2>
       
       <#if infoMessage??>
           <h3>${infoMessage}</h3>
       </#if>
       
       <#if errorMessage??>
           <div id="errorAlert"><img src="${alertImageUrl}" width="24" height="24" alert="Error alert icon"/>
                  <p>${errorMessage}</p>
           </div>
       </#if>
       
       <form action="${formAction}" method="post" onsubmit="return isValidLogin(this)">
              <label for="loginName">Email</label>
              <input name="loginName" type="text" value="${loginName}"  />
              <label for="loginPassword">Password</label>
              <input type="password" name="loginPassword"  />
              <input name="loginForm"  type="submit" class="submit" value="Log in"/>
       </form>
</div>

