<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<header id="branding" role="banner">
    <h1 class="vivo-logo"><a href="${urls.home}" title="${siteName}"><span class="displace">${siteName}</span></a></h1>
    <#if siteTagline?has_content>
        <em>${siteTagline}</em>
    </#if>

    <nav role="navigation">
        <ul id="header-nav" role="list">
            <#include "languageSelector.ftl">
            <#if user.loggedIn>
                <li role="listitem">${user.loginName}</li>
                <li role="listitem"><a href="${urls.logout}" title="${i18n().end_your_Session}">${i18n().log_out}</a></li>
                <#if user.hasSiteAdminAccess>
                    <li role="listitem"><a href="${urls.siteAdmin}" title="${i18n().manage_site}">${i18n().site_admin}</a></li>
                </#if>
            <#else>
                <li role="listitem"><a href="${urls.login}" title="${i18n().login_to_manage_site}" >${i18n().login_button}</a></li>
            </#if>
            <#-- List of links that appear in submenus, like the header and footer. -->
                <li role="listitem"><a href="${urls.about}" title="${i18n().more_details_about_site}">${i18n().about}</a></li>
            <#if urls.contact??>
                <li role="listitem"><a href="${urls.contact}" title="${i18n().send_feedback_questions}">${i18n().contact_us}</a></li>
            </#if>
                <li role="listitem"><a href="http://www.vivoweb.org/support" title="${i18n().visit_project_website}" target="blank">${i18n().support}</a></li>
                <li role="listitem"><a href="${urls.index}" title="${i18n().view_content_index}">${i18n().index}</a></li>
        </ul>
    </nav>
    
    <section id="search" role="region">
        <fieldset>
            <legend>${i18n().search_form}</legend>
            
            <form id="search-form" action="${urls.search}" name="search" role="search" accept-charset="UTF-8" method="POST"> 
                <div id="search-field">
                    <input type="text" name="querytext" class="search-vitro" value="${querytext!}" autocapitalize="off" />
                    <input type="submit" value="${i18n().search_button}" class="submit">
                </div>
            </form>
        </fieldset>
    </section>
</header>
