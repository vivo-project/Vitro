<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#import "lib-list.ftl" as l>

<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "head.ftl">
    </head>
    
    <body class="${bodyClasses!}">
        <#include "identity.ftl">
        <#include "search.ftl">
        <#include "menu.ftl">
        
        ${body}
        
        <#include "footer.ftl">
    </body>
</html>