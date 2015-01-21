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


<h2>${i18n().background_threads}</h2>

<section id="show-threads" role="region">
    <table class="threadInfo" summary="Status of background threads.">
        <tr>
            <th>${i18n().name}</th>
            <th>${i18n().work_level}</th>
            <th>${i18n().since}</th>
            <th>${i18n().flags}</th>
        </tr>
        <#list threads as threadInfo>
            <tr>
                <td>${threadInfo.name}</td>
                <td>${threadInfo.workLevel}</td>
                <td>${threadInfo.since}</td>
                <td>${threadInfo.flags}</td>
        </#list>
    </table>
</section>