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
        <p><strong>Error message:</strong> ${adminErrorData.errorMessage?html}</p>
    </#if>
    <#if adminErrorData.stackTrace?has_content>
        <p>
            <strong>Stack trace</strong> (full trace available in the vivo log): ${adminErrorData.stackTrace?html}
        </p>
                   
        <#if adminErrorData.cause?has_content>
            <p><strong>Caused by:</strong> ${adminErrorData.cause?html}</p>            
        </#if>
    </#if>  

<#elseif ! errorOnHomePage> <#-- view for other users -->
    <p>Return to the <a href="${urls.home}" title="home page">home page</a></p> 
</#if>

