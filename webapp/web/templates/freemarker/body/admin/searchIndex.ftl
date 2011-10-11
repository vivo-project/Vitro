<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- 
    Template for the page that controls the updating or rebuilding of the Search Index. 
-->

<h1>Search Index Status</h1>

<#if worklevel == "IDLE">
    <#if hasPreviousBuild??>
        <p>Previous activity completed at ${since?string("hh:mm:ss a, MMMM dd, yyyy")}</p>
    </#if>
    
    <form action="${actionUrl}" method="POST">
        <input type="submit" name="update" value="Update">
        Add the latest changes to the index.
        <br>
        <input type="submit" name="rebuild" value="Rebuild">
        Start with an empty index and build it completely.
    </form>
<#else>
    <p>Active since ${since?string("hh:mm:ss a, MMMM dd, yyyy")}</p>
</#if>
