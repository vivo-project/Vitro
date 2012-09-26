<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- 
    Template for the page that controls the updating or rebuilding of the Search Index. 
-->

<h2>Search Index Status</h2>

<#if !indexIsConnected>
    <!-- Can't contact the Solr server. Indexing would be impossible. Show an error message. -->
    <section id="error-alert" role="alert">
        <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon" />
        <p>The search index is not connected.</p>
        <p><tt>SolrServer.ping()</tt> failed.
        <p>Check startup status page and/or Tomcat logs for more information.</p>
    </section>
    
<#elseif worklevel == "IDLE">
    <!-- Solr indexer is idle. Show the button that rebuilds the index. -->
    <h3>The search indexer is idle.</h3>
    <#if hasPreviousBuild??>
        <p>The most recent update was at ${since?string("hh:mm:ss a, MMMM dd, yyyy")}</p>
    </#if>
    
    <form action="${actionUrl}" method="POST">
        <p>
            <input class="submit" type="submit" name="rebuild" value="Rebuild" role="button" />
            Reset the search index and re-populate it.
        </p>
    </form>
    
<#elseif totalToDo == 0>
    <!-- Solr indexer is preparing the list of records. Show elapsed time since request. -->
    <h3>Preparing to rebuild the search index. </h3>
    <p>since ${since?string("hh:mm:ss a, MMMM dd, yyyy")}, elapsed time ${elapsed}</p>
    
<#else>
    <!-- Solr indexer is re-building the index. Show the progress. -->
    <h3>${currentTask} the search index.</h3>
    <p>since ${since?string("hh:mm:ss a, MMMM dd, yyyy")}, elapsed time ${elapsed}, estimated total time ${expected}</p>
    <p>Completed ${completedCount} out of ${totalToDo} index records.</p>
    
</#if>
