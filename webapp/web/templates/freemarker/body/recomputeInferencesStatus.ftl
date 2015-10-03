<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- 
    Template for the page that controls the updating or rebuilding of the Search Index. 
-->

<section id="reasoner" role="region">
    <h3>History</h3>
    <table class="history">
        <tr> <th>Event</th> <th>Message</th> </tr>
        <#if history?has_content >
            <#list history as ie>
               <@showReasonerEvent ie />
            </#list>
        <#else>
            <tr><td colspan="4">Reasoner history is not available.</td></tr>
        </#if>
    </table>
</section>

<#macro showReasonerEvent event>
    <tr>
        <td>${event.event}</td>
        <td>${event.message}</td>
    </tr>
</#macro>
