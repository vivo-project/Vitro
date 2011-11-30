<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-----------------------------------------------------------------------------
    Macro for generating number of accounts, pagination, accounts per page, 
    and delete function.
------------------------------------------------------------------------------>


<#assign counts=[25, 50, 100] /> <#-- accounts per page-->

<#macro accountsNav accountsCount=counts>

  <section class="accounts">
      <input type="submit" name="delete-account" class="delete-account delete" value="Delete" onClick="changeAction(this.form, '${formUrls.delete}')" />
      <!-- 
          When this is clicked, the checkboxes are noticed and all other fields are ignored. 
          submit the form (submit action is formUrls.delete)
      -->

      <nav class="display-tools">
          <span>| ${total} accounts | </span>  

          <select name="accountsPerPage" class="accounts-per-page">
              <#list accountsCount as count>
              <option value="${count}" <#if accountsPerPage=count>selected</#if> >${count}</option>
              </#list>
             <option value="${total}" <#if accountsPerPage=total>selected</#if> >All</option>
              <!--     
                  When accountsPerPage changes, 
                  set pageIndex to 1 
                  submit the form (submit action is formUrls.list) 
              -->     
          </select>

          accounts per page <input type="submit" name="accounts-per-page" value="Update" /> | 

          <#if page.previous?has_content>
              <a href="${formUrls.list}?accountsPerPage=${accountsPerPage}&pageIndex=${page.previous}" title="previous">Previous</a> <!-- only present if current page is not 1.-->
          </#if>
              ${page.current} of ${page.last} 
          <#if page.next?has_content>
              <a href="${formUrls.list}?accountsPerPage=${accountsPerPage}&pageIndex=${page.next}" title="next">Next</a><!-- only present if current page is not last page.-->
          </#if>
      </nav>
  </section>

</#macro>