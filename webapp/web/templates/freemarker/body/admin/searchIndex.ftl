<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- 
    Template for the page that controls the updating or rebuilding of the Search Index. 
-->

<h2>${i18n().search_index_status}</h2>

<#if !indexIsConnected>
    <!-- Can't contact the Solr server. Indexing would be impossible. Show an error message. -->
    <section id="error-alert" role="alert">
        <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon" />
        <p>${i18n().search_index_not_connected}</p>
        <p><tt>SolrServer.ping()</tt> ${i18n().failed}.
        <p>${i18n().check_startup_status}</p>
    </section>
    
<#elseif worklevel == "IDLE">
    <!-- Solr indexer is idle. Show the button that rebuilds the index. -->
    <h3>${i18n().search_indexer_idle}</h3>
    <#if hasPreviousBuild??>
        <p>${i18n().most_recent_update} ${since?string("hh:mm:ss a, MMMM dd, yyyy")}</p>
    </#if>
    
    <form action="${actionUrl}" method="POST">
        <p>
            <input class="submit" type="submit" name="rebuild" value="${i18n().rebuild_button}" role="button" />
            ${i18n().reset_search_index}
        </p>
    </form>
    
<#elseif totalToDo == 0>
    <!-- Solr indexer is preparing the list of records. Show elapsed time since request. -->
    <h3>${i18n().preparing_to_rebuild_index}</h3>
    <p>${i18n().since_elapsed_time(since?string("hh:mm:ss a, MMMM dd, yyyy"),elapsed)}</p>
    
<#else>
    <!-- Solr indexer is re-building the index. Show the progress. -->
    <h3>${i18n().current_task(currentTask)}</h3>
    <p>${i18n().since_elapsed_time_est_total(since?string("hh:mm:ss a, MMMM dd, yyyy"),elapsed,expected)}</p>
    <p>${i18n().index_recs_completed(completedCount,totalToDo)}</p>
    
</#if>
