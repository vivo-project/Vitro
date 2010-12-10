<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<div class="contents searchForm">

    <div class="advancedSearchForm">
        <form name="filterForm" method="post" action="search">
            <h3>Search</h3>
            <input class="top_padded" name="querytext" value="" type="text" size="50" />
            <p><input id="submit" value="Search" type="submit"/></p>
        </form>     
    </div><!--advancedSearchForm--> 
    
    <div class="searchTips">
        <#include "search-help.ftl">
    </div>                  

</div>

${stylesheets.addFromTheme("/css/search.css")}