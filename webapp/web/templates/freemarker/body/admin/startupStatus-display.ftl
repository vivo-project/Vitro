<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- 
    Template for the page that displays the StartupStatus on request. 
    
    Unlike the "raw" page, this one assumes that the usual Freemarker framework is in place.
-->

<#macro statusItem item>
    <#if item.level = "FATAL">
        <#assign color = "error" >
    <#elseif item.level = "WARNING">
        <#assign color = "warning" >
    <#elseif item.level = "INFO">
        <#assign color = "info" >
    <#elseif item.level = "NOT_EXECUTED">
        <#assign color = "not_executed" >
    <#else>
        <#assign color = "" >
    </#if>
    <tr><td>
        <table cellspacing="0" class="item ${color}">
            <tr class="top">
                <td width="20%">${item.level}</td>
                <td>${item.shortSourceName}</td>
            </tr>
            <tr>
                <td colspan="2">${item.message}</td>
            </tr>
            <tr>
                <td colspan="2">${item.sourceName}</td>
            </tr>
            <#if item.cause??>
                <tr>
                    <td colspan="2">${item.cause}</td>
                </tr>
            </#if>
        </table>
    </td></tr>
</#macro>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/startupStatus.css" />')}

<#if status.errorItems?has_content>
    <h2>Fatal error</h2>
    <p>VIVO detected a fatal error during startup.</p>
    <#list status.errorItems as item>
      <@statusItem item=item />
    </#list>
</#if>

<#if status.warningItems?has_content>
    <h2>Warning</h2>
    <p>VIVO issued warnings during startup.</p>
    <#list status.warningItems as item>
      <@statusItem item=item />
    </#list>
</#if>

<h2>Startup trace</h2>
<p>The full list of startup events and messages.</p>
  <table cellspacing="0" class="trace">
      <#list status.statusItems as item>
          <@statusItem item=item />
      </#list>
  </table>
