<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<div id="identity">

    <h1><a title="Home" href="${urls.home}">${siteName}</a></h1>
    
    <#-- RY We will need this in non-NIH VIVO versions
    <#if tagline.has_content>
        <em>${tagline}</em>
    </#if>
    -->
    
    <ul id="otherMenu">    
        <#if loginName??>
            <li class="border">
                Logged in as <strong>${loginName}</strong> (<a href="${urls.logout}">Log out</a>)     
            </li> 
            <li class="border"><a href="${urls.siteAdmin}">Site Admin</a></li> 
        <#else>
             <li class="border"><a title="log in to manage this site" href="${urls.login}">Log in</a></li>
        </#if> 
        
        <li class="border"><a href="${urls.about}$">About</a></li>
        <li <#if urls.contact??>class="border"</#if>><a href="${urls.aboutFM}">About - FM</a></li>
        <#if urls.contact??>
            <li><a href="${urls.contact}">Contact Us</a></li>
        </#if>        
    </ul>   
</div>