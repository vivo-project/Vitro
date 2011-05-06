<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for displaying list of user accounts -->

<h2>
    Accounts
</h2>

<div style="border: solid">
  <form method="POST" action="${formUrl}">
    <input type="submit" name="add" value="Add new account" />
    <!-- When this is clicked, all other fields are ignored. -->
    
    <input type="submit" name="delete" value="Delete selected accounts" />
    <!-- When this is clicked, the checkboxes are noticed and all other fields are ignored. -->
    <br>

  	current page: <input type="text" name="pageIndex" value="${page.current}" />
  	<br>
  	
    <!--
      When  roleFilterUri or searchTerm changes,
      pageIndex should be set to 1. When any of these changes (including pageIndex), the form 
      should be submitted.
    -->
  
  	show <input type="text" name="accountsPerPage" value="${accountsPerPage}" /> accounts per page
  	<!-- When accountsPerPage changes, 
  	        set pageIndex to 1 
  	        submit the form (submit action is "list") --> 
  	<br>
  	
  	sort order:
  	<!-- Manolo: I don't know the right way to handle these links in the column headers. --> 
  	<#assign directions = ["ASC", "DESC"]>
  	<select name="orderDirection" >
      <#list directions as direction>
        <option value="${direction}" <#if orderDirection = direction>selected</#if> >${direction}</option>
      </#list>
  	</select> 
  	<!-- When orderDirection changes, 
  	        set pageIndex to 1 
  	        submit the form (submit action is "list") --> 
  	<br>

  	sort field:
  	<!-- Manolo: I don't know the right way to handle these links in the column headers. --> 
  	<#assign fields = ["email", "firstName", "lastName", "status", "count"]>
  	<select name="orderField" >
      <#list fields as field>
        <option value="${field}" <#if orderField = field>selected</#if> >${field}</option>
      </#list>
  	</select> 
  	<!-- When orderField changes, 
  	        set pageIndex to 1
  	        set orderDirection to "ASC" 
  	        submit the form (submit action is "list") --> 
  	<br>

  	search term: <input type="text" name="searchTerm" value="${searchTerm}" /> 
  	<!-- When searchTerm changes, 
  	        set pageIndex to 1
  	        set orderDirection to "ASC"
  	        set orderField to "email" 
  	        submit the form (submit action is "list") --> 
  	<br>

  	<select name="roleFilterUri">
      <option value="" <#if roleFilterUri = "">selected</#if> >Filter by roles</option>
      <#list roles as role>
        <option value="${role.uri}" <#if roleFilterUri = role.uri>selected</#if> >${role.label}</option>
      </#list>
    </select>
  	<!-- When searchTerm changes, 
  	        set pageIndex to 1
  	        set orderDirection to "ASC"
  	        set orderField to "email" 
  	        submit the form (submit action is "list") --> 
    <br>

    <input type="submit" name="list" value="Refresh page" />
  </form>
</div>

<div style="border: solid">
  Current page: ${page.current} 
   <br> Last page: ${page.last} 

   <#if page.next?has_content>
     <br> Next page: ${page.next} 
     <!-- only present if current page is not last page. -->
   </#if>
   
   <#if page.previous?has_content>
     <br> Previous page: ${page.previous} 
     <!-- only present if current page is not 1. -->
   </#if>
</div>

<div style="border: solid">
	<table style="border: solid">
		<tr>
			<th>Email Address</th>
			<th>First Name</th>
			<th>Last Name</th>
			<th>Status</th>
			<th>Roles</th>
			<th>Login Count</th>
		</tr>
		<#list accounts as account>
		  <tr>
		    <td>
		      <input type="checkbox" name="deleteAccount" value="${account.uri}" />
		      <!-- ignored unless submit action is "delete" -->
		      
		      <a href="${formUrl}?edit&editAccount=${account.uri}" >${account.emailAddress}</a>
		      <!-- if submit action is "edit", editAccount is noticed and all other fields are ignored. -->
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
	</table>
	<br>
	
	link on user's email address currently does nothing
</div>
