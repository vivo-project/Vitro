<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template viewing the authorization mechanisms: current identifiers, factories, policies, etc. -->

<style media="screen" type="text/css">
table.threadInfo {
    margin: 10px 10px 10px 10px;
    border: medium groove black;
    text-align: left;
}
table.threadInfo th, td {
    padding: 4px 10px 4px 10px;
}
table.threadInfo th {
    font-weight: bolder;
}
</style>


<h2>Background Threads</h2>

<section id="show-threads" role="region">
    <#list threads as threadInfo>
        <table class="threadInfo ${threadInfo.workLevel}" summary="Thread ${threadInfo.name}">
            <tr><th>Name</th><td>${threadInfo.name}</td></tr>
            <tr><th>WorkLevel</th><td>${threadInfo.workLevel}</td></tr>
            <tr><th>Since</th><td>${threadInfo.since}</td></tr>
            <tr><th>Flags</th><td>${threadInfo.flags}</td></tr>
        </table>
    </#list>
</section>