<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Test widget -->

<#macro assets>
    ${stylesheets.add("/css/test.css")}
    ${scripts.add("/js/testscript.js")}
    ${headScripts.add("/js/testheadscript.js")} 
</#macro>

<#macro markup>
<#import "lib-list.ftl" as l>
    <div class="testWidget">
        <h4>This is the test widget using macros.</h4>    
        <p>I like ${fruit}.</p>
    </div>
</#macro>

<#macro altMarkup>
    <div class="testWidget">
        <h4>This is the alternate version of the test widget.</h4>    
        <p>I hate ${fruit}.</p>
    </div>
</#macro>