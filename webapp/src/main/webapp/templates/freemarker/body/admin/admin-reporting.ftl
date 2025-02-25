<#-- $This file is distributed under the terms of the license in LICENSE$ -->
<#if adminControls>
    <h2>${i18n().reporting_config_title}</h2>
<#else>
    <h2>${i18n().reporting_title}</h2>
</#if>
<section id="reporting-config" role="region">
    <#if reports??>
        <form id="reportingConfigForm" action="${submitUrlBase}" method="get">
            <select name="addType">
                <#list reportTypes as reportType>
                    <option value="${reportType.name}">${i18n()["reporting_config_" + reportType.name]}</option>
                </#list>
            </select>
            <input id="add" value="${i18n().reporting_config_add}" role="button" type="submit" class="submit" accesskey="a">
        </form>
    </#if>
    <table id="table-listing">
        <tr>
            <th>${i18n().reporting_config_name}</th>
            <th>${i18n().reporting_config_type}</th>
            <th>${i18n().reporting_config_edit}</th>
        </tr>
        <#if reports??>
            <#list reports as report>
                <tr>
                    <td>
                        ${report.reportName}
                    </td>
                    <td>
                        ${i18n()["reporting_config_" + report.className]}
                    </td>
                    <td>
                        <a href="${submitUrlBase}?editUri=${report.uri?url}"><img src="${urls.images!}/individual/editIcon.gif" alt="${i18n().reporting_config_edit}"></a>
                        <#if report.implementsXml>
                            <a href="${submitUrlBase}/${report.reportName}?download=xml">${i18n().reporting_download_xml}</a>
                        </#if>
                        <#if report.runnable>
                            <a href="${submitUrlBase}/${report.reportName}"><img src="${urls.images!}/run.png" alt="${i18n().reporting_run}"></a>
                        </#if>
                    </td>
                </tr>
            </#list>
        </#if>
    </table>
</section>
