<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for displaying list of user accounts -->

<#import "userAccounts-accountsNav.ftl" as p>

<#assign strings = i18n() />

<form method="POST" action="${formUrls.add}" id="add-account" class="customForm" role="Add account">
    <h3><span>${strings.user_accounts_link} | </span><input type="submit" class="submit add-account" value="${strings.add_new_account}" /></h3>
</form>

<#if newUserAccount?? >
    <section class="account-feedback">
        <p>
            ${strings.new_account_1}
            <a href="${newUserAccount.editUrl}" title="${strings.new_account_title}">${newUserAccount.firstName} ${newUserAccount.lastName}</a>
            ${strings.new_account_2}
            <#if emailWasSent?? >${strings.new_account_notification(newUserAccount.emailAddress)}</#if>
        </p>
    </section>
</#if>

<#if updatedUserAccount?? >
    <section class="account-feedback">
        <p>
            ${strings.updated_account_1}
            <a href="${updatedUserAccount.editUrl}" title="${strings.updated_account_title}">${updatedUserAccount.firstName} ${updatedUserAccount.lastName}</a>
            ${strings.updated_account_2}
            <#if emailWasSent?? >${strings.updated_account_notification(updatedUserAccount.emailAddress)}</#if>
        </p>
    </section>
</#if>

<#if deletedAccountCount?? >
    <section class="account-feedback">
        <p>
            ${strings.deleted_accounts(deletedAccountCount)}
        </p>
    </section>
</#if>

<section id="filter-roles">
    <form method="POST" action="${formUrls.list}" class="customForm" role="filter by roles">
        <select name="roleFilterUri" id="roleFilterUri">
            <option value="" <#if roleFilterUri = "">selected</#if> >${strings.filter_by_roles}</option>
            <#list roles as role>
            <option value="${formUrls.list}?roleFilterUri=${role.uri?url}" <#if roleFilterUri = role.uri>selected</#if> >${role.label}</option>
            </#list>
            <!--
            When roleFilterUri or searchTerm changes,
            pageIndex should be set to 1. When any of these changes (including pageIndex), the form 
            should be submitted.
            -->
        </select>
        
        <#if roleFilterUri?has_content>
             <span><a href="${formUrls.list}" title="${strings.view_all_accounts_title}">${strings.view_all_accounts}</a></span>
        </#if>
    </form>
</section>

<section id="search-accounts">
    <form method="POST" action="${formUrls.list}" class="customForm" role="search accounts">
        <input type="text" name="searchTerm" />
        <input class="submit" type="submit" value="${strings.search_accounts_button}"/>
        <!--
            When searchTerm changes, 
            set pageIndex to 1
            set orderDirection to "ASC"
            set orderField to "email" 
            submit the form (submit action is "list") 
        -->
    </form>
</section>
<#if searchTerm?has_content>
<section id="search-feedback">
    <p>${strings.accounts_search_results} "<strong>${searchTerm}</strong>" | <span><a href="${formUrls.list}" title="${strings.view_all_accounts_title}">${strings.view_all_accounts}</a></span></p>
</section>
</#if>
<form method="POST" action="${formUrls.list}" id="account-display" class="customForm" role="accounts display">
    <@p.accountsNav />
    
    <table id="table-listing">
        <caption>${strings.account_management}</caption>

        <thead>
            <tr>
               <th scope="col"> <input  class="delete-all hidden" type="checkbox" name="delete-all">
                   ${strings.email_address}
                   <nav class="account-alpha-browse">
                       <a class="sort-asc" href="?accountsPerPage=${accountsPerPage}&orderField=email&orderDirection=ASC" title="${strings.ascending_order}"></a> 
                       <a class="sort-desc" href="?accountsPerPage=${accountsPerPage}&orderField=email&orderDirection=DESC" title="${strings.descending_order}"></a>
                   </nav>
                </th>
                
                <th scope="col">
                    ${strings.first_name}
                    <nav class="account-alpha-browse">
                        <a class="sort-asc" href="?accountsPerPage=${accountsPerPage}&orderField=firstName&orderDirection=ASC" title="${strings.ascending_order}"></a> 
                        <a class="sort-desc" href="?accountsPerPage=${accountsPerPage}&orderField=firstName&orderDirection=DESC" title="${strings.descending_order}"></a>
                    </nav>
                </th>
                
                <th scope="col">
                    ${strings.last_name}
                    <nav class="account-alpha-browse">
                        <a class="sort-asc" href="?accountsPerPage=${accountsPerPage}&orderField=lastName&orderDirection=ASC" title="${strings.ascending_order}"></a> 
                        <a class="sort-desc" href="?accountsPerPage=${accountsPerPage}&orderField=lastName&orderDirection=DESC" title="${strings.descending_order}"></a>
                    </nav>
                </th>
                
                <th scope="col">
                    ${strings.status}
                    <nav class="account-alpha-browse">
                        <a class="sort-asc" href="?accountsPerPage=${accountsPerPage}&orderField=status&orderDirection=ASC" title="${strings.ascending_order}"></a> 
                        <a class="sort-desc" href="?accountsPerPage=${accountsPerPage}&orderField=status&orderDirection=DESC" title="${strings.descending_order}"></a>
                    </nav>
                </th>
                
                <th scope="col">${strings.roles}</th>
                
                <th scope="col">
                    ${strings.login_count}
                    <nav class="account-alpha-browse">
                        <a class="sort-asc" href="?accountsPerPage=${accountsPerPage}&orderField=count&orderDirection=ASC" title="${strings.ascending_order}"></a> 
                        <a class="sort-desc" href="?accountsPerPage=${accountsPerPage}&orderField=count&orderDirection=DESC" title="${strings.descending_order}"></a>
                    </nav>
                </th>

                <th scope="col">
                    ${strings.last_login}
                    <nav class="account-alpha-browse">
                        <a class="sort-asc" href="?accountsPerPage=${accountsPerPage}&orderField=lastLogin&orderDirection=ASC" title="${strings.ascending_order}"></a> 
                        <a class="sort-desc" href="?accountsPerPage=${accountsPerPage}&orderField=lastLogin&orderDirection=DESC" title="${strings.descending_order}"></a>
                    </nav>
                </th>
            </tr>
        </thead>
    
        <tbody>
            <#list accounts as account>
                <tr>
                    <td>
                        <#if account.deletable>
                            <input type="checkbox" name="deleteAccount" value="${account.uri}" title="${strings.select_account_to_delete}"/>
                            <#assign disableDeleteAccount = '' />
                            <!-- ignored unless submit action is formUrls.delete -->
                        <#else>
                             <#assign disableDeleteAccount = 'class="disable-delete"' />
                        </#if>
    
                        <#if account.editUrl != "">
                            <a ${disableDeleteAccount} href="${account.editUrl}"  title="${strings.click_to_view_account}">${account.emailAddress}</a>
                            <!-- when this link is clicked, editAccount is noticed and all other fields are ignored. -->
                        <#else>
                            <span class="unlinkedAccount">${account.emailAddress}</span>
                        </#if>
                    </td>
                    <td>${account.firstName}</td>
                    <td>${account.lastName}</td>
                    <td>${account.status}</td>
                    <td>
                        <#list account.permissionSets as permissionSet>
                            <div>${permissionSet}</div>
                        </#list>
                    </td>
                    <td>${account.loginCount}</td>
                    <td>
                        <#if account.lastLoginTime??>
                            ${account.lastLoginTime?date?string.medium}
                            <br />${account.lastLoginTime?time?string.short}
                        </#if>
                    </td>
                </tr>
            </#list>
        </tbody>
    </table>
    
    <@p.accountsNav />
</form>

<script type="text/javascript">
    confirm_delete_account_singular = "${strings.confirm_delete_account_singular}"
    confirm_delete_account_plural = "${strings.confirm_delete_account_plural}"
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/account/account.css" />')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/account/accountUtils.js"></script>')}
