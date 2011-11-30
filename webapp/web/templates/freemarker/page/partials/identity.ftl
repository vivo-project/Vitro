<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<header id="branding" role="banner">
    <h1 class="vivo-logo"><a href="${urls.home}" title="site name"><span class="displace">${siteName}</span></a></h1>
    <#if siteTagline?has_content>
        <em>${siteTagline}</em>
    </#if>

    <nav role="navigation">
        <ul id="header-nav" role="list">
            <#if user.loggedIn>
                <li role="listitem">${user.loginName}</li>
                <li role="listitem"><a href="${urls.logout}" title="End your session">Log out</a></li>
                <#if user.hasSiteAdminAccess>
                    <li role="listitem"><a href="${urls.siteAdmin}" title="Manage this site">Site Admin</a></li>
                </#if>
            <#else>
                <li role="listitem"><a href="${urls.login}" title="Log in to manage this site" >Log in</a></li>
            </#if>
            <#-- List of links that appear in submenus, like the header and footer. -->
                <li role="listitem"><a href="${urls.about}" title="More details about this site">About</a></li>
            <#if urls.contact??>
                <li role="listitem"><a href="${urls.contact}" title="Send us your feedback or ask a question">Contact Us</a></li>
            </#if>
                <li role="listitem"><a href="http://www.vivoweb.org/support" title="Visit the national project web site" target="blank">Support</a></li>
                <li role="listitem"><a href="${urls.index}" title="View an outline of the content in this site">Index</a></li>
        </ul>
    </nav>
    
    <section id="search" role="region">
        <fieldset>
            <legend>Search form</legend>
            
            <form id="search-form" action="${urls.search}" name="search" role="search" accept-charset="UTF-8" method="POST"> 
                <div id="search-field">
                    <input type="text" name="querytext" class="search-vitro" value="${querytext!}" autocapitalize="off" />
                    <input type="submit" value="Search" class="submit">
                </div>
            </form>
        </fieldset>
    </section>
</header>
