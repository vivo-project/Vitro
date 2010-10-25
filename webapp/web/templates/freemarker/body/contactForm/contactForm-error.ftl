<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Contact form processing errors -->

<h2>${title}</h2>

    <#if errorMessage?has_content>       

        <div id="errorAlert"><img src="${urls.siteIcons}/iconAlert.png"/>
                  <p>${errorMessage}</p>
           </div>
       
    </#if>


<p class="contactUsReturnHome">Return to the <a href="${urls.home}">home page</a>.</p> 