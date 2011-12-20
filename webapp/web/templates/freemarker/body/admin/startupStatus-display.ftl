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

    <li class="item ${color}" role="listitem">
        <h4>${item.level}: ${item.shortSourceName}</h4>
        
        <ul class="item-spec" role="navigation">
            <li role="listitem">${item.message}</li>
            <li role="listitem">${item.sourceName}</li>
            <#if item.cause?has_content>
            <li role="listitem"><pre>${item.cause}</pre></li>
            </#if>
        </ul>
    </li>
</#macro>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/startupStatus.css" />')}

<#if status.errorItems?has_content>
    <h2>Fatal error</h2>
    
    <p>${applicationName} detected a fatal error during startup.</p>
   
    <ul id="startup-trace" cellspacing="0" class="trace" role="navigation">
    <#list status.errorItems as item>
      <@statusItem item=item />
    </#list>
    </ul>
</#if>

<#if status.warningItems?has_content>
    <h2>Warning</h2>
    
    <p>${applicationName} issued warnings during startup.</p>
    
    <ul id="startup-trace" cellspacing="0" class="trace" role="navigation"><#list status.warningItems as item>
      <@statusItem item=item />
    </#list>
    </ul>
</#if>

<h2>Startup trace</h2>

<p>The full list of startup events and messages.</p>

<ul id="startup-trace" cellspacing="0" class="trace" role="navigation">
      <#list status.statusItems as item>
          <@statusItem item=item />
      </#list>
</ul>
