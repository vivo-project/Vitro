<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for general system error. -->

<p>
    There was an error in the system. 
    <#-- This error has been reported to the site administrator. -->
</p>  
    
<#if errorData??> <#-- view for site administrators -->
   <#if errorData.errorMessage?has_content>
        <p><strong>Error message:</strong> ${errorData.errorMessage}</p>
    </#if>
    <#if errorData.stackTrace?has_content>
        <div>
            <p><strong>Stack trace</strong> (full trace available in the vivo log):</p>
            ${errorData.stackTrace}
        </div>
    </#if>    
<#elseif ! urls.currentPage?ends_with("home")> <#-- view for other users -->
    <p>Return to the <a href="${urls.home}">home page</a>.</p> 
</#if>

