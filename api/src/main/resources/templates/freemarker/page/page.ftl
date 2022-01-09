<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#include "doctype.html">

<#include "head.ftl">

<body class="${bodyClasses!}">
    <div id="wrap" class="container">
        <div id="header">

            <header id="branding" role="banner">
                <#include "identity.ftl">
            </header>

            <#-- Note to UI team: do not change this div without also making the corresponding change in menu.jsp -->
            <div id="navAndSearch" class="block">
                <#include "menu.ftl">
                <#include "search.ftl">
            </div> <!-- navAndSearch -->

            <#include "breadcrumbs.ftl">
        </div> <!-- header -->

        <hr class="hidden" />

        <div id="contentwrap">
            <#include "flash.ftl">

            <div id="content">
                ${body}
            </div> <!-- content -->
        </div> <!-- contentwrap -->

        <#include "footer.ftl">
    </div> <!-- wrap -->

    <#include "scripts.ftl">
</body>
</html>

