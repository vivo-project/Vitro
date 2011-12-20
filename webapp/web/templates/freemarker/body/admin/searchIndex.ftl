<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- 
    Template for the page that controls the updating or rebuilding of the Search Index. 
-->

<h2>Search Index Status</h2>

<#if worklevel == "IDLE">
    <#if hasPreviousBuild??>
        <p>Most recent update was at ${since?string("hh:mm:ss a, MMMM dd, yyyy")}</p>
    </#if>
    
    <form action="${actionUrl}" method="POST">
        <p>
            <input class="submit" type="submit" name="rebuild" value="Rebuild" role="button" />
            Reset the search index and re-populate it.
        </p>
    </form>
<#else>
    <h3>The search index is currently being ${currentTask}.</h3>
    <p>since ${since?string("hh:mm:ss a, MMMM dd, yyyy")}, elapsed time ${elapsed}, estimated total time ${expected}</p>
    <p>Completed ${completedCount} out of ${totalToDo} index records.</p>
</#if>
