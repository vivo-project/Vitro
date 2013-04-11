<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#import "lib-list.ftl" as l>

<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "head.ftl">
    </head>
    
    <body class="${bodyClasses!}">
        <#include "identity.ftl">
        
        <section id="search" role="region">
            <fieldset>
                <legend>Search form</legend>

                <form id="search-form" action="${urls.search}" name="search" role="search" accept-charset="UTF-8" method="POST"> 
                    <div id="search-field">
                        <input type="text" name="querytext" class="search-vitro" value="${querytext!}" autocapitalize="off" />
                        <input type="submit" value="Search" class="search">
                    </div>
                </form>
            </fieldset>
        </section>
        
        <#include "menu.ftl">
        
        ${body}
        
        <#include "footer.ftl">
    </body>
</html>