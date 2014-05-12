<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-----------------------------------------------------------------------------
    Macro for generating number of accounts, pagination, accounts per page, 
    and delete function.
------------------------------------------------------------------------------>


<#assign counts=[25, 50, 100] /> <#-- accounts per page-->

<#macro accountsNav accountsCount=counts>

  <section class="accounts">
      <input type="submit" name="delete-account" class="delete-account delete" value="${i18n().delete_button}" onClick="changeAction(this.form, '${formUrls.delete}')" />
      <!-- 
          When this is clicked, the checkboxes are noticed and all other fields are ignored. 
          submit the form (submit action is formUrls.delete)
      -->

      <nav class="display-tools">
          <span>| ${total} ${i18n().accounts} | </span>  

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

          ${i18n().accounts_per_page} <input type="submit" name="accounts-per-page" value="${i18n().update_button}" /> | 
          <input id="roleTypeContainer" type="hidden" name="roleFilterUri" value="">
          <#if page.previous?has_content>
              <a id="previousPage" href="${formUrls.list}?accountsPerPage=${accountsPerPage}&pageIndex=${page.previous}" title="${i18n().previous}">${i18n().previous}</a> <!-- only present if current page is not 1.-->
          </#if>
              ${page.current} of ${page.last} 
          <#if page.next?has_content>
              <a id="nextPage" href="${formUrls.list}?accountsPerPage=${accountsPerPage}&pageIndex=${page.next}" title="${i18n().next_capitalized}">${i18n().next_capitalized}</a><!-- only present if current page is not last page.-->
          </#if>
      </nav>
  </section>

</#macro>