<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- pageSetup.ftl is automatically included for all page template requests.
It provides an avenue to assign variables related to display, which fall outside
the domain of the controllers. -->

<#assign bodyClasses>
    <#-- The compress directives and formatting here resolve whitespace issues in output; please do not alter them. -->
    <#compress>
    <#assign bodyClassList = [currentServlet!]>
    
    <#if user.loggedIn> 
        <#assign bodyClassList = bodyClassList + ["loggedIn"]/>
    </#if> 
    <#list bodyClassList as class>${class}<#if class_has_next> </#if></#list>
    </#compress>
</#assign>