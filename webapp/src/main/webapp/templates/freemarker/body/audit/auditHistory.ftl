<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<section id="audit" role="region">
    <h2>Audit history</h2>
    <form>
      <label for="user">User uri:</label>
      <select name="user" id="user">
        <option value="">Any</option>
        <#list users as curUser>
          <@printSelectOption curUser userUri />
        </#list>
      </select>
      <label for="user">Graph uri:</label>
      <select name="graph" id="graph">
        <option value="">Any</option>
        <#list graphs as graph>
          <@printSelectOption graph selectedGraphUri />
        </#list>
      </select>
      <label for="user">Datasets per page</label>
      <select name="limit" id="limit">
        <#list limits as curLimit>
          <@printSelectOption curLimit limit />
        </#list>
      </select>
      <label for="user">Order</label>
      <select name="order" id="order">
        <#list orders as curOrder>
          <@printSelectOption curOrder order />
        </#list>
      </select>
      <input type="submit" value="Update" >
    </form>

    <table class="history">
        <tbody>
            <tr>
                <th>Pos</th>
                <th>User</th>
                <th>Date</th>
                <th>Changes</th>
            </tr>
            <#assign pos = results.offset>
            <#list results.datasets as dataset>
                <#assign pos = pos + 1>
                <tr>
                    <td>${pos}</td>
                    <td>${dataset.userLastName!} ${dataset.userFirstName!} ${dataset.userEmail}</td>
                    <td>${dataset.requestTime?datetime}</td>
                    <td>
                        <#list dataset.graphUris as graphUri>
                            <#assign added = listAddedStatements(dataset, graphUri)>
                            <#assign removed = listRemovedStatements(dataset, graphUri)>

                            <b>Graph</b>: ${graphUri}<br />
                            <#if added??>
                                <br /><b>Added</b>:<br />
                                <div><pre style="font-size: 0.8em;">${added?html}</pre></div>
                            </#if>
                            <#if removed??>
                                <br /><b>Removed</b>:<br />
                                <div><pre style="font-size: 0.8em;">${removed?html}</pre></div>
                            </#if>
                        </#list>
                    </td>
                </tr>
            </#list>
        </tbody>
    </table>
    <#if prevPage??><a class="prev" href="${prevPage}" title="${i18n().previous}">${i18n().previous}</a></#if>
    <#if nextPage??><a class="next" href="${nextPage}" title="${i18n().next_capitalized}">${i18n().next_capitalized}</a></#if>
</section>

<#macro printSelectOption currentOption selectedOption >
  <#assign selected = "">
  <#if currentOption = selectedOption>
    <#assign selected = "selected=\"selected\"">
  </#if>
  <option value="${currentOption}"${selected}>${currentOption}</option>
</#macro>


${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/audit/audit.css" />')}

