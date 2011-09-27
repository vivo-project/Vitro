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

<html>
    <head>
        <title>Startup Status</title>
        
        <style TYPE="text/css">
            table.item {
                border: thin solid black;
                font-family: monospace;
                width: 100%;
            }
            table.item tr.top {
                font-size: larger;
            }
            table.item td {
                border: thin solid black;
            }
            .error td {
                background: #FFDDDD;
                font-weight: bolder;
            }
            .warning td {
                background: #FFFFDD;
            }
            .info td {
                background: #DDFFDD;
            }
            .not_executed td {
                color: #444444;
            }
        </style> 
    </head>

    <body>
    	<#if status.errorItems?has_content>
    	    <h2>Fatal error</h2>
    	    <p>${contextPath} detected a fatal error during startup.</p>
    	    <#if showLink>
                <p><a href=".">Continue</a></p>
    	    </#if>
    	    <#list status.errorItems as item>
              <@statusItem item=item />
    	    </#list>
    	</#if>
    
    	<#if status.warningItems?has_content>
    	    <h2>Warning</h2>
    	    <p>${contextPath} found problems during startup.</p>
    	    <#if showLink>
                <p><a href=".">Continue</a></p>
    	    </#if>
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
    </body>
</html>
