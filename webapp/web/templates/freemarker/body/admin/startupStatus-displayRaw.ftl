<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- 
    Template for the raw page that displays the StartupStatus if there 
    are warnings or errors.
    
    "raw" because this template works outside of the usual framework, in 
    case the Freemarker context didn't initialize properly.
    
    This file can't even include a reference to an external CSS file, in case
    the servlet routing filters are broken.
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

<!DOCTYPE html>

<html lang="en">
    <head>
        <title>Startup Status</title>
        
        <style TYPE="text/css">
           #startup-trace {
               width: 100%;
           }
           #startup-trace h4 {
               padding: .5em;
               margin-bottom: 0;
               padding-bottom: .5em;
               padding-top: 1em;
           }
           #startup-trace ul.item-spec {
               margin-bottom: 1em;
           }
           #startup-trace ul.item-spec li{
               padding-left: .5em;
               padding-bottom: .4em;
           }
           #startup-trace li.error {
               background-color: #FFDDDD;
           }
           #startup-trace li.warning{
               background-color: #FFFFDD; 
           }
           #startup-trace li.info {
               background-color: #DDFFDD;
           }
           #startup-trace li.not_executed {
               background-color: #F3F3F0;
           }
           
        </style> 
    </head>

    <body>
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
            
            <#-- If there were no fatal errors, let them go forward from here. -->
            <#if showLink>
                <p><a href="." title="continue">Continue</a></p>
    	    </#if>
            
        </#if>

        <h2>Startup trace</h2>

        <p>The full list of startup events and messages.</p>

        <ul id="startup-trace" cellspacing="0" class="trace" role="navigation">
              <#list status.statusItems as item>
                  <@statusItem item=item />
              </#list>
        </ul>
    </body>
</html>
