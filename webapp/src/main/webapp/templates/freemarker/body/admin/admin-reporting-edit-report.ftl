
<#-- $This file is distributed under the terms of the license in LICENSE$ -->
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/reporting.css" />')}


<#if objectClass??>
    <h2>${i18n()["reporting_config_" + objectClass.name]}</h2>
</#if>
<#if addType??>
    <#assign submitUrl = "${submitUrlBase}?addType=${addType?url}" />
<#else>
    <#assign submitUrl = "${submitUrlBase}?editUri=${editUri?url}" />
</#if>
<noscript>
    <section id="error-alert">
        <img src="${urls.images}/iconAlertBig.png" alt="${i18n().alert_icon}"/>
        <p>${i18n().javascript_require_to_edit} <a href="http://www.enable-javascript.com" title="${i18n().javascript_instructions}">${i18n().to_enable_javascript}</a>.</p>
    </section>
</noscript>
<script lang="text/javascript">
    var dataSourceIndex = 0;
    function addDataSource(distibutorName, rank, outputName) {
        distributorName = typeof distributorName !== 'undefined' ? distributorName : "";
        rank = typeof rank !== 'undefined' ? rank : dataSourceIndex + 1;
        outputName = typeof outputName !== 'undefined' ? outputName : "";
        var element = document.getElementById('field_dataSources');
        if (element != null) {
            dataSourceIndex++;
            element.appendChild(createElementFromHTML(
                '<div id="field_dataSource' + dataSourceIndex + '" class="reportDatasource">' +
                <#if readOnly??>
                <#else>
                '<button class="delete" onclick="return deleteDataSource(' + dataSourceIndex + ');"><img src="${urls.images!}/individual/deleteIcon.gif" /></button>' +
                </#if>
                '<input type="hidden" name="dataSourceIndex" value="' + dataSourceIndex + '" <#if readOnly??>readonly</#if> />' +
                '<label>${i18n()["reporting_config_dataSource_distributor"]}</label>' +
                '<select name="dataSource' + dataSourceIndex + '_distributor" <#if readOnly??>readonly</#if> >' +
                <#list datadistributors as datadistributor>
                ((distibutorName == "${datadistributor.name}") ?
                        '<option value="${datadistributor.name}" selected>${datadistributor.name}</option>'
                        :
                        '<option value="${datadistributor.name}">${datadistributor.name}</option>'
                ) +
                </#list>
                '</select>' +
                '<label>${i18n()["reporting_config_dataSource_rank"]}</label>' +
                '<input type="number" name="dataSource' + dataSourceIndex + '_rank" value=' + rank + ' required <#if readOnly??>readonly</#if> />' +
                '<label><span class="required">*</span> ${i18n()["reporting_config_dataSource_outputName"]}</label>' +
                '<input type="text" name="dataSource' + dataSourceIndex + '_outputName" size="50" value="' + outputName + '" required <#if readOnly??>readonly</#if> />' +
                '</div>'
            ));
        }

        return false;
    }
    //                '<button class="delete" onclick="return deleteDataSource(\'field_dataSource' + dataSourceIndex + '\');"><img src="${urls.images!}/individual/deleteIcon.gif" /></button>' +

    function deleteDataSource(id) {
        var element = document.getElementById("field_dataSource" + id);
        if (element != null) {
            element.remove();
        }
        return false;
    }

    function createElementFromHTML(htmlString) {
        var div = document.createElement('div');
        div.innerHTML = htmlString.trim();

        // Change this to div.childNodes to support multiple top-level nodes
        return div.firstChild;
    }
</script>
<section id="dataa-config" role="region">
    <form id="reportConfigForm" action="${submitUrl}" method="post" enctype="multipart/form-data">
        <div id="field_reportName" class="field">
            <label>
                <span class="required">*</span>
                ${i18n()["reporting_config_field_reportName"]}
            </label>
            <p>
                <input type="text" name="reportName" size="50" value="${report.reportName!}" required <#if readOnly??>readonly</#if> />
            </p>
        </div>
        <div id="field_dataSources">
            <#list report.dataSources as dataSource>
                <script lang="text/javascript">
                    addDataSource("${dataSource.distributorName}", ${dataSource.rank}, "${dataSource.outputName}");
                </script>
            </#list>
        </div>
        <p class="addDataSource"><a onclick="return addDataSource()"><img src="${urls.images!}/individual/addIcon.gif" alt="${i18n().reporting_config_add_datasource}"> ${i18n().reporting_config_add_datasource}</a></p>
        <#if report.implementsTemplate>
            <#if report.template?? && report.reportName??>
                <div class="downloadTemplate">
                    <a href="${submitUrlBase}/${report.reportName!}?download=template">${i18n().reporting_download_template} <img src="${urls.images!}/download-icon.png" /></a>
                </div>
            </#if>
            <div class="reportTemplate">
                ${i18n().reporting_template} <input type="file" name="template" />
            </div>
        </#if>
        <#if readOnly??>
            <a href="${submitUrlBase}">cancel</a>
        <#else>
        <input type="hidden" name="submitted" value="submitted" />
        <input id="save" value="${i18n().reporting_config_save}" role="button" type="submit" class="submit" />
            <a href="${submitUrlBase}">cancel</a>
        <#if addType??>
        <#else>
            <button onclick="return confirmDeleteThis()">
                <img src="${urls.images!}/individual/deleteIcon.gif" alt="${i18n().reporting_config_delete}"> ${i18n().reporting_config_delete}
            </button>
            <script lang="text/javascript">
                function confirmDeleteThis() {
                    var confirmed = confirm("${i18n().reporting_config_delete_confirm}");
                    if (confirmed) {
                        window.location.href = "${submitUrlBase}?deleteUri=${editUri?url}";
                    }
                    return false;
                }
            </script>
        </#if>
        </#if>
    </form>
</section>
