<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#include "doctype.html">

<#include "head.ftl">

<body>
    <div id="wrap" class="container">
        <div id="header">
        
            <#include "identity.ftl">
            
            <div id="navAndSearch" class="block">
                <#include "menu.ftl">  
                <#include "search.ftl">
            </div> <!-- navAndSearch --> 
                      
        </div> <!-- header --> 

        <hr class="hidden" />

        <div id="contentwrap">      
            <div id="content" <#if contentClass??> class="${contentClass}" </#if>
                ${body} 
            </div> <!-- content -->
        </div> <!-- contentwrap -->
    
        <#include "footer.ftl">
                                      
    </div> <!-- wrap -->  
</body>
</html>
