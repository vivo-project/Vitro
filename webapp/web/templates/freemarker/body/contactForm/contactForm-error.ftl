<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Contact form processing errors -->

<h2>${title}</h2>

<#if errorMessage?has_content>       
    <section id="error-alert"><img src="${urls.images}/iconAlert.png" role="error alert"/>
        <p>${errorMessage}</p>
    </section>
</#if>

<p class="contactUsReturnHome">Return to the <a href="${urls.home}" title="home page">home page</a>.</p> 