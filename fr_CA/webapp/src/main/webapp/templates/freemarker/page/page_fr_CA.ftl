<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#import "lib-list.ftl" as l>

<!DOCTYPE html>
<!-- page.ftl  -->
<html lang="${country}">


    <head>
        <#include "head.ftl">
    </head>
    
    <body class="${bodyClasses!}">
        <header id="branding" role="banner">
            <#include "identity.ftl">
            <#include "search.ftl">
        </header>
        <#include "menu.ftl">

        <div id="wrapper-content" role="main">
            ${body}
        </div>
        <#include "footer.ftl">
    </body>
</html>