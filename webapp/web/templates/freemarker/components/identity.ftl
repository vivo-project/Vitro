<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<div id="identity">

    <h1><a title="Home" href="${urls.home}">${siteName}</a></h1>
    
    <#-- RY We will need this in non-NIHVIVO versions
    <#if tagline.has_content>
        <em>${tagline}</em>
    </#if>
    -->
    
    <ul id="otherMenu">    
        <#if loginName??>
            <li>
                Logged in as <strong>${loginName}</strong> (<a href="${urls.logout}">Log out</a>)     
            </li> 
            <li><a href="${urls.siteAdmin}">Site Admin</a></li> 
        <#else>
             <li><a title="log in to manage this site" href="${urls.login}">Log in</a></li>
        </#if> 
        
        <li><a href="${urls.about}$">About</a></li>
        <li <#if !urls.contact??>class="last"</#if>><a href="${urls.aboutFM}">About - FM</a></li>
        <#if urls.contact??>
            <li class="last"><a href="${urls.contact}">Contact Us</a></li>
        </#if>        
    </ul>   
</div>