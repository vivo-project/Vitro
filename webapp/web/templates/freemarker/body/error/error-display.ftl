<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for general system error. -->

<p>
    There was an error in the system. 
    <#if sentEmail>
        This error has been reported to the site administrator. 
    </#if>
</p>  
    
<#if adminErrorData??> <#-- view for site administrators -->
   <#if adminErrorData.errorMessage?has_content>
        <p><strong>Error message:</strong> ${adminErrorData.errorMessage}</p>
    </#if>
    <#if adminErrorData.stackTrace?has_content>
        <div>
            <p><strong>Stack trace</strong> (full trace available in the vivo log):</p>
            ${adminErrorData.stackTrace}
        </div>
    </#if>    
<#elseif ! errorOnHomePage> <#-- view for other users -->
    <p>Return to the <a href="${urls.home}">home page</a>.</p> 
</#if>

