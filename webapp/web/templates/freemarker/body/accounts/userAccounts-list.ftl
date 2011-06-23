<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for displaying list of user accounts -->

<form method="POST" action="${formUrls.list}" class="customForm" role="">

    <#--current page: <input type="text" name="pageIndex" value="${page.current}" />
    <br />-->

    <#--sort order:
    <!-- Manolo: I don't know the right way to handle these links in the column headers. --> 
    <#--<#assign directions = ["ASC", "DESC"]>
    <select name="orderDirection" >
      <#list directions as direction>
        <option value="${direction}" <#if orderDirection = direction>selected</#if> >${direction}</option>
      </#list>
    </select> 
    <!-- When orderDirection changes, 
            set pageIndex to 1 
            submit the form (submit action is "list") --> 
    <#--<br />        -->
    

    <#--sort field:
    <!-- Manolo: I don't know the right way to handle these links in the column headers. --> 
    <#--<#assign fields = ["email", "firstName", "lastName", "status", "count"]>
    <select name="orderField" >
      <#list fields as field>
        <option value="${field}" <#if orderField = field>selected</#if> >${field}</option>
      </#list>
    </select> -->
    
    <!-- When orderField changes, 
            set pageIndex to 1
            set orderDirection to "ASC" 
            submit the form (submit action is "list") --> 
   <!-- <br />
    <input type="submit" name="list" value="Refresh page" />--> 
</form>

<form method="POST" action="${formUrls.add}" class="customForm" role="">
    <h3>Account  | <input type="submit" class="submit" value="Add new account" /></h3>
</form>

<#if newUserAccount?? >
    <section class="account-feedback">
        <p>
            A new account for 
            <a href="${newUserAccount.editUrl}">${newUserAccount.firstName} ${newUserAccount.lastName}</a>
            was successfully created. 
            <#if emailWasSent?? > 
                A notification email has been sent to ${newUserAccount.emailAddress}
                with instructions for activating the account and creating a password.
            </#if>
        </p>
    </section>
</#if>

<#if updatedUserAccount?? >
    <section class="account-feedback">
        <p>
            The account for 
            <a href="${updatedUserAccount.editUrl}">${updatedUserAccount.firstName} ${updatedUserAccount.lastName}</a>
            has been updated.
            <#if emailWasSent?? > 
                A confirmation email has been sent to ${updatedUserAccount.emailAddress}
                with instructions for resetting a password. 
                The password will not be reset until the user follows the link provided in this email.
            </#if>
        </p>
    </section>
</#if>

<#if deletedAccountCount?? >
    <section class="account-feedback">
        <p>
            Deleted ${deletedAccountCount} accounts.
        </p>
    </section>
</#if>

<section id="filter-roles">
    <select name="roleFilterUri" id="">
        <option value="" <#if roleFilterUri = "">selected</#if> >Filter by roles</option>
        <#list roles as role>
        <option value="${role.uri}" <#if roleFilterUri = role.uri>selected</#if> >${role.label}</option>
        </#list>
        <!--
        When roleFilterUri or searchTerm changes,
        pageIndex should be set to 1. When any of these changes (including pageIndex), the form 
        should be submitted.
        -->
    </select>
</section>

<form method="POST" action="${formUrls.list}" class="customForm" role="">
    <section id="search-accounts">
        <input type="text" name="" />
        <input class="submit" type="submit" value="Search accounts"/>
        <!--
            When searchTerm changes, 
            set pageIndex to 1
            set orderDirection to "ASC"
            set orderField to "email" 
            submit the form (submit action is "list") 
        -->
    </section>
</form>

<form method="POST" action="${formUrls.list}" id="account-display" class="customForm" role="">
    <section class="accounts">
        <input type="submit" name="delete-account" class="delete-account submit" value="Delete" onClick="changeAction(this.form, '${formUrls.delete}')" />
        <!-- 
            When this is clicked, the checkboxes are noticed and all other fields are ignored. 
            submit the form (submit action is formUrls.delete)
        -->
    
        <nav class="display-tools">
            <span>| <a href="#">n</a> accounts | </span>  
    
            <#assign counts = [25, 50, 100]>
            
            <select name="accountsPerPage" class="accounts-per-page">
                <#list counts as count>
                <option value="${count}" <#if accountsPerPage= count>selected</#if> >${count}</option>
                </#list>
                <!--     
                    When accountsPerPage changes, 
                    set pageIndex to 1 
                    submit the form (submit action is formUrls.list) 
                -->     
            </select>
    
            accounts per page <input type="submit" name="accounts-per-page" value="Update" /> | 
    
            <#if page.previous?has_content>
                <a href="${formUrls.list}?accountsPerPage=${accountsPerPage}&pageIndex=${page.previous}">Previous</a> <!-- only present if current page is not 1.-->
            </#if>
                ${page.current} of ${page.last} 
            <#if page.next?has_content>
                <a href="${formUrls.list}?accountsPerPage=${accountsPerPage}&pageIndex=${page.next}">Next</a><!-- only present if current page is not last page.-->
            </#if>
        </nav>
    </section>

    <table id="account">
        <caption>Account Management</caption>

        <thead>
            <tr>
                <th scope="col">
                    <input class="hide" type="checkbox" name="delete-all" id="">
                    Email Address
                    <nav class="account-alpha-browse">
                        <a class="sort-asc" href="?accountsPerPage=${accountsPerPage}&orderField=email&orderDirection=ASC" title="ascending order"></a> 
                        <a class="sort-desc" href="?accountsPerPage=${accountsPerPage}&orderField=email&orderDirection=DESC" title="descending order"></a>
                    </nav>
                </th>
                <th scope="col">
                    First name
                    <nav class="account-alpha-browse">
                        <a class="sort-asc" href="?accountsPerPage=${accountsPerPage}&orderField=firstName&orderDirection=ASC" title="ascending order"></a> 
                        <a class="sort-desc" href="?accountsPerPage=${accountsPerPage}&orderField=firstName&orderDirection=DESC" title="descending order"></a>
                    </nav>
                </th>
                
                <th scope="col">
                    Last name
                    <nav class="account-alpha-browse">
                        <a class="sort-asc" href="?accountsPerPage=${accountsPerPage}&orderField=lastName&orderDirection=ASC" title="ascending order"></a> 
                        <a class="sort-desc" href="?accountsPerPage=${accountsPerPage}&orderField=lastName&orderDirection=DESC" title="descending order"></a>
                    </nav>
                </th>
                
                <th scope="col">
                    Status
                    <nav class="account-alpha-browse">
                        <a class="sort-asc" href="?accountsPerPage=${accountsPerPage}&orderField=status&orderDirection=ASC" title="ascending order"></a> 
                        <a class="sort-desc" href="?accountsPerPage=${accountsPerPage}&orderField=status&orderDirection=DESC" title="descending order"></a>
                    </nav>
                </th>
                
                <th scope="col">Roles</th>
                
                <th scope="col">
                    Login count
                    <nav class="account-alpha-browse">
                        <a class="sort-asc" href="?accountsPerPage=${accountsPerPage}&orderField=count&orderDirection=ASC" title="ascending order"></a> 
                        <a class="sort-desc" href="?accountsPerPage=${accountsPerPage}&orderField=count&orderDirection=DESC"" title="descending order"></a>
                    </nav>
                </th>
            </tr>
        </thead>
    
        <tbody>
            <#list accounts as account>
                <tr>
                    <td>
                        <input type="checkbox" name="deleteAccount" value="${account.uri}" />
                        <!-- ignored unless submit action is formUrls.delete -->
    
                        <a href="${account.editUrl}" >${account.emailAddress}</a>
                        <!-- when this link is clicked, editAccount is noticed and all other fields are ignored. -->
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
                </tr>
            </#list>
        </tbody>
    </table>
    
    <section class="accounts">
        <input type="submit" name="delete-account" class="delete-account submit" value="Delete" onClick="changeAction(this.form, '${formUrls.delete}')" />
        <!-- 
            When this is clicked, the checkboxes are noticed and all other fields are ignored. 
            submit the form (submit action is formUrls.delete)
        -->
    
        <nav class="display-tools">
            <span>| <a href="#">n</a> accounts | </span>  
    
            <#assign counts = [25, 50, 100]>
            
            <select name="accountsPerPage" class="accounts-per-page">
                <#list counts as count>
                <option value="${count}" <#if accountsPerPage= count>selected</#if> >${count}</option>
                </#list>
                <!--     
                    When accountsPerPage changes, 
                    set pageIndex to 1 
                    submit the form (submit action is formUrls.list) 
                -->     
            </select>
    
            accounts per page <input type="submit" name="accounts-per-page" value="Update" /> | 
    
            <#if page.previous?has_content>
                <a href="${formUrls.list}?accountsPerPage=${accountsPerPage}&pageIndex=${page.previous}">Previous</a> <!-- only present if current page is not 1.-->
            </#if>
                ${page.current} of ${page.last} 
            <#if page.next?has_content>
                <a href="${formUrls.list}?accountsPerPage=${accountsPerPage}&pageIndex=${page.next}">Next</a><!-- only present if current page is not last page.-->
            </#if>
        </nav>
    </section>
</form>

${scripts.add('<script type="text/javascript" src="${urls.base}/js/account/accountUtils.js"></script>')}